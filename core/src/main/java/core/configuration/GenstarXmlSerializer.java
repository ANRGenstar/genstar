package core.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.metamodel.IAttribute;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.IGSSurvey;

/**
 * TODO: javadoc
 * 
 * TODO: test relative file path
 * 
 * @author kevinchapuis
 *
 */
public class GenstarXmlSerializer {

	private static final String GS_CONFIG_ALIAS = "GosplConfiguration";
	
	private static final String GS_METADATA_FILE_ALIAS = "SurveyMetaDataType";		
	private static final String GS_FILE_ALIAS = "GosplDataFile";
	private static final String GS_FILE_LIST_ALIAS = "GosplDataFiles";

	private static final String GS_ATTRIBUTE_ALIAS = "IAttribute";
	private static final String GS_ATTRIBUTE_LIST_ALIAS = "Iattributes";
	
	private XStream xs = null;
	private File mkdir = null;
	
	public GenstarXmlSerializer() throws FileNotFoundException {
		this.mkdir = new File(System.getProperty("user.dir"));
		this.xs = new XStream(new DomDriver());
		
		/*
		 * Class alias for xml record
		 */
		xs.alias(GS_CONFIG_ALIAS, GenstarConfigurationFile.class);
		xs.alias(GS_FILE_ALIAS, IGSSurvey.class);
		xs.alias(GS_METADATA_FILE_ALIAS, GSSurveyType.class);
		xs.alias(GS_ATTRIBUTE_ALIAS, IAttribute.class);
		
		/*
		 * Map Converter
		 */
		xs.registerConverter(new NamedMapConverter(xs.getMapper(), null, "Relative_path", Path.class, "Meta_data_type", GSSurveyType.class));
		
		/*
		 * field alias for xml record
		 */
		xs.aliasField(GS_FILE_LIST_ALIAS, GenstarConfigurationFile.class, "dataFiles");
		xs.aliasField(GS_ATTRIBUTE_LIST_ALIAS, GenstarConfigurationFile.class, "attributes");
		
	}
	
	public void serializeGSConfig(GenstarConfigurationFile gcf, String xmlName) throws IOException{
		Writer w = new StringWriter();
		xs.toXML(gcf, w);
		Files.write(Paths.get(mkdir+File.separator+xmlName+".xml"), w.toString().getBytes());
	}
	
	public GenstarConfigurationFile deserializeGSConfig(File valideXmlFile) throws FileNotFoundException{
		if(!valideXmlFile.exists())
			throw new FileNotFoundException(valideXmlFile.toString());
		else if(!valideXmlFile.getName().toLowerCase().endsWith(".xml"))
			throw new FileNotFoundException("The file "+valideXmlFile.getName()+" is not an xml file");  
		
		String baseDirectory = valideXmlFile.getParentFile().getAbsolutePath();

		GenstarConfigurationFile res = (GenstarConfigurationFile) xs.fromXML(valideXmlFile);
		for (IGSSurvey sf : res.getDataFiles()) {
			File f = new File(sf.getSurveyFilePath());
			if (!f.isAbsolute()) {
				sf.setSurveyFilePath(baseDirectory+File.separator+sf.getName());
			}
		}

		return res;
	}
	
	public GenstarConfigurationFile deserializeGSConfig(Path xmlFilePath) throws FileNotFoundException{
		return this.deserializeGSConfig(xmlFilePath.toFile());
	}
	
	public XStream getXStream(){
		return xs;
	}
	
	public void setMkdir(Path filePath) throws FileNotFoundException {
		this.mkdir = new File(filePath.toString());
		if(!mkdir.exists()){
			throw new FileNotFoundException("the file "+mkdir+" does not exist");
		} else if(!mkdir.isDirectory()){
			throw new FileNotFoundException("the file "+mkdir+" is not a directory");
		}
	}
	
	
}
