package core.configuration;

import java.io.FileNotFoundException;
import java.io.ObjectStreamException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IAttribute;
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.io.GSSurveyWrapper;

/**
 * TODO: describe the main contract for input data in genstar !!!
 * <br>
 * TODO: add configuration for localization process 
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

	private final Set<APopulationAttribute> attributeSet = new HashSet<>();

	private final Map<String, IAttribute<? extends IValue>> keyAttribute = new HashMap<>();

	public GenstarConfigurationFile(List<GSSurveyWrapper> dataFiles, 
			Set<APopulationAttribute> attributes, 
			Map<String, IAttribute<? extends IValue>> keyAttribute) {
		// TEST DATA FILE COMPATIBILITY
		for(GSSurveyWrapper wrapper : dataFiles)
			if(!wrapper.getAbsolutePath().toFile().exists()){
				try {
					wrapper.setRelativePath(Paths.get(System.getProperty("user.dir")));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		this.dataFileList.addAll(dataFiles);
		
		// TEST ATTRIBUTE SET CIRCLE REFERENCES
		try {
			this.isCircleReferencedAttribute(attributes);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.attributeSet.addAll(attributes);
		
		// TEST KEY MAP
		this.keyAttribute.putAll(keyAttribute == null ? Collections.emptyMap(): keyAttribute);
	}

	// ------------------------------------------- //
	
	/**
	 * Gives the survey wrappers
	 * @return
	 */
	public List<GSSurveyWrapper> getSurveyWrapper(){
		return dataFileList;
	}

	/**
	 * Gives the population attribute set
	 * @return
	 */
	public Set<APopulationAttribute> getAttributes(){
		return attributeSet;
	}

	/**
	 * TODO: yet to be specified
	 * @return
	 */
	private Map<String, IAttribute<? extends IValue>> getKeyAttributes() {
		return keyAttribute;
	}
	
	// --------------- UTILITIES --------------- //

	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException, FileNotFoundException {
		List<GSSurveyWrapper> dataFiles = getSurveyWrapper();
		Set<APopulationAttribute> attributes = getAttributes();
		Map<String, IAttribute<? extends IValue>> keyAttribute = getKeyAttributes();
		return new GenstarConfigurationFile(dataFiles, attributes, keyAttribute);
	}
	
	/*
	 * Throws an exception if attributes have feedback loop references, e.g. : A referees to B that referees to C
	 * that referees to A; in this case, no any attribute can be taken as a referent one 
	 */
	private void isCircleReferencedAttribute(Set<APopulationAttribute> attSet) throws IllegalArgumentException {
		// store attributes that have referent attribute
		Map<APopulationAttribute, APopulationAttribute> attToRefAtt = attSet.stream()
				.filter(att -> !att.getReferentAttribute().equals(att) && !att.isRecordAttribute())
				.collect(Collectors.toMap(att -> att, att -> att.getReferentAttribute()));
		// store attributes that are referent and which also have a referent attribute
		Map<APopulationAttribute, APopulationAttribute> opCircle = attToRefAtt.keySet()
				.stream().filter(key -> attToRefAtt.values().contains(key))
			.collect(Collectors.toMap(key -> key, key -> attToRefAtt.get(key)));
		// check if all referent attributes are also ones to refer to another attributes (circle)
		if(!opCircle.isEmpty() && opCircle.keySet().containsAll(opCircle.values()))
			throw new IllegalArgumentException("You cannot setup circular references between attributes: "
					+ opCircle.entrySet().stream().map(e -> e.getKey().getAttributeName()+" > "+e.getValue().getAttributeName())
					.reduce((s1, s2) -> s1.concat(" >> "+s2)).get());
	}

}
