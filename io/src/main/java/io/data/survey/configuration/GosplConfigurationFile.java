package io.data.survey.configuration;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.metamodel.attribut.IAttribute;

public class GosplConfigurationFile {

	private final List<GSSurveyFile> dataFileList = new ArrayList<>();
	
	private final Set<IAttribute> attributeSet = new HashSet<>();
	
	public GosplConfigurationFile(List<GSSurveyFile> dataFiles, Set<IAttribute> attributes){
		this.dataFileList.addAll(dataFiles);
		this.attributeSet.addAll(attributes);
	}
	
	public List<GSSurveyFile> getDataFiles(){
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
		List<GSSurveyFile> dataFiles = getDataFiles();
		Set<IAttribute> attributes = getAttributes();
		return new GosplConfigurationFile(dataFiles, attributes);
	}
	
}
