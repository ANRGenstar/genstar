package gospl.metamodel.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.io.survey.attribut.MappedAttribute;
import core.io.survey.attribut.RangeAttribute;
import core.io.survey.attribut.RecordAttribute;
import core.io.survey.attribut.UniqueAttribute;
import core.io.survey.attribut.value.RangeValue;
import core.io.survey.attribut.value.UniqueValue;
import core.metamodel.IAttribute;
import gospl.metamodel.GSSurveyFile;
import gospl.metamodel.GSSurveyType;

/**
 * TODO: move to a parametric and generic serializer
 * 
 * @author kevinchapuis
 *
 */
public class GosplXmlSerializer {

private static final String GS_CONFIG_ALIAS = "GosplConfiguration";
	
	private static final String GS_METADATA_FILE_ALIAS = "SurveyMetaDataType";		
	private static final String GS_FILE_ALIAS = "GosplDataFile";
	private static final String GS_FILE_LIST_ALIAS = "GosplDataFiles";

	private static final String GS_ATTRIBUTE_ALIAS = "IAttribute";
	private static final String GS_ATTRIBUTE_LIST_ALIAS = "Iattributes";
	
	
	private static final String GS_UNIQUE_ATTRIBUTE_ALIAS = "uniqueAttribute";
	private static final String GS_RANGE_ATTRIBUTE_ALIAS = "rangeAttribute";
	private static final String GS_AGGREG_ATTRIBUTE_ALIAS = "aggregatedAttribute";
	private static final String GS_RECORD_ATTRIBUTE_ALIAS = "recordAttribute";

	private static final String GS_UNIQUE_VALUE_ALIAS = "uniqueValue";
	private static final String GS_RANGE_VALUE_ALIAS = "rangeValue";
	
	private XStream xs = null;
	private File mkdir = null;
	
	public GosplXmlSerializer() throws FileNotFoundException {
		this.mkdir = new File(System.getProperty("user.dir"));
		this.xs = new XStream(new DomDriver());
		
		/*
		 * Class alias for xml record
		 */
		xs.alias(GS_CONFIG_ALIAS, GosplConfigurationFile.class);
		xs.alias(GS_FILE_ALIAS, GSSurveyFile.class);
		xs.alias(GS_METADATA_FILE_ALIAS, GSSurveyType.class);
		xs.alias(GS_ATTRIBUTE_ALIAS, IAttribute.class);
		
		
		xs.alias(GS_UNIQUE_ATTRIBUTE_ALIAS, UniqueAttribute.class);
		xs.alias(GS_UNIQUE_VALUE_ALIAS, UniqueValue.class);
		xs.alias(GS_RANGE_ATTRIBUTE_ALIAS, RangeAttribute.class);
		xs.alias(GS_RANGE_VALUE_ALIAS, RangeValue.class);
		xs.alias(GS_AGGREG_ATTRIBUTE_ALIAS, MappedAttribute.class);
		xs.alias(GS_RECORD_ATTRIBUTE_ALIAS, RecordAttribute.class);
		
		/*
		 * Map Converter
		 */
		xs.registerConverter(new NamedMapConverter(xs.getMapper(), null, "Relative_path", Path.class, "Meta_data_type", GSSurveyType.class));
		
		/*
		 * field alias for xml record
		 */
		xs.aliasField(GS_FILE_LIST_ALIAS, GosplConfigurationFile.class, "dataFiles");
		xs.aliasField(GS_ATTRIBUTE_LIST_ALIAS, GosplConfigurationFile.class, "attributes");
		
	}
	
	public void serializeGSConfig(GosplConfigurationFile gcf, String xmlName) throws IOException{
		Writer w = new StringWriter();
		xs.toXML(gcf, w);
		Files.write(Paths.get(mkdir+File.separator+xmlName+".xml"), w.toString().getBytes());
	}
	
	public GosplConfigurationFile deserializeGSConfig(File valideXmlFile) throws FileNotFoundException {
		
		if(!valideXmlFile.exists())
			throw new FileNotFoundException(valideXmlFile.toString());
		else if(!valideXmlFile.getName().toLowerCase().endsWith(".xml"))
			throw new FileNotFoundException("The file "+valideXmlFile.getName()+" is not an xml file");  
		
		String baseDirectory = valideXmlFile.getParentFile().getAbsolutePath();

		GosplConfigurationFile res = (GosplConfigurationFile) xs.fromXML(valideXmlFile);
		for (GSSurveyFile sf : res.getDataFiles()) {
			File f = new File(sf.getSurveyFilePath());
			if (!f.isAbsolute()) {
				sf._setSurveyFilePath(baseDirectory+File.separator+sf.getSurveyFileName());
			}
		}

		return res;
	}
	
	public GosplConfigurationFile deserializeGSConfig(Path xmlFilePath) throws FileNotFoundException{
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
