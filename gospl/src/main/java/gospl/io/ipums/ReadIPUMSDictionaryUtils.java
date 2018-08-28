package gospl.io.ipums;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.configuration.dictionary.AttributeDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;

/**
 * Provide utilities to read data dictionary (i.e. attribute and value description) from IPUMS data set
 * 
 * @author kevinchapuis
 *
 */
public class ReadIPUMSDictionaryUtils {

	private Logger log = LogManager.getLogger();
	private static final Level LEVEL = Level.TRACE;
			
	
	// Specific regex
	private static final String GENERAL_REGEX = "GS_REXEG";
	
	// File formating
	public static final String DETAILED_ATTRIBUTE_TAG = "[detailed version]";
	public static final String TAB_SEPARATOR = "\t";
	public static final String ESCAPE_FORMAT_CHAR = "\\";
	
	// Attribute definition
	public static final String FILE_TYPE_HEADER = "File Type";
	public static final String VARIABLE_COL = "Variable";
	private static final List<String> REGEX_HEADER = Arrays.asList(
			"COUNTRY", "YEAR", "SAMPLE", "SERIAL", "PERNUM", "PERWT",
			"Variable Availability Key:");
	
	// Key represent CODE NAME and value ATTRIBUTE NAME
	private Map<String, String> attributeNames;

	// Sample definition
	public static final String SAMPLE_HEADER = "Samples selected";
	public static final String DENSITY_VAR = "Density";
	
	private String country;
	private int year;
	private double globalSampleSize;
	private double sampleSize;
	
	/**
	 * Main method to read IPUMS dictionary from a Rich Text File (.rtf)
	 * 
	 * @param dictionary
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public IGenstarDictionary<Attribute<? extends IValue>> readDictionaryFromRTF(File dictionary) throws GSIllegalRangedData {
		
		IGenstarDictionary<Attribute<? extends IValue>> ad = new AttributeDictionary();
		
		List<String> ipums_dic = null;
		try {
			ipums_dic = Files.readAllLines(dictionary.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Remove hidden character header
		boolean header = true;
		while(header) {
			if(!ipums_dic.get(0).contains(SAMPLE_HEADER)) {
				ipums_dic.remove(0);
			} else {
				header = false;
			}
		}
		
		/*
		 * Split the whole file into recognizable pieces: sample description, attribute description, attributes themself
		 */
		Map<String, List<String>> paragraphes = new HashMap<>();
		String ref = "";
		for(String line : ipums_dic) {
			if(line.isEmpty() || (line.length() == 1 && line.contains(ESCAPE_FORMAT_CHAR)) && !ref.isEmpty()) {
				ref = "";
			} else {
				if(ref.isEmpty()) {
					final String theLine = line;
					if(REGEX_HEADER.stream().anyMatch(regex -> theLine.contains(regex))) {
						ref = GENERAL_REGEX;
					} else {
						if(line.contains(SAMPLE_HEADER)) {
							ref = SAMPLE_HEADER;
							paragraphes.put(ref, new ArrayList<>());
						} else if(line.contains(FILE_TYPE_HEADER)) {
							ref = FILE_TYPE_HEADER;
							paragraphes.put(ref, new ArrayList<>());
						} else {
							ref = line.replace(ESCAPE_FORMAT_CHAR, "");
							paragraphes.put(ref, new ArrayList<>());
						}
					}
				} else if(!ref.equals(GENERAL_REGEX)) {
					line = line.replace(ESCAPE_FORMAT_CHAR, "");
					line = line.replaceAll("/", " ");
					paragraphes.get(ref).add(line.trim());
				}
			}
		}
		
		this.setAttributesName(paragraphes);
		this.setSampleDescription(paragraphes.get(SAMPLE_HEADER));
		
