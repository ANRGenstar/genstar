package core.io.survey.configuration;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.io.survey.attribut.ASurveyAttribute;

/**
 * 
 * TODO: move this class to gospl and extract interface class from this one
 * 
 * @author kevinchapuis
 *
 */
public class GosplConfigurationFile {

	private final List<GSSurveyFile> dataFileList = new ArrayList<>();
	
	private final Set<ASurveyAttribute> attributeSet = new HashSet<>();
	
	public GosplConfigurationFile(List<GSSurveyFile> dataFiles, Set<ASurveyAttribute> attributes){
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
	}
	
	public List<GSSurveyFile> getDataFiles(){
		return Collections.unmodifiableList(dataFileList);
	}
	
	public Set<ASurveyAttribute> getAttributes(){
		return Collections.unmodifiableSet(attributeSet);
	}
	
	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException {
		List<GSSurveyFile> dataFiles = getDataFiles();
		Set<ASurveyAttribute> attributes = getAttributes();
		return new GosplConfigurationFile(dataFiles, attributes);
	}
	
}
