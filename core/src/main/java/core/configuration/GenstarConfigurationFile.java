package core.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.value.IValue;

/**
 * Data configuration consist in a base directory where to find ressources, plus
 * a list of wrapped file that encapsulate data file plus information to read it, and
 * finally dictionary to understand data in file
 * <p><ul>
 * <li> list of survey files
 * <li> list of survey attribute
 * <li> list of key attribute (link between survey and spatial attribute)
 * </ul><p>
 * Configuration file can be saved using Json based file with {@link GenstarJsonUtil}
 * <p>
 * TODO: add configuration for localization process
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeName(value = GenstarConfigurationFile.SELF)
public class GenstarConfigurationFile {

	public final static String SELF = "CONFIGURATION FILE";
	
	private final List<GSSurveyWrapper> dataFileList = new ArrayList<>();

	// Demographic attributes
	private DemographicDictionary<Attribute<? extends IValue>> demoDictionary;

	/**
	 * The path in which the files included in this configuration is stored, if known.
	 */
	protected Path baseDirectory = null; 
	
	/**
	 * Default constructor
	 */
	public GenstarConfigurationFile() {}

	// ------------------------------------------- //
	
	/**
	 * Gives the survey wrappers
	 * @return
	 */
	@JsonProperty(GenstarJsonUtil.INPUT_FILES)
	public List<GSSurveyWrapper> getSurveyWrappers(){
		return dataFileList;
	}
	
	@JsonProperty(GenstarJsonUtil.INPUT_FILES)
	public void setSurveyWrappers(List<GSSurveyWrapper> surveys) {
		this.dataFileList.addAll(surveys);
	}

	public void addSurveyWrapper(GSSurveyWrapper survey) {
		this.dataFileList.add(survey);
	}
	
	/**
	 * Gives the dictionary of attribute
	 * @return
	 */
	@JsonProperty(GenstarJsonUtil.DEMO_DICO)
	public DemographicDictionary<Attribute<? extends IValue>> getDemoDictionary(){
		return demoDictionary;
	}
	
	@JsonProperty(GenstarJsonUtil.DEMO_DICO)
	public void setDemoDictionary(DemographicDictionary<Attribute<? extends IValue>> dictionary) {
		this.demoDictionary = dictionary;
		this.isCircleReferencedAttribute();
	}
	
	/**
	 * The root directory from when to resolve relative path
	 * @return
	 */
	@JsonProperty(GenstarJsonUtil.BASE_DIR)
	public Path getBaseDirectory() {
		return this.baseDirectory;
	}
	
	@JsonProperty(GenstarJsonUtil.BASE_DIR)
	public void setBaseDirectory(Path f) {
		LogManager.getLogger().info("Setting Genstar configuration basepath to "+f);
		this.baseDirectory = f;
	}
	
	// --------------- UTILITIES --------------- //

	/*
	 * Throws an exception if attributes have feedback loop references, e.g. : A referees to B that referees to C
	 * that referees to A; in this case, no any attribute can be taken as a referent one 
	 */
	private void isCircleReferencedAttribute() throws IllegalArgumentException {
		Collection<Attribute<? extends IValue>> attributes = new HashSet<>();
		if(demoDictionary != null) attributes.addAll(demoDictionary.getAttributes());
		// store attributes that have referent attribute
		Map<Attribute<? extends IValue>, Attribute<? extends IValue>> attToRefAtt = 
				attributes.stream().filter(att -> !att.getReferentAttribute().equals(att))
				.collect(Collectors.toMap(att -> att, att -> att.getReferentAttribute()));
		// store attributes that are referent and which also have a referent attribute
		Map<Attribute<? extends IValue>, Attribute<? extends IValue>> opCircle = attToRefAtt.keySet()
				.stream().filter(key -> attToRefAtt.values().contains(key))
			.collect(Collectors.toMap(key -> key, key -> attToRefAtt.get(key)));
		// check if all referent attributes are also ones to refer to another attributes (circle)
		if(!opCircle.isEmpty() && opCircle.keySet().containsAll(opCircle.values()))
			throw new IllegalArgumentException("You cannot setup circular references between attributes: "
					+ opCircle.entrySet().stream().map(e -> e.getKey().getAttributeName()+" > "+e.getValue().getAttributeName())
					.reduce((s1, s2) -> s1.concat(" >> "+s2)).get());
	}

}
