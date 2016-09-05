package gospl.survey;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gospl.metamodel.attribut.IAttribute;
import gospl.survey.adapter.GosplDataFile;

public class GosplConfigurationFile {

	private final List<GosplDataFile> dataFileList = new ArrayList<>();
	
	private final Set<IAttribute> attributeSet = new HashSet<>();
	
	public GosplConfigurationFile(List<GosplDataFile> dataFiles, Set<IAttribute> attributes){
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
	}
	
	public List<GosplDataFile> getDataFiles(){
		return Collections.unmodifiableList(dataFileList);
	}
	
	public Set<IAttribute> getAttributes(){
		return Collections.unmodifiableSet(attributeSet);
	}
	
	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException {
		List<GosplDataFile> dataFiles = getDataFiles();
		Set<IAttribute> attributes = getAttributes();
		return new GosplConfigurationFile(dataFiles, attributes);
	}
	
}
