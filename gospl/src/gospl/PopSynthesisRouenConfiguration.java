package gospl;

import java.io.File;
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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gospl.excpetion.GSException;
import gospl.metamodel.attribut.AttributeFactory;
import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import gospl.survey.GosplConfigurationFile;
import gospl.survey.GosplMetatDataType;
import gospl.survey.adapter.GosplDataFile;
import gospl.survey.adapter.GosplXmlSerializer;
import io.data.GSDataType;
import io.datareaders.surveyreader.exception.GSIllegalRangedData;

public class PopSynthesisRouenConfiguration {

	public static String INDIV_CLASS_PATH = "template/Rouen/insee_indiv";
	public static String HHOLD_CLASS_PATH = "template/Rouen/insee_ménage";

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
			
			
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Couple-Tableau 1.csv",
					GosplMetatDataType.ContingencyTable, 1, 1, ';'));
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Sexe & CSP-Tableau 1.csv",
					GosplMetatDataType.ContingencyTable, 2, 1, ';'));
			individualDataFiles.add(new GosplDataFile(INDIV_CLASS_PATH+File.separator+"Age & Sexe-Tableau 1.csv",
					GosplMetatDataType.ContingencyTable, 1, 1, ';'));
			
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Ménage & Enfants-Tableau 1.csv",
					GosplMetatDataType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Taille ménage & CSP référent-Tableau 1.csv", 
					GosplMetatDataType.ContingencyTable, 1, 1, ';'));
			householdDataFiles.add(new GosplDataFile(HHOLD_CLASS_PATH+File.separator+"Taille ménage & Sex & Age-Tableau 1.csv", 
					GosplMetatDataType.ContingencyTable, 2, 1, ';'));

			try {
				// Instantiate a referent attribute
				IAttribute referentAgeAttribute = attf.createAttribute("Age", GSDataType.Integer, 
						Arrays.asList("Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
								"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"), GosplValueType.range);
				indivAttributes.add(referentAgeAttribute);
				// Create a mapper
				Map<String, Set<String>> mapperA1 = new HashMap<>();
				mapperA1.put("15 à 19 ans", new HashSet<>(Arrays.asList("15 à 19 ans")));
				mapperA1.put("20 à 24 ans", new HashSet<>(Arrays.asList("20 à 24 ans")));
				mapperA1.put("25 à 39 ans", new HashSet<>(Arrays.asList("25 à 29 ans", "30 à 34 ans", "35 à 39 ans")));
				mapperA1.put("40 à 54 ans", new HashSet<>(Arrays.asList("40 à 44 ans", "45 à 49 ans", "50 à 54 ans")));
				mapperA1.put("55 à 64 ans", new HashSet<>(Arrays.asList("55 à 59 ans", "60 à 64 ans")));
				mapperA1.put("65 à 79 ans", new HashSet<>(Arrays.asList("65 à 69 ans", "70 à 74 ans", "75 à 79 ans")));
				mapperA1.put("80 ans ou plus", new HashSet<>(Arrays.asList("80 à 84 ans", "85 à 89 ans", "90 à 94 ans", 
						"95 à 99 ans", "100 ans ou plus")));
				// Instantiate an aggregated attribute using previously referent attribute
				indivAttributes.add(attf.createAttribute("Age_2", GSDataType.Integer,
						new ArrayList<>(mapperA1.keySet()), GosplValueType.range, referentAgeAttribute,
						mapperA1));
				// Create another mapper
				Map<String, Set<String>> mapperA2 = new HashMap<>();
				mapperA2.put("15 à 19 ans", new HashSet<>(Arrays.asList("15 à 19 ans")));
				mapperA2.put("20 à 24 ans", new HashSet<>(Arrays.asList("20 à 24 ans")));
				mapperA2.put("25 à 39 ans", new HashSet<>(Arrays.asList("25 à 29 ans", "30 à 34 ans", "35 à 39 ans")));
				mapperA2.put("40 à 54 ans", new HashSet<>(Arrays.asList("40 à 44 ans", "45 à 49 ans", "50 à 54 ans")));
				mapperA2.put("55 à 64 ans", new HashSet<>(Arrays.asList("55 à 59 ans", "60 à 64 ans")));
				mapperA2.put("65 ans ou plus", new HashSet<>(Arrays.asList("65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
						"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus")));
				indivAttributes.add(attf.createAttribute("Age_3", GSDataType.Integer,
						new ArrayList<>(mapperA2.keySet()), GosplValueType.range, referentAgeAttribute,
						mapperA2));		
				indivAttributes.add(attf.createAttribute("Couple", GSDataType.String, 
						Arrays.asList("Vivant en couple", "Ne vivant pas en couple"), 
						GosplValueType.unique));
				indivAttributes.add(attf.createAttribute("CSP", GSDataType.String, 
						Arrays.asList("Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
								"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
								"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"), 
						GosplValueType.unique));
				indivAttributes.add(attf.createAttribute("Sexe", GSDataType.String,
						Arrays.asList("Hommes", "Femmes"), GosplValueType.unique));
				
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
				Map<String, Set<String>> mapper = new HashMap<>();
				mapper.put("Moins de 20 ans", new HashSet<>(Arrays.asList("Moins de 5 ans", "5 à 9 ans", "10 à 14 ans",
						"15 à 19 ans")));
				mapper.put("20 à 24 ans", new HashSet<>(Arrays.asList("20 à 24 ans")));
				mapper.put("25 à 39 ans", new HashSet<>(Arrays.asList("25 à 29 ans", "30 à 34 ans", "35 à 39 ans")));
				mapper.put("40 à 54 ans", new HashSet<>(Arrays.asList("40 à 44 ans", "45 à 49 ans", "50 à 54 ans")));
				mapper.put("55 à 64 ans", new HashSet<>(Arrays.asList("55 à 59 ans", "60 à 64 ans")));
				mapper.put("65 à 79 ans", new HashSet<>(Arrays.asList("65 à 69 ans", "70 à 74 ans", "75 à 79 ans")));
				mapper.put("80 ans ou plus", new HashSet<>(Arrays.asList("80 à 84 ans", "85 à 89 ans", "90 à 94 ans", 
						"95 à 99 ans", "100 ans ou plus")));
				householdAttributes.add(attf.createAttribute("Age référent", GSDataType.Integer, 
						new ArrayList<>(mapper.keySet()), GosplValueType.range, referentAgeAttribute,
						mapper));
				
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
