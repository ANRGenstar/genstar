package gospl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.AttributeFactory;
import core.io.survey.attribut.GSEnumAttributeType;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.metamodel.GSSurveyFile;
import gospl.metamodel.GSSurveyType;
import gospl.metamodel.configuration.GosplConfigurationFile;
import gospl.metamodel.configuration.GosplXmlSerializer;

public class GosplBangkokConf {
	
	public static String INDIV_CLASS_PATH = "../template/Bangkok/Bkk_indiv";
	public static String INDIV_EXPORT = "GSC_BangkokIndividual";
	public static String indiv1 = "BKK 160 NSO10 DEM-Tableau 1.csv";
	public static String indiv2 = "BKK 160 NSO10 WRK-Tableau 1.csv";
	public static String indiv3 = "BKK 160 NSO10 EDU-Tableau 1.csv";
	public static String indiv4 = "Districts-Tableau 1.csv";
	
	public static String HHOLD_CLASS_PATH = "../template/Bangkok/Bkk_menage";
	public static String HHOLD_EXPORT = "GSC_BangkokHoushold";
	public static String menage1 = "BKK 160 NSO10 DEM-Tableau 1.csv";
	public static String menage2 = "BKK 160 NSO10 HH-Tableau 1.csv";
	
	public static void main(String[] args) throws InvalidFormatException {

		// Setup the serializer that save configuration file
		GosplXmlSerializer gxs = null;
		try {
			gxs = new GosplXmlSerializer();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Setup the factory that build attribute
		AttributeFactory attf = new AttributeFactory();
		
		if(new ArrayList<>(Arrays.asList(args)).isEmpty()){
			
			// Setup input files' configuration for individual aggregated data
			List<GSSurveyFile> individualDataFiles = new ArrayList<>();
			Set<ASurveyAttribute> indivAttributes = new HashSet<>();
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv1,
					GSSurveyType.ContingencyTable, 1, 4, ';'));
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv2,
					GSSurveyType.LocalFrequencyTable, 1, 4, ';'));
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv3,
					GSSurveyType.LocalFrequencyTable, 1, 4, ';'));
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv4, 
					GSSurveyType.ContingencyTable, 1, 3, ';'));
			
			
			// Setup input files' configuration for household aggregated data
			List<GSSurveyFile> householdDataFiles = new ArrayList<>();
			Set<ASurveyAttribute> householdAttributes = new HashSet<>();
			householdDataFiles.add(new GSSurveyFile(HHOLD_CLASS_PATH+File.separator+menage1,
					GSSurveyType.ContingencyTable, 1, 4, ';'));
			householdDataFiles.add(new GSSurveyFile(HHOLD_CLASS_PATH+File.separator+menage2, 
					GSSurveyType.LocalFrequencyTable, 1, 4, ';'));
			
			try {

				// -------------------------
				// Setup "PAT" attribute: INDIVIDUAL & MENAGE
				// -------------------------
				
				ASurveyAttribute khwaeng = attf.createAttribute("PAT", GSEnumDataType.String,
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
				
				indivAttributes.add(khwaeng);
				householdAttributes.add(khwaeng);
				
				// -------------------------
				// Setup "PA" attribute: INDIVIDUAL & MENAGE
				// -------------------------
				
				// TODO: make 'khet' an aggregated attribute of 'khwaeng' 
				
				ASurveyAttribute khet = attf.createAttribute("PA", GSEnumDataType.String, 
						Arrays.asList("1001", "1002", "1003", "1004", "1005", "1006", "1007", "1008", "1009", "1010", "1011", "1012", "1013", "1014", "1015",
								"1016", "1017", "1018", "1019", "1020", "1021", "1022", "1023", "1024", "1025", "1026", "1027", "1028", "1029", "1030",
								"1031", "1032", "1033", "1034", "1035", "1036", "1037", "1038", "1039", "1040", "1041", "1042", "1043", "1044", "1045",
								"1046", "1047", "1048", "1049", "1050"), 
						GSEnumAttributeType.unique);
				
				indivAttributes.add(khet);
				householdAttributes.add(khet);
				
				// --------------------------
				// Setupe "Count" attribute: INDIVIDUAL
				// --------------------------
				
				// Instantiate a record attribute: just count the number of occurrences
				indivAttributes.add(attf.createAttribute("population", GSEnumDataType.Integer, 
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

				indivAttributes.add(attf.createAttribute("occupation", GSEnumDataType.String,
						Arrays.asList("TOCC1", "TOCC2", "TOCC3", "TOCC4", "TOCC5", "TOCC6", "TOCC7",
								"TOCC8", "TOCC9", "TTOCC"), 
						GSEnumAttributeType.unique));
				
				// -------------------------
				// Setup "AGE" attribute: INDIVIDUAL
				// -------------------------
				
				indivAttributes.add(attf.createAttribute("age", GSEnumDataType.Integer, 
						Arrays.asList("0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34", "35-39",
								"40-44", "45-49", "50-54", "55-59", "60-64", "65-69", "70-74", "75-79",
								"80-84", "85-89", "90-94", "95-99", "100+"), 
						GSEnumAttributeType.range));
				
				// --------------------------
				// Setup "SEXE" attribute: INDIVIDUAL
				// --------------------------
				
				indivAttributes.add(attf.createAttribute("gender", GSEnumDataType.String, 
						Arrays.asList("Male", "Female"), GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "Count" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute("population", GSEnumDataType.Integer, 
						Arrays.asList("HT1"), GSEnumAttributeType.record));

				
			} catch (GSIllegalRangedData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// ------------------------------
			// SERIALIZE CONFIGURATION FILES
			// ------------------------------

			try {
				gxs.setMkdir(Paths.get(INDIV_CLASS_PATH));
				GosplConfigurationFile gsdI = new GosplConfigurationFile(individualDataFiles, indivAttributes);
				gxs.serializeGSConfig(gsdI, INDIV_EXPORT);
				System.out.println("Serialize Genstar individual data with:\n"+
						gsdI.getAttributes().size()+" attributs\n"+
						gsdI.getDataFiles().size()+" data files");
				
				gxs.setMkdir(Paths.get(HHOLD_CLASS_PATH));
				GosplConfigurationFile gsdHH = new GosplConfigurationFile(householdDataFiles, householdAttributes);
				gxs.serializeGSConfig(gsdHH, HHOLD_EXPORT);
				System.out.println("Serialize Genstar household"
						+ " data with:\n"+
						gsdHH.getAttributes().size()+" attributs\n"+
						gsdHH.getDataFiles().size()+" data files");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			GosplConfigurationFile gcf = null;
			try {
				gcf = gxs.deserializeGSConfig(Paths.get(args[0].trim()));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Deserialize Genstar data configuration contains:\n"+
					gcf.getAttributes().size()+" attributs\n"+
					gcf.getDataFiles().size()+" data files");
		}
	}




}
