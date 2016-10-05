package template.rouen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gospl.exception.GSException;
import gospl.metamodel.attribut.AttributeFactory;
import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import gospl.survey.GosplConfigurationFile;
import gospl.survey.GosplMetaDataType;
import gospl.survey.adapter.GosplDataFile;
import gospl.survey.adapter.GosplXmlSerializer;
import io.data.GSDataType;
import io.datareaders.surveyreader.exception.GSIllegalRangedData;

public class PopSynthesisRouenConfiguration {

	public static String INDIV_CLASS_PATH = "data/Rouen/insee_indiv";
	public static String HHOLD_CLASS_PATH = "data/Rouen/insee_ménage";

	public static void main(String[] args) throws InvalidFormatException {
		// TODO Auto-generated method stub

		GosplXmlSerializer gxs = null;
		try {
			gxs = new GosplXmlSerializer();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AttributeFactory attf = new AttributeFactory();
		if(new ArrayList<>(Arrays.asList(args)).isEmpty()){

			List<GosplDataFile> individualDataFiles = new ArrayList<>();
			Set<IAttribute> indivAttributes = new HashSet<>();
			
			List<GosplDataFile> householdDataFiles = new ArrayList<>();
			Set<IAttribute> householdAttributes = new HashSet<>();
			
			// TODO: setup the sample data file
			List<GosplDataFile> sampleDataFiles = new ArrayList<>();
			Set<IAttribute> sampleAttributes = new HashSet<>();
			
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Couple-Tableau 1.csv",
					GosplMetaDataType.ContingencyTable, 1, 1, ';'));
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Sexe & CSP-Tableau 1.csv",
					GosplMetaDataType.ContingencyTable, 2, 1, ';'));
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Sexe-Tableau 1.csv",
					GosplMetaDataType.ContingencyTable, 1, 1, ';'));
			
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Ménage & Enfants-Tableau 1.csv",
					GosplMetaDataType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Taille ménage & CSP référent-Tableau 1.csv", 
					GosplMetaDataType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Taille ménage & Sex & Age-Tableau 1.csv", 
					GosplMetaDataType.ContingencyTable, 2, 1, ';'));

			try {
				// Instantiate a referent attribute
				IAttribute referentAgeAttribute = attf.createAttribute("Age", GSDataType.Integer, 
						Arrays.asList("Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
								"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"), GosplValueType.range);
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
				indivAttributes.add(attf.createAttribute("Age_2", GSDataType.Integer,
						mapperA1.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GosplValueType.range, referentAgeAttribute, mapperA1));
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
				indivAttributes.add(attf.createAttribute("Age_3", GSDataType.Integer,
						mapperA2.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GosplValueType.range, referentAgeAttribute, mapperA2));		
				
				List<String> ageInput = Arrays.asList("000", "005", "010", "015", "020", "025", "030",
						"035", "040", "045", "050", "055", "060", "065", "070", "075", "080", "085",
						"090", "095", "100", "105", "110", "115", "120");
				List<String> ageModel = Arrays.asList("0 à 4 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", 
						"20 à 24 ans", "25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
						"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
						"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 à 104 ans", "105 à 109 ans", 
						"110 à 114 ans", "115 à 129 ans", "120 ans");
				sampleAttributes.add(attf.createAttribute("Age_y", GSDataType.Integer, 	
						ageInput, ageModel, GosplValueType.range));
				
				IAttribute couple = attf.createAttribute("Couple", GSDataType.String, 
						Arrays.asList("Vivant en couple", "Ne vivant pas en couple"), 
						GosplValueType.unique);
				sampleAttributes.add(couple);
				indivAttributes.add(couple);
				IAttribute sexe = attf.createAttribute("Sexe", GSDataType.String,
						Arrays.asList("Hommes", "Femmes"), GosplValueType.unique);
				sampleAttributes.add(sexe);
				indivAttributes.add(sexe);
				indivAttributes.add(attf.createAttribute("CSP", GSDataType.String, 
						Arrays.asList("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"), 
						GosplValueType.unique));
				
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
				sampleAttributes.add(attf.createAttribute("SCP", GSDataType.String, 
						scpInput, scpModel, GosplValueType.unique));
				
				
				householdAttributes.add(attf.createAttribute("Ménage", GSDataType.String, 
						Arrays.asList("Couple sans enfant", "Couple avec enfant(s)", 
								"Famille monoparentale composée d'un homme avec enfant(s)", "Famille monoparentale composée d'une femme avec enfant(s)"), 
						GosplValueType.unique));
				householdAttributes.add(attf.createAttribute("Enfants", GSDataType.String, 
						Arrays.asList("Aucun enfant de moins de 25 ans", "1 enfant de moins de 25 ans", 
								"2 enfants de moins de 25 ans", "3 enfants de moins de 25 ans", 
								"4 enfants ou plus de moins de 25 ans"), GosplValueType.unique));
				householdAttributes.add(attf.createAttribute("Taille", GSDataType.Integer, 
						Arrays.asList("1 personne", "2 personnes", "3 personnes", "4 personnes", "5 personnes", "6 personnes ou plus"), 
						GosplValueType.unique));
				householdAttributes.add(attf.createAttribute("CSP référent", GSDataType.String, 
						Arrays.asList("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"), 
						GosplValueType.unique));
				householdAttributes.add(attf.createAttribute("Sexe référent", GSDataType.String, 
						Arrays.asList("Hommes", "Femmes"), GosplValueType.unique));
				
				// Another mapper
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
				householdAttributes.add(attf.createAttribute("Age référent", GSDataType.Integer, 
						mapperA1.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()), 
						GosplValueType.range, referentAgeAttribute, mapper));
				
			} catch (GSException | GSIllegalRangedData e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				gxs.setMkdir(Paths.get(INDIV_CLASS_PATH));
				GosplConfigurationFile gsdI = new GosplConfigurationFile(individualDataFiles, indivAttributes);
				gxs.serializeGSConfig(gsdI, "GSC_RouenIndividual");
				System.out.println("Serialize Genstar individual data with:\n"+
						gsdI.getAttributes().size()+" attributs\n"+
						gsdI.getDataFiles().size()+" data files");
				
				gxs.setMkdir(Paths.get(HHOLD_CLASS_PATH));
				GosplConfigurationFile gsdHH = new GosplConfigurationFile(householdDataFiles, householdAttributes);
				gxs.serializeGSConfig(gsdHH, "GSC_RouenHoushold");
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
