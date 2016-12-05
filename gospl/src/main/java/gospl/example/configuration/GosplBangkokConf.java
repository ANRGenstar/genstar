package gospl.example.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarXmlSerializer;
import core.metamodel.IAttribute;
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.entity.attribute.AttributeFactory;
import gospl.entity.attribute.GSEnumAttributeType;

public class GosplBangkokConf {

	public static String CONF_CLASS_PATH = "../template/Bangkok/";
	public static String CONF_EXPORT = "GSC_Bangkok";

	public static void main(String[] args) throws InvalidFormatException {

		// Setup the serializer that save configuration file
		GenstarXmlSerializer gxs = null;
		try {
			gxs = new GenstarXmlSerializer();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Setup the factory that build attribute
		AttributeFactory attf = new AttributeFactory();

		// What to define in this configuration file
		List<GSSurveyWrapper> inputFiles = new ArrayList<>();
		Set<APopulationAttribute> inputAttributes = new HashSet<>();
		Map<String, IAttribute<? extends IValue>> inputKeyMap = new HashMap<>();

		// Make things a bit more abstract
		Path absolutePath = Paths.get(CONF_CLASS_PATH).toAbsolutePath();

		if(new ArrayList<>(Arrays.asList(args)).isEmpty()){

			// Setup input files' configuration for individual aggregated data
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_indiv/BKK 160 NSO10 DEM-Tableau 1.csv").toString(), 
					GSSurveyType.ContingencyTable, ';', 1, 4));
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_indiv/BKK 160 NSO10 WRK-Tableau 1.csv").toString(), 
					GSSurveyType.LocalFrequencyTable, ';', 1, 4));
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_indiv/BKK 160 NSO10 EDU-Tableau 1.csv").toString(), 
					GSSurveyType.LocalFrequencyTable, ';', 1, 4));
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_indiv/Districts-Tableau 1.csv").toString(), 
					GSSurveyType.ContingencyTable, ';', 1, 3));
			// Setup input files' configuration for household aggregated data
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_menage/BKK 160 NSO10 DEM-Tableau 1.csv").toString(), 
					GSSurveyType.ContingencyTable, ';', 1, 4));
			inputFiles.add(new GSSurveyWrapper(absolutePath.resolve("Bkk_menage/BKK 160 NSO10 HH-Tableau 1.csv").toString(), 
					GSSurveyType.LocalFrequencyTable, ';', 1, 4));


			try {
				// -------------------------
				// Setup "PAT" attribute: INDIVIDUAL & MENAGE
				// -------------------------

				APopulationAttribute khwaeng = attf.createAttribute("PAT", GSEnumDataType.String,
						Arrays.asList("100101", "100102", "100103", "100104", "100105", "100106", "100107", "100108", "100109", "100110", "100111", "100112",
								"100201", "100202", "100203", "100204", "100206", "100301", "100302", "100303", "100304", "100305", "100306", "100307", 
								"100308", "100401", "100402", "100403", "100404", "100405", "100502", "100508", "100601", "100608", "100701", "100702",
								"100703", "100704", "100801", "100802", "100803", "100804", "100805", "100905", "101001", "101002", "101101", "101102",
								"101103", "101104", "101105", "101106", "101203", "101204", "101301", "101302", "101303", "101401", "101501", "101502",
								"101503", "101504", "101505", "101506", "101507", "101601", "101602", "101701", "101702", "101704", "101801", "101802",
								"101803", "101804", "101901", "101902", "101903", "101904", "101905", "101907", "102004", "102005", "102006", "102007",
								"102009", "102105", "102107", "102201", "102202", "102206", "102207", "102208", "102209", "102210", "102302", "102303",
								"102401", "102402", "102501", "102502", "102503", "102504", "102601", "102701", "102801", "102802", "102803", "102901",
								"103001", "103002", "103003", "103004", "103005", "103101", "103102", "103103", "103201", "103202", "103203", "103301",
								"103302", "103303", "103401", "103501", "103502", "103503", "103504", "103602", "103701", "103702", "103703", "103704",
								"103801", "103802", "103901", "103902", "103903", "104001", "104002", "104003", "104004", "104101", "104102", "104201",
								"104202", "104203", "104301", "104401", "104501", "104601", "104602", "104603", "104604", "104605", "104701", "104801",
								"104802", "104901", "104902", "105001"),
						GSEnumAttributeType.unique); 

				inputAttributes.add(khwaeng);

				// -------------------------
				// Setup "PA" attribute: INDIVIDUAL & MENAGE
				// -------------------------

				// TODO: make 'khet' an aggregated attribute of 'khwaeng' 

				APopulationAttribute khet = attf.createAttribute("PA", GSEnumDataType.String, 
						Arrays.asList("1001", "1002", "1003", "1004", "1005", "1006", "1007", "1008", "1009", "1010", "1011", "1012", "1013", "1014", "1015",
								"1016", "1017", "1018", "1019", "1020", "1021", "1022", "1023", "1024", "1025", "1026", "1027", "1028", "1029", "1030",
								"1031", "1032", "1033", "1034", "1035", "1036", "1037", "1038", "1039", "1040", "1041", "1042", "1043", "1044", "1045",
								"1046", "1047", "1048", "1049", "1050"), 
						GSEnumAttributeType.unique);

				inputAttributes.add(khet);

				// --------------------------
				// Setupe "Count" attribute: INDIVIDUAL
				// --------------------------

				// Instantiate a record attribute: just count the number of occurrences
				inputAttributes.add(attf.createAttribute("population", GSEnumDataType.Integer, 
						Arrays.asList("POP"), GSEnumAttributeType.record));		

				// --------------------------
				// Setup "EDU" attribute: INDIVIDUAL
				// --------------------------

				//				indivAttributes.add(attf.createAttribute("education", GSEnumDataType.String, 
				//						Arrays.asList("GC00", "GC01", "GC02", "GC03", "GC04", "GC05", "GC06"), 
				//						GSEnumAttributeType.unique));

				// -------------------------
				// Setup "WRK" attribute: INDIVIDUAL
				// -------------------------

				inputAttributes.add(attf.createAttribute("occupation", GSEnumDataType.String,
						Arrays.asList("TOCC1", "TOCC2", "TOCC3", "TOCC4", "TOCC5", "TOCC6", "TOCC7",
								"TOCC8", "TOCC9", "TTOCC"), 
						GSEnumAttributeType.unique));

				// -------------------------
				// Setup "AGE" attribute: INDIVIDUAL
				// -------------------------

				inputAttributes.add(attf.createAttribute("age", GSEnumDataType.Integer, 
						Arrays.asList("0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34", "35-39",
								"40-44", "45-49", "50-54", "55-59", "60-64", "65-69", "70-74", "75-79",
								"80-84", "85-89", "90-94", "95-99", "100+"), 
						GSEnumAttributeType.range));

				// --------------------------
				// Setup "SEXE" attribute: INDIVIDUAL
				// --------------------------

				inputAttributes.add(attf.createAttribute("gender", GSEnumDataType.String, 
						Arrays.asList("Male", "Female"), GSEnumAttributeType.unique));

				// --------------------------
				// Setup "Count" attribute: MENAGE
				// --------------------------

				inputAttributes.add(attf.createAttribute("population", GSEnumDataType.Integer, 
						Arrays.asList("HT1"), GSEnumAttributeType.record));


			} catch (GSIllegalRangedData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ------------------------------
			// SERIALIZE CONFIGURATION FILES
			// ------------------------------

			try {
				gxs.setMkdir(Paths.get(CONF_CLASS_PATH));
				GenstarConfigurationFile gsdI = new GenstarConfigurationFile(inputFiles, 
						inputAttributes, inputKeyMap);
				gxs.serializeGSConfig(gsdI, CONF_EXPORT);
				System.out.println("Serialize Genstar configuration data with:\n"+
						gsdI.getAttributes().size()+" attributs\n"+
						gsdI.getSurveyWrapper().size()+" data files");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			GenstarConfigurationFile gcf = null;
			try {
				gcf = gxs.deserializeGSConfig(Paths.get(args[0].trim()));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Deserialize Genstar data configuration contains:\n"+
					gcf.getAttributes().size()+" attributs\n"+
					gcf.getSurveyWrapper().size()+" data files");
		}
	}




}
