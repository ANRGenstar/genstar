package gospl.io.insee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPather;
import org.htmlcleaner.XPatherException;

import au.com.bytecode.opencsv.CSVReader;
import core.configuration.dictionary.DemographicDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.io.ReadDictionaryUtils;

/**
 * provides utils to parse dictionaries describing data from various sources
 * 
 * FIXME: change the way attribute are created - then suppress options > see {@link DemographicAttributeFactory}
 * 
 * @author Samuel Thiriot
 *
 */
public class ReadINSEEDictionaryUtils {

	private static Logger logger = LogManager.getLogger();
	
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readFromWebsite(
			String url) {
		return readFromWebsite(url, null);
	}
	
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readFromWebsite(
			String url, String splitCode) {
		try {
			return readFromWebsite(new URL(url), splitCode);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("malformed url", e);
		}
	}

	/**
	 * Attempts to find a common separator in different strings
	 * @param tokens
	 * @return
	 */
	public static String detectSeparator(Collection<String> tokens) {

	    String commonStr="";
	    String smallStr ="";        

	    //identify smallest String      
	    for (String s :tokens) {
	        if(smallStr.length()< s.length()){
	            smallStr=s;
	        }
	    }

	    String tempCom="";
	    char [] smallStrChars=smallStr.toCharArray();               
	    for (char c: smallStrChars){
	        tempCom+= c;

	        for (String s: tokens){
	            if(!s.contains(tempCom)){
	                tempCom=""+c;
	                for (String s2 : tokens){
	                    if(!s2.contains(tempCom)){
	                        tempCom="";
	                        break;
	                    }
	                }
	                break;
	            }               
	        }

	        if(tempCom!="" && tempCom.length()>commonStr.length()){
	            commonStr=tempCom;  
	        }                       
	    }   

	    return commonStr;
	}
	