		/*
		 *  Retrieve the paragraph of each attribute {attribute name :: lines of variable}
		 */
		Map<String, List<String>> attMap = attributeNames.entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getKey(), 
						e -> paragraphes.entrySet().stream()
							.filter(l -> l.getKey().contains(e.getValue()))
									.findFirst().get().getValue()
							));
		
		/*
		 * Map of {Detailed attribute Code = Aggregated Attribute Code} to build referent attribute FROM aggregated attribute
		 */
		Map<String, String> aggAtt = new HashMap<>();
		for(Entry<String,String> attNames : attributeNames.entrySet()) {
			if(attNames.getValue().contains(DETAILED_ATTRIBUTE_TAG)) {
				String aggName = attNames.getValue();
				String refName = attributeNames.entrySet().stream()
					.filter(e -> !e.getValue().equals(aggName) 
							&& e.getValue().contains(aggName.replace(DETAILED_ATTRIBUTE_TAG, "")))
					.findFirst().get().getKey();
				aggAtt.put(attNames.getKey(), refName);
			}
		}
		
		log.log(LEVEL, "Had mapped attributes features:\n{}\n"
				+ "Aggregated attributes: {}\n"
				+ "Attribute names: {}", 
				attMap.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				aggAtt.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining(", ")),
				attributeNames.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining(",")));
		
		/*
		 *  Convert lines of variable into record attribute: 
		 *  e.g. AGE={000,005,...120} & age={moins de 5 an, ... 120 ans et plus}
		 *  In fact, IPUMS data comes in a encoded form and a full form. So we have a record attribute
		 *  with each encoded value being binded to with one full form value 
		 */
		List<IPUMSAttribute> ipumsAttributes = attMap.keySet().stream()
				.filter(k -> !aggAtt.containsKey(k) && !aggAtt.containsValue(k))
				.map(k -> new IPUMSAttribute(k, attributeNames.get(k), attMap.get(k)))
				.collect(Collectors.toList());

		List<Attribute<? extends IValue>> attList = new ArrayList<>();
		for(IPUMSAttribute ipumsAtt : ipumsAttributes) {
			Attribute<? extends IValue> att = ipumsAtt.getAttribute(); 
			attList.addAll(Arrays.asList(att, att.getReferentAttribute()));
		}
		
		/*
		 * Do the same thing as previously but with aggregated attribute. There must be
		 * built with the referent attribute. Hence, it is the creation of a record referent
		 * attribute and its aggregated attribute (which cannot be a record rigth now)
		 */
		for(String attCode : aggAtt.keySet()) {
			
			String attNameRef = attributeNames.get(attCode);
			List<String> rawContentRef = attMap.get(attCode);
			
			String attNameAgg = attributeNames.get(aggAtt.get(attCode));
			List<String> rawContentAgg = attMap.get(aggAtt.get(attCode));
						
			Attribute<? extends IValue> aggregateAttribute = new IPUMSAttribute(
						attCode, attNameRef, rawContentRef, attNameAgg, rawContentAgg)
					.getAttribute();
			
			Attribute<? extends IValue> aggRefAtt = aggregateAttribute.getReferentAttribute();
			
			attList.addAll(Arrays.asList(aggregateAttribute, aggRefAtt, aggRefAtt.getReferentAttribute()));
			
		}
		
		List<Attribute<? extends IValue>> referents = attList.stream()
				.filter(a -> a.getReferentAttribute().equals(a)).collect(Collectors.toList());
		attList.removeAll(referents);
		
		ad.addAttributes(referents);
		ad.addAttributes(attList);
		
		return ad;
	}
	
	/**
	 * Retrieve the attribute name from IPUMS paragraph definition
	 * @param list
	 * @return
	 */
	protected void setAttributesName(Map<String, List<String>> paragraphes) {
		
		// Retrieve the paragraph of attribute description
		List<String> attParagraph = paragraphes.get(FILE_TYPE_HEADER);
		
		boolean headerLine = true;
		while(headerLine) {
			if(attParagraph.get(0).contains(VARIABLE_COL))
				headerLine = false;
			attParagraph.remove(0);
		}
		
		attParagraph = attParagraph.stream()
				.map(l -> new StringTokenizer(l).nextToken().trim())
				.filter(l -> !REGEX_HEADER.contains(l))
				.collect(Collectors.toList());
		
		Map<String, String> attMapName = new HashMap<>();
		for(String para : attParagraph) {
			StringTokenizer st = new StringTokenizer(paragraphes.keySet().stream()
					.filter(k -> k.contains(para)).findFirst().get(), TAB_SEPARATOR);
			attMapName.put(st.nextToken().trim(), st.nextToken().trim());
		}
		
		attributeNames = attMapName;
	}
	
	/**
	 * Parse the sample description, with country, year and sample size
	 * @param paragraph
	 */
	private void setSampleDescription(List<String> paragraph) {
		StringTokenizer st = new StringTokenizer(paragraph.get(0));
		country = st.nextToken().trim();
		year = Integer.valueOf(st.nextToken().trim());
		
		boolean global = true;
		for(int i = 1; i < paragraph.size(); i++) {
			if(paragraph.get(i).contains(DENSITY_VAR)) {
				String sampleSizeString = paragraph.get(i).split(":")[1].trim();
				sampleSizeString = sampleSizeString.substring(0, sampleSizeString.length()-1);
				if(global) {
					globalSampleSize = Double.valueOf(sampleSizeString) / 100;
					global = false;
				} else {
					sampleSize = Double.valueOf(sampleSizeString) / 100;
				}
			}
		}
		

	}
	
	// -------------------- GETTER ON IPUMS SPECIFIC DETAIL ON SAMPLE -------------------- //
	
	public String getCountry() {
		return country;
	}
	
	public int getYear() {
		return year;
	}
	
	public double getSampleSize() {
		return sampleSize;
	}
	
	public double getGlobalSampleSize() {
		return globalSampleSize;
	}
	
	/**
	 * Name of targeted attribute
	 * @return
	 */
	public Map<String, String> getAttributeNames(){
		return Collections.unmodifiableMap(attributeNames);
	}

}
