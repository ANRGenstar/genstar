package gospl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.configuration.GosplConfigurationFile;
import core.io.configuration.GosplXmlSerializer;
import core.io.survey.GSSurveyFile;
import core.io.survey.GSSurveyType;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.AttributeFactory;
import core.io.survey.entity.attribut.GSEnumAttributeType;
import core.metamodel.IAttribute;
import core.metamodel.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class GosplRouenConf {

	public static String CONF_CLASS_PATH = "../spll/sample/Rouen/Rouen_insee_indiv/";
	public static String CONF_EXPORT = "GSC_Rouen";
	
	public static String indiv1 = "AgeCouple-Tableau 1.csv";
	public static String indiv2 = "AgeSexeCSP-Tableau 1.csv";
	public static String indiv3 = "AgeSexe-Tableau 1.csv";
	public static String indiv4 = "Rouen_iris.csv";
//	public static String menage1 = "Ménage & Enfants-Tableau 1.csv";
//	public static String menage2 = "Taille ménage & CSP référent-Tableau 1.csv";
//	public static String menage3 = "Taille ménage & Sex & Age-Tableau 1.csv";
//	public static String sample1 = "Rouen_sample_IRIS.csv";

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
			Set<AGenstarAttribute> indivAttributes = new HashSet<>();
			individualDataFiles.add(new GSSurveyFile(indiv1, GSSurveyType.ContingencyTable, 1, 1, ';'));
			individualDataFiles.add(new GSSurveyFile(indiv2, GSSurveyType.ContingencyTable, 2, 1, ';'));
			individualDataFiles.add(new GSSurveyFile(indiv3, GSSurveyType.ContingencyTable, 1, 1, ';'));
			individualDataFiles.add(new GSSurveyFile(indiv4, GSSurveyType.ContingencyTable, 1, 1, ','));
			
			/*
			// Setup input files' configuration for household aggregated data
			List<GSSurveyFile> householdDataFiles = new ArrayList<>();
			Set<AGenstarAttribute> householdAttributes = new HashSet<>();
			householdDataFiles.add(new GSSurveyFile(menage1, GSSurveyType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GSSurveyFile(menage2, GSSurveyType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GSSurveyFile(menage3, GSSurveyType.ContingencyTable, 2, 1, ';'));

			
			// Setup input files' configuration for sample data
			List<GSSurveyFile> sampleDataFiles = new ArrayList<>();
			Set<AGenstarAttribute> sampleAttributes = new HashSet<>();
			sampleDataFiles.add(new GSSurveyFile(sample1,GSSurveyType.Sample, 1, 0));
			*/
			
			try {
				
				// --------------------------
				// Setupe "AGE" attribute: INDIVIDUAL
				// --------------------------
				
				// Instantiate a referent attribute
				AGenstarAttribute referentAgeAttribute = attf.createAttribute("Age", GSEnumDataType.Integer, 
						Arrays.asList("Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
								"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"), GSEnumAttributeType.range);
				indivAttributes.add(referentAgeAttribute);
				
				
				// Create a mapper
				Map<Set<String>, Set<String>> mapperA1 = new HashMap<>();
				mapperA1.put(Stream.of("15 à 19 ans").collect(Collectors.toSet()), 
						Stream.of("15 à 19 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("20 à 24 ans").collect(Collectors.toSet()), 
						Stream.of("20 à 24 ans").collect(Collectors.toSet())); 
				mapperA1.put(Stream.of("25 à 39 ans").collect(Collectors.toSet()), 
						Stream.of("25 à 29 ans", "30 à 34 ans", "35 à 39 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("40 à 54 ans").collect(Collectors.toSet()), 
						Stream.of("40 à 44 ans", "45 à 49 ans", "50 à 54 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("55 à 64 ans").collect(Collectors.toSet()),
						Stream.of("55 à 59 ans", "60 à 64 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("65 à 79 ans").collect(Collectors.toSet()), 
						Stream.of("65 à 69 ans", "70 à 74 ans", "75 à 79 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("80 ans ou plus").collect(Collectors.toCollection(HashSet::new)),
						Stream.of("80 à 84 ans", "85 à 89 ans", "90 à 94 ans", 
						"95 à 99 ans", "100 ans ou plus").collect(Collectors.toCollection(HashSet::new)));
				// Instantiate an aggregated attribute using previously referent attribute
				indivAttributes.add(attf.createAttribute("Age_2", GSEnumDataType.Integer,
						mapperA1.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GSEnumAttributeType.range, referentAgeAttribute, mapperA1));
				
				// Create another mapper
				Map<Set<String>, Set<String>> mapperA2 = new HashMap<>();
				mapperA2.put(Stream.of("15 à 19 ans").collect(Collectors.toSet()), 
						Stream.of("15 à 19 ans").collect(Collectors.toSet()));
				mapperA2.put(Stream.of("20 à 24 ans").collect(Collectors.toSet()), 
						Stream.of("20 à 24 ans").collect(Collectors.toSet()));
				mapperA2.put(Stream.of("25 à 39 ans").collect(Collectors.toSet()), 
						Stream.of("25 à 29 ans", "30 à 34 ans", "35 à 39 ans").collect(Collectors.toSet()));
				mapperA2.put(Stream.of("40 à 54 ans").collect(Collectors.toSet()), 
						Stream.of("40 à 44 ans", "45 à 49 ans", "50 à 54 ans").collect(Collectors.toSet()));
				mapperA2.put(Stream.of("55 à 64 ans").collect(Collectors.toSet()),
						Stream.of("55 à 59 ans", "60 à 64 ans").collect(Collectors.toSet()));
				mapperA2.put(Stream.of("65 ans ou plus").collect(Collectors.toSet()), 
						Stream.of("65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
						"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus").collect(Collectors.toSet()));
				indivAttributes.add(attf.createAttribute("Age_3", GSEnumDataType.Integer,
						mapperA2.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GSEnumAttributeType.range, referentAgeAttribute, mapperA2));		
				
				/*
				// Create another "age" attribute with diverging data and model modalities
				sampleAttributes.add(attf.createAttribute("Age_y", GSEnumDataType.Integer, 	
						Arrays.asList("000", "005", "010", "015", "020", "025", "030",
								"035", "040", "045", "050", "055", "060", "065", "070", "075", "080", "085",
								"090", "095", "100", "105", "110", "115", "120"), 
						Arrays.asList("0 à 4 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", 
								"20 à 24 ans", "25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 à 104 ans", "105 à 109 ans", 
								"110 à 114 ans", "115 à 129 ans", "120 ans"), 
						GSEnumAttributeType.range));
						*/
				
				// --------------------------
				// Setup "COUPLE" attribute: INDIVIDUAL
				// --------------------------
				
				indivAttributes.add(attf.createAttribute("Couple", GSEnumDataType.String, 
						Arrays.asList("Vivant en couple", "Ne vivant pas en couple"), 
						GSEnumAttributeType.unique));
				
				/*
				sampleAttributes.add(attf.createAttribute("Couple", GSEnumDataType.String, 
						Arrays.asList("1", "2"),
						Arrays.asList("Vivant en couple", "Ne vivant pas en couple"), 
						GSEnumAttributeType.unique));
						*/
				
				// --------------------------
				// Setup "IRIS" attribute: INDIVIDUAL
				// --------------------------
				
				indivAttributes.add(attf.createAttribute("IRIS", GSEnumDataType.String, 
						Arrays.asList("765400602", "765400104","765400306","765400201",
								"765400601","765400901","765400302","765400604","765400304",
								"765400305","765400801","765400301","765401004","765401003",
								"765400402","765400603","765400303","765400103","765400504",
								"765401006","765400702","765400401","765400202","765400802",
								"765400502","765400106","765400701","765401005","765400204",
								"765401001","765400405","765400501","765400102","765400503",
								"765400404","765400105","765401002","765400902","765400403",
								"765400203","765400101","765400205"), 
						GSEnumAttributeType.unique));
				
				indivAttributes.add(attf.createAttribute("POP", GSEnumDataType.Integer,
						Arrays.asList("P13_POP"), GSEnumAttributeType.record));
				
				// -------------------------
				// Setup "SEXE" attribute: INDIVIDUAL
				// -------------------------
				
				indivAttributes.add(attf.createAttribute("Sexe", GSEnumDataType.String,
						Arrays.asList("Hommes", "Femmes"), GSEnumAttributeType.unique));
				
				/*
				sampleAttributes.add(attf.createAttribute("Sexe", GSEnumDataType.String,
						Arrays.asList("1", "2"),
						Arrays.asList("Hommes", "Femmes"), GSEnumAttributeType.unique));
						*/
				
				// -------------------------
				// Setup "CSP" attribute: INDIVIDUAL
				// -------------------------
				
				indivAttributes.add(attf.createAttribute("CSP", GSEnumDataType.String, 
						Arrays.asList("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"), 
						GSEnumAttributeType.unique));
				
				/*
				Map<Set<String>, Set<String>> scp_mapper = new HashMap<>();
				List<String> scpInput = Arrays.asList("11", "12", "21", "22", "23", "24", "25");
				List<String> scpModel = Arrays.asList("Actifs ayant un emploi, y compris sous apprentissage ou en stage rémunéré", 
						"Chômeurs", "Retraités ou préretraités", "Elèves, étudiants, stagiaires non rémunéré de 14 ans ou plus", 
						"Moins de 14 ans", "Femmes ou hommes au foyer", "Autres inactifs");
				scp_mapper.put(Stream.of("Retraités ou préretraités").collect(Collectors.toSet()),
						Stream.of("Retraités").collect(Collectors.toSet()));
				scp_mapper.put(Stream.of("Autres inactifs", "Chômeurs", 
						"Elèves, étudiants, stagiaires non rémunéré de 14 ans ou plus", "Femmes ou hommes au foyer").collect(Collectors.toSet()), 
						Stream.of("Autres personnes sans activité professionnelle").collect(Collectors.toSet()));
				scp_mapper.put(Stream.of("Moins de 14 ans").collect(Collectors.toSet()), 
						Stream.of((String) null).collect(Collectors.toSet()));
				scp_mapper.put(Stream.of("Actifs ayant un emploi, y compris sous apprentissage ou en stage rémunéré").collect(Collectors.toSet()), 
						Stream.of("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers").collect(Collectors.toSet()));
				
				
				sampleAttributes.add(attf.createAttribute("SCP", GSEnumDataType.String, 
						scpInput, scpModel, GSEnumAttributeType.unique));
						*/
				
				// --------------------------
				// Setup "MENAGE" attribute: MENAGE
				// --------------------------
				
				/*
				householdAttributes.add(attf.createAttribute("Ménage", GSEnumDataType.String, 
						Arrays.asList("Couple sans enfant", "Couple avec enfant(s)", 
								"Famille monoparentale composée d'un homme avec enfant(s)", "Famille monoparentale composée d'une femme avec enfant(s)"), 
						GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "ENFANT" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute("Enfants", GSEnumDataType.String, 
						Arrays.asList("Aucun enfant de moins de 25 ans", "1 enfant de moins de 25 ans", 
								"2 enfants de moins de 25 ans", "3 enfants de moins de 25 ans", 
								"4 enfants ou plus de moins de 25 ans"), GSEnumAttributeType.unique));
								
				
				// --------------------------
				// Setup "TAILLE" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute("Taille", GSEnumDataType.Integer, 
						Arrays.asList("1 personne", "2 personnes", "3 personnes", "4 personnes", "5 personnes", "6 personnes ou plus"),
						Arrays.asList("1", "2", "3", "4", "5", "6"),
						GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "CSP REF" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute("CSP référent", GSEnumDataType.String, 
						Arrays.asList("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"), 
						GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "SEXE REF" attribute: MENAGE
				// --------------------------
				
				householdAttributes.add(attf.createAttribute("Sexe référent", GSEnumDataType.String, 
						Arrays.asList("Hommes", "Femmes"), GSEnumAttributeType.unique));
				
				// --------------------------
				// Setup "AGE REF" attribute: MENAGE
				// --------------------------
				
				// A mapper to link individual age and menage ref age
				Map<Set<String>, Set<String>> mapper = new HashMap<>();
				mapper.put(Stream.of("Moins de 20 ans").collect(Collectors.toSet()), 
						Stream.of("Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("20 à 24 ans").collect(Collectors.toSet()), 
						Stream.of("20 à 24 ans").collect(Collectors.toSet())); 
				mapperA1.put(Stream.of("25 à 39 ans").collect(Collectors.toSet()), 
						Stream.of("25 à 29 ans", "30 à 34 ans", "35 à 39 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("40 à 54 ans").collect(Collectors.toSet()), 
						Stream.of("40 à 44 ans", "45 à 49 ans", "50 à 54 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("55 à 64 ans").collect(Collectors.toSet()),
						Stream.of("55 à 59 ans", "60 à 64 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("65 à 79 ans").collect(Collectors.toSet()), 
						Stream.of("65 à 69 ans", "70 à 74 ans", "75 à 79 ans").collect(Collectors.toSet()));
				mapperA1.put(Stream.of("80 ans ou plus").collect(Collectors.toCollection(HashSet::new)),
						Stream.of("80 à 84 ans", "85 à 89 ans", "90 à 94 ans", 
						"95 à 99 ans", "100 ans ou plus").collect(Collectors.toCollection(HashSet::new)));
				householdAttributes.add(attf.createAttribute("Age référent", GSEnumDataType.Integer, 
						mapperA1.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GSEnumAttributeType.range, referentAgeAttribute, mapper));
						*/
				
			} catch (GSIllegalRangedData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// ------------------------------
			// SERIALIZE CONFIGURATION FILES
			// ------------------------------

			// The whole set of attributes
			Set<AGenstarAttribute> attList = indivAttributes; /*Stream.concat(Stream.concat(indivAttributes.stream(), sampleAttributes.stream()), 
					householdAttributes.stream()).collect(Collectors.toSet());*/
			// The list of input files
			List<GSSurveyFile> surveyFiles = individualDataFiles; /*Stream.concat(Stream.concat(individualDataFiles.stream(), sampleDataFiles.stream()), 
					householdDataFiles.stream()).collect(Collectors.toList());*/
			// The key-attribute mapping
			Map<String, IAttribute<? extends IValue>> keyMap = new HashMap<>();
			
			try {
				gxs.setMkdir(Paths.get(CONF_CLASS_PATH).toAbsolutePath());
				GosplConfigurationFile gsdI = new GosplConfigurationFile(surveyFiles, attList, keyMap);
				gxs.serializeGSConfig(gsdI, CONF_EXPORT);
				System.out.println("Serialize Genstar input data with:\n"+
						gsdI.getAttributes().size()+" attributs\n"+
						gsdI.getDataFiles().size()+" data files");
								
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