	// TODO use splitCode
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readFromWebsite(URL url, String splitCode) {
		
		logger.debug("reading a dictionnary of data from URL {}", url);
		
        TagNode tagRoot;
        HtmlCleaner cleaner = new HtmlCleaner();
		try {
			tagRoot = cleaner.clean(url);
		} catch (IOException e1) {
			throw new RuntimeException("unable to connect this website: "+url, e1);
		}
        
		Map<String,List<String>> variable2modalities = new HashMap<>();
		
		XPather xpath1 = new XPather("//div[@id='dictionnaire']/div[1]");
		
		XPather xpath = new XPather("./ul/li");	
		try {
			
			TagNode nodeDict = (TagNode)xpath1.evaluateAgainstNode(tagRoot)[0];

			for (Object nodeVariableO: xpath.evaluateAgainstNode(nodeDict)) {
				TagNode nodeVariable = (TagNode)nodeVariableO;
				String variable = null;
				for (Object c: nodeVariable.getAllChildren()) {
					if (c instanceof ContentNode) {
						String v2 = ((ContentNode)c).getContent().trim();
						if (variable == null || v2.length() > variable.length())
							variable = v2;
					}
				}
				//System.out.println("variable: "+variable);
				
				variable = variable.replaceAll("[\n\r]", "");
				
				List<String> modalities = new LinkedList<>();
				for (Object nodeModalityO: xpath.evaluateAgainstNode(nodeVariable)) {
					TagNode nodeModality = (TagNode)nodeModalityO;
					//System.out.println("   modality: "+nodeModality.getText().toString().trim());
					modalities.add(nodeModality.getText().toString().trim().replaceAll("[\n\r]", "").replaceAll("[\\t]", " ").replaceAll("[\\s]+", " "));
				}
				
				variable2modalities.put(variable, modalities);
			}
		} catch (XPatherException e) {
			throw new RuntimeException("error while parsing the INSEE webpage", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("error while parsing the INSEE webpage", e);
		}

		// now we have all the possible variables and their modalities
		// we will now create a list of variable and their modalities
		Map<String,Map<String,String>> variable2modality2text = new HashMap<>(variable2modalities.size());
		
		// let's try to find a general common substring which should be the separator 
		String separator = detectSeparator(variable2modalities.values().stream()
											.flatMap(List::stream)
											.collect(Collectors.toList())
											);
		
		logger.info("detected separator: "+separator);
		if (separator.length() > 0) {
			// there is a common substring; let's use it
			
			for (Map.Entry<String,List<String>> e: variable2modalities.entrySet()) {
				
				Map<String,String> modality2txt = new LinkedHashMap<>(e.getValue().size());
				for (String v: e.getValue()) {
					int idx = v.indexOf(separator);
					modality2txt.put(v.substring(0, idx), v.substring(idx+separator.length(),v.length()));
				}
				
				variable2modality2text.put(e.getKey(), modality2txt);
			}
			
			
		} else {
			
			// there is no common substring; we will try to create that for each variable
			for (Map.Entry<String,List<String>> e: variable2modalities.entrySet()) {
				
				separator = detectSeparator(e.getValue());
				if (separator.length() == 0)
					throw new IllegalArgumentException("unable to detect separator for modalities; please define it manually");
				
				Map<String,String> modality2txt = new LinkedHashMap<>(e.getValue().size());
				for (String v: e.getValue()) {
					int idx = v.indexOf(separator);
					modality2txt.put(v.substring(0, idx), v.substring(idx+separator.length(),v.length()));
				}
				
				variable2modality2text.put(e.getKey(), modality2txt);
			}
			

		}
		
		logger.info("read modalities {}", variable2modality2text);
		
		// now detect a separator for variable names
		String separatorName = detectSeparator(variable2modalities.keySet());
		
		// now we have everything to construct variables !
		List<DemographicAttribute<? extends IValue>> attributes = new ArrayList<>(variable2modality2text.size());

		for (Map.Entry<String,Map<String,String>> e: variable2modality2text.entrySet()) {
			String variableName;
			String variableDescription = null;
			if (separatorName.length() == 0)
				variableName = e.getKey();
			else {
				int idx = e.getKey().indexOf(separatorName);
				variableName = e.getKey().substring(0, idx);
				variableDescription = e.getKey().substring(idx+separatorName.length(),e.getKey().length());
			}
				
			final Map<String,String> modalities = e.getValue();
			final boolean isRange = ReadDictionaryUtils.detectIsRange(modalities.values());
			final boolean isInteger = !isRange && ReadDictionaryUtils.detectIsInteger(modalities.values());
			
			GSEnumDataType dataType = GSEnumDataType.Nominal;
			if (isRange) {
				dataType = GSEnumDataType.Range;
			} else if (isInteger) {
				dataType = GSEnumDataType.Integer;
			} 
				
			// TODO add coding as a mapped attribute ???
			
			try {
				DemographicAttribute<? extends IValue> att = DemographicAttributeFactory.getFactory().createAttribute(
						variableName, 
						dataType, 
						new ArrayList<String>(modalities.keySet()) /*, 
						new ArrayList<String>(modalities.values()),
						attType, 
						null, 
						null */
						);
				att.setDescription(variableDescription);
				// TODO is this range processing ok???
				attributes.add(att);
			} catch (GSIllegalRangedData e1) {
				throw new IllegalArgumentException("wrong range for variable "+variableName, e1);
			}
		}
		
		return new DemographicDictionary<DemographicAttribute<? extends IValue>>(attributes);
	}

	
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readDictionnaryFromMODFile(String filename, String encoding) {
		
		if (encoding == null) {
			// TODO automatic detection
			
		}
		
		return readDictionnaryFromMODFile(new File(filename), encoding);
	}
	
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readDictionnaryFromMODFile(String filename) {
		return readDictionnaryFromMODFile(filename, Charset.defaultCharset().name());
	}
	
	/**
	 * Actual creation of an attribute
	 * @param modalitiesCode2Lib
	 * @param previousCode
	 * @param previousLib
	 * @return
	 * @throws GSIllegalRangedData
	 */
	protected static DemographicAttribute<? extends IValue> createAttribute(
			Map<String,String> modalitiesCode2Lib,
			String previousCode,
			String previousLib
			) throws GSIllegalRangedData {

		final boolean isRange = ReadDictionaryUtils.detectIsRange(modalitiesCode2Lib.values());
		final boolean isInteger = !isRange && ReadDictionaryUtils.detectIsInteger(modalitiesCode2Lib.values());
				
		List<Object> actualValues = null;
		
		GSEnumDataType dataType = GSEnumDataType.Nominal;
		if (modalitiesCode2Lib.isEmpty() || modalitiesCode2Lib.size()==1) {
			// TODO define how this should be... defined !
			if (modalitiesCode2Lib.isEmpty())
				modalitiesCode2Lib.put(previousCode, previousLib);
			dataType = GSEnumDataType.Nominal; // unfortunatly we don't know exactly what it is
			actualValues = new LinkedList<>(modalitiesCode2Lib.keySet());

		} else if (isRange) {
			dataType = GSEnumDataType.Range;
			actualValues = new LinkedList<>(modalitiesCode2Lib.keySet());
		} else if (isInteger) {
			dataType = GSEnumDataType.Integer;
		} else {
			actualValues = new LinkedList<>(modalitiesCode2Lib.keySet());
		}
		
		List<String> labels = new LinkedList<>(modalitiesCode2Lib.values());

		
		logger.info("detected attribute {} - {}, {} with {} modalities: {} and codes {}", 
				previousCode, previousLib, dataType, modalitiesCode2Lib.size(),
				labels, actualValues);


		DemographicAttribute<? extends IValue> att = DemographicAttributeFactory.getFactory().createAttribute(
				previousCode, 
				dataType, 
				labels,
				actualValues
				);
		att.setDescription(previousLib);
						
		return att;
	}
	
	/**
	 * Example of such a file:
	 * VAR_CODE;VAR_LIB;MOD_CODE;MOD_LIB
	 * REGION;"Région du lieu de résidence";01;"Guadeloupe"
	 * REGION;"Région du lieu de résidence";02;"Martinique"
	 * REGION;"Région du lieu de résidence";03;"Guyane"
	 * NUMMR;"Numéro du ménage dans la région (anonymisé)";Z;"Individu hors ménage"
	 * ACHLR;"Période regroupée d'achèvement de la construction de la maison ou de l'immeuble";1;"Avant 1919"
	 * ACHLR;"Période regroupée d'achèvement de la construction de la maison ou de l'immeuble";2;"De 1919 à 1945"
	 * 
	 * @param f
	 * @return
	 */
	public static IGenstarDictionary<DemographicAttribute<? extends IValue>> readDictionnaryFromMODFile(
			File f, String encoding) {
		
		logger.info("reading a dictionnary of data from file {}", f);
		CSVReader reader = null;
		try {
			InputStreamReader isReader = new InputStreamReader(new FileInputStream(f), encoding);
			reader = new CSVReader(isReader,';'); // , '\t'
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("unable to find file "+f);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Collection<DemographicAttribute<? extends IValue>> attributes = new LinkedList<>();
		
		try {
			
			// read title line 
			String[] s = reader.readNext();
			if (!Arrays.equals(s, new String[]{"VAR_CODE","VAR_LIB","MOD_CODE","MOD_LIB"})) {
				throw new IllegalArgumentException("the first line of such a file should be exactly VAR_CODE;VAR_LIB;MOD_CODE;MOD_LIB");
			}

			String previousCode = null;
			String previousLib = null;
			Map<String,String> modalitiesCode2Lib = new LinkedHashMap<>();
			
			while ((s = reader.readNext()) != null) {
		        
				// ignore empty lines
				if (s.length == 0 || ( s.length == 1 && s[0].length() == 0) )
					continue;
				
				
				DemographicAttribute<? extends IValue> att = null;

				final String varCode = s[0];
				final String varLib = s[1];
				
				if (!varCode.equals(previousCode)) {
					if (previousCode != null) {
						// we finished the previous attribute, let's create it
						att = createAttribute(modalitiesCode2Lib, previousCode, previousLib);
						attributes.add(att);						
						// restart from a fresh ground
						modalitiesCode2Lib.clear();
					}
					previousCode = varCode;
					previousLib = varLib;
				}
				
				if (s.length > 2) {
					final String modCode = s[2];
					final String modLib = s[3];
					
					modalitiesCode2Lib.put(modCode, modLib);
					
				}
		     }
			
			if (previousCode != null) {
				// we finished the previous attribute, let's create it
				DemographicAttribute<? extends IValue> att = createAttribute(modalitiesCode2Lib, previousCode, previousLib);
				attributes.add(att);		
			}

			
		} catch (IOException e) {
			throw new RuntimeException("error while reading file", e);
		} catch (GSIllegalRangedData e) {
			throw new IllegalArgumentException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return new DemographicDictionary<DemographicAttribute<? extends IValue>>(attributes);
		
	}
	private ReadINSEEDictionaryUtils() {}

}
