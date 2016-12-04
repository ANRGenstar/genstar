package core.configuration;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.io.IGSSurvey;

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
public class GenstarConfigurationFile {

	private final List<IGSSurvey> dataFileList = new ArrayList<>();

	private final Set<APopulationAttribute> attributeSet = new HashSet<>();

	private final Map<String, IAttribute<? extends IValue>> keyAttribute = new HashMap<>();

	public GenstarConfigurationFile(List<IGSSurvey> dataFiles, 
			Set<APopulationAttribute> attributes, 
			Map<String, IAttribute<? extends IValue>> keyAttribute){
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
		this.keyAttribute.putAll(keyAttribute == null ? Collections.emptyMap(): keyAttribute);
	}

	public List<IGSSurvey> getDataFiles(){
		return dataFileList;
	}

	public Set<APopulationAttribute> getAttributes(){
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
		List<IGSSurvey> dataFiles = getDataFiles();
		Set<APopulationAttribute> attributes = getAttributes();
		Map<String, IAttribute<? extends IValue>> keyAttribute = getKeyAttributes();
		return new GenstarConfigurationFile(dataFiles, attributes, keyAttribute);
	}

}
