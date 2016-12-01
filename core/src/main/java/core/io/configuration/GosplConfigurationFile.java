package core.io.configuration;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.io.survey.GSSurveyFile;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.metamodel.IAttribute;
import core.metamodel.IValue;

/**
 * 
 * TODO: describe the main contract for input data in genstar !!!
 * <br>
 * TODO: describe
 * <p><ul>
 * <li> list of survey and spatial files
 * <li> list of survey attribute
 * <li> list of key attribute (link between survey and spatial attribute)
 * </ul><p>
 * 
 * @author kevinchapuis
 *
 */
public class GosplConfigurationFile {

	private final List<GSSurveyFile> dataFileList = new ArrayList<>();

	private final Set<AGenstarAttribute> attributeSet = new HashSet<>();

	private final Map<String, IAttribute<? extends IValue>> keyAttribute = new HashMap<>();

	public GosplConfigurationFile(List<GSSurveyFile> dataFiles, 
			Set<AGenstarAttribute> attributes, Map<String, IAttribute<? extends IValue>> keyAttribute){
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
		this.keyAttribute.putAll(keyAttribute);
	}

	public List<GSSurveyFile> getDataFiles(){
		return dataFileList;
	}

	public Set<AGenstarAttribute> getAttributes(){
		return attributeSet;
	}

	private Map<String, IAttribute<? extends IValue>> getKeyAttributes() {
		return keyAttribute;
	}

	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException {
		List<GSSurveyFile> dataFiles = getDataFiles();
		Set<AGenstarAttribute> attributes = getAttributes();
		Map<String, IAttribute<? extends IValue>> keyAttribute = getKeyAttributes();
		return new GosplConfigurationFile(dataFiles, attributes, keyAttribute);
	}

}
