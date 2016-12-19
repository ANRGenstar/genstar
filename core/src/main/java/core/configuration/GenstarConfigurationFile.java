package core.configuration;

import java.io.FileNotFoundException;
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
			Map<String, IAttribute<? extends IValue>> keyAttribute) throws FileNotFoundException{
		for(GSSurveyWrapper wrapper : dataFiles)
			if(!wrapper.getAbsolutePath().toFile().exists())
				throw new FileNotFoundException("Absolute path "+wrapper.getAbsoluteStringPath()+" does not denote any file");
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
		this.keyAttribute.putAll(keyAttribute == null ? Collections.emptyMap(): keyAttribute);
	}

	public List<GSSurveyWrapper> getSurveyWrapper(){
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
	protected Object readResolve() throws ObjectStreamException, FileNotFoundException {
		List<GSSurveyWrapper> dataFiles = getSurveyWrapper();
		Set<APopulationAttribute> attributes = getAttributes();
		Map<String, IAttribute<? extends IValue>> keyAttribute = getKeyAttributes();
		return new GenstarConfigurationFile(dataFiles, attributes, keyAttribute);
	}

}
