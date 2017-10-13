package core.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectStreamException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.pop.DemographicAttribute;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.metamodel.value.IValue;

/**
 * TODO: describe the main contract for input data in genstar !!!
 * <br>
 * TODO: add configuration for localization process
 * <br>
 * TODO: move to a proper .xml translation which IS NOT based on object serialization
 * <p><ul>
 * <li> list of survey files
 * <li> list of survey attribute
 * <li> list of key attribute (link between survey and spatial attribute)
 * </ul><p>
 * 
 * @author kevinchapuis
 *
 */
public class GenstarConfigurationFile {

	private final List<GSSurveyWrapper> dataFileList = new ArrayList<>();

	private final DemographicDictionary demoDictionary;

	/**
	 * The path in which the files included in this configuration is stored, if known.
	 */
	@XStreamOmitField
	protected File baseDirectory = null; 
	
	public GenstarConfigurationFile(List<GSSurveyWrapper> dataFiles, DemographicDictionary demoDictionary) {
		// TEST DATA FILE COMPATIBILITY
		for(GSSurveyWrapper wrapper : dataFiles)
			if(!wrapper.getAbsolutePath().toFile().exists()){
				try {
					wrapper.setRelativePath(Paths.get(System.getProperty("user.dir")));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		this.dataFileList.addAll(dataFiles);
		
		try {
			this.isCircleReferencedAttribute(demoDictionary.getAttributes());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.demoDictionary = demoDictionary;
	}

	// ------------------------------------------- //
	
	/**
	 * Gives the survey wrappers
	 * @return
	 */
	public List<GSSurveyWrapper> getSurveyWrappers(){
		return dataFileList;
	}

	/**
	 * Gives the population attribute set
	 * @return
	 */
	public DemographicDictionary getDemoDictionary(){
		return demoDictionary;
	}
	
	// --------------- UTILITIES --------------- //
	
	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException, FileNotFoundException {
		List<GSSurveyWrapper> dataFiles = getSurveyWrappers();
		DemographicDictionary demoDico = getDemoDictionary();
		return new GenstarConfigurationFile(dataFiles, demoDico);
	}

	/*
	 * Throws an exception if attributes have feedback loop references, e.g. : A referees to B that referees to C
	 * that referees to A; in this case, no any attribute can be taken as a referent one 
	 */
	private void isCircleReferencedAttribute(Set<DemographicAttribute<? extends IValue>> attSet) throws IllegalArgumentException {
		// store attributes that have referent attribute
		Map<DemographicAttribute<? extends IValue>, DemographicAttribute<? extends IValue>> attToRefAtt = attSet.stream()
				.filter(att -> !att.getReferentAttribute().equals(att))
				.collect(Collectors.toMap(att -> att, att -> att.getReferentAttribute()));
		// store attributes that are referent and which also have a referent attribute
		Map<DemographicAttribute<? extends IValue>, DemographicAttribute<? extends IValue>> opCircle = attToRefAtt.keySet()
				.stream().filter(key -> attToRefAtt.values().contains(key))
			.collect(Collectors.toMap(key -> key, key -> attToRefAtt.get(key)));
		// check if all referent attributes are also ones to refer to another attributes (circle)
		if(!opCircle.isEmpty() && opCircle.keySet().containsAll(opCircle.values()))
			throw new IllegalArgumentException("You cannot setup circular references between attributes: "
					+ opCircle.entrySet().stream().map(e -> e.getKey().getAttributeName()+" > "+e.getValue().getAttributeName())
					.reduce((s1, s2) -> s1.concat(" >> "+s2)).get());
	}

	/**
	 * facilitates the creation of lisible mappings.
	 * 
	 * Example: Map<Set<String>, Set<String>> mapper = new HashMap<>();
	 * addMapper(mapper, Arrays.asList("moins de 15"),  Arrays.asList("0-5", "6-15"));
	 * addMapper(mapper, Arrays.asList("16-25"), Arrays.asList("16-25"));
	 * addMapper(mapper, Arrays.asList("26-55"), Arrays.asList("26-40","40-55"));
	 * addMapper(mapper, Arrays.asList("55 et plus"), Arrays.asList("55 et plus"));
	 *	
	 * @param mapper
	 * @param from
	 * @param to
	 */
	public static void addMapper(
			Map<Set<String>, Set<String>> mapper, 
			List<String> from, List<String> to) {
		
		mapper.put(new HashSet<>(from), new HashSet<>(to));
		
	}
	
	public void setBaseDirectory(File f) {
		this.baseDirectory = f.getParentFile();
	}
	
	public File getBaseDirectory() {
		return this.baseDirectory;
	}

}
