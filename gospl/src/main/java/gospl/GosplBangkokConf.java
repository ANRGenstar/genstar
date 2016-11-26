package gospl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.AttributeFactory;
import core.io.survey.attribut.GSEnumAttributeType;
import core.io.survey.configuration.GSSurveyFile;
import core.io.survey.configuration.GSSurveyType;
import core.io.survey.configuration.GosplConfigurationFile;
import core.io.survey.configuration.GosplXmlSerializer;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class GosplBangkokConf {
	
	public static String INDIV_CLASS_PATH = "../genstar/template/Bangkok/Bkk_indiv";
	public static String INDIV_EXPORT = "GSC_BangkokIndividual";
	public static String indiv1 = "BKK 160 NSO10 DEM-Tableau 1";
	public static String indiv2 = "BKK 160 NSO10 WRK-Tableau 1.csv";
	public static String indiv3 = "BKK 160 NSO10 EDU-Tableau 1.csv";
	public static String indiv4 = "Districts-Tableau 1.csv";
	
	public static String HHOLD_CLASS_PATH = "../genstar/template/Bangkok/Bkk_menage";
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
			
			String[] bkk_indiv_att = new String[]{"POP", "EDU", "WRK", "AGE", "SEXE"};
			String[] bkk_menage_att = new String[]{"HT1"};
			
			// Setup input files' configuration for individual aggregated data
			List<GSSurveyFile> individualDataFiles = new ArrayList<>();
			Set<ASurveyAttribute> indivAttributes = new HashSet<>();
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv1,
					GSSurveyType.ContingencyTable, 1, 1, ';'));
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv2,
					GSSurveyType.ContingencyTable, 2, 1, ';'));
			individualDataFiles.add(new GSSurveyFile(INDIV_CLASS_PATH+File.separator+indiv3,
					GSSurveyType.ContingencyTable, 1, 1, ';'));
			
			// Setup input files' configuration for household aggregated data
			List<GSSurveyFile> householdDataFiles = new ArrayList<>();
			Set<ASurveyAttribute> householdAttributes = new HashSet<>();
			householdDataFiles.add(new GSSurveyFile(HHOLD_CLASS_PATH+File.separator+menage1,
					GSSurveyType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GSSurveyFile(HHOLD_CLASS_PATH+File.separator+menage2, 
					GSSurveyType.ContingencyTable, 1, 1, ';'));
			
			try {
				
				// --------------------------
				// Setupe "Count" attribute: INDIVIDUAL
				// --------------------------
				
				// Instantiate a record attribute: just count the number of occurrences
				indivAttributes.add(attf.createAttribute(bkk_indiv_att[0], GSEnumDataType.Integer, 
						Collections.emptyList(), GSEnumAttributeType.record));		
				
				// --------------------------
				// Setup "EDU" attribute: INDIVIDUAL
				// --------------------------

				indivAttributes.add(attf.createAttribute(bkk_indiv_att[1], GSEnumDataType.String, 
						Arrays.asList("GC00", "GC01", "GC02", "GC03", "GC04", "GC05", "GC06"), 
						GSEnumAttributeType.unique));
				
				// -------------------------
				// Setup "WRK" attribute: INDIVIDUAL
				// -------------------------

				indivAttributes.add(attf.createAttribute(bkk_indiv_att[2], GSEnumDataType.String,
						Arrays.asList("TOCC1", "TOCC2", "TOCC3", "TOCC4", "TOCC5", "TOCC6", "TOCC7",
								"TOCC8", "TOCC9", "TTOCC"), 
						GSEnumAttributeType.unique));
				
				// -------------------------
				// Setup "AGE" attribute: INDIVIDUAL
				// -------------------------
				
				indivAttributes.add(attf.createAttribute(bkk_indiv_att[3], GSEnumDataType.Integer, 
						Arrays.asList("0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34", "35-39",
								"40-44", "45-49", "50-54", "55-59", "60-64", "65-69", "70-74", "75-79",
								"80-84", "85-89", "90-94", "95-99", "100+"), 
						GSEnumAttributeType.range));
				
				// --------------------------
				// Setup "SEXE" attribute: INDIVIDUAL
				// --------------------------
				
				householdAttributes.add(attf.createAttribute(bkk_indiv_att[4], GSEnumDataType.String, 
						Arrays.asList("Male", "Female"), GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "Count" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute(bkk_menage_att[0], GSEnumDataType.Integer, 
						Collections.emptyList(), GSEnumAttributeType.record));

				
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
