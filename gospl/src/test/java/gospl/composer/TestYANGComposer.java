package gospl.composer;

import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.algo.composer.yang.YangComposerAlgo;
import gospl.distribution.GosplInputDataManager;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.io.insee.ReadINSEEDictionaryUtils;
import gospl.io.util.ReadPopulationsUtils;

public class TestYANGComposer {


	@Test
	public void test() {

		// load the list of dwellings
		IGenstarDictionary<Attribute<? extends IValue>> dicoDwellings = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/yang/dwelling_household_toy/MOD_DWELLING.txt");
		
		GosplPopulation popDwellings = ReadPopulationsUtils.readFromCSVFile(
				"src/test/resources/yang/dwelling_household_toy/list_dwellings.csv", 
				dicoDwellings);
	
		// load the list of households
		IGenstarDictionary<Attribute<? extends IValue>> dicoHouseholds = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/yang/dwelling_household_toy/MOD_HOUSEHOLD.txt");
		
		GosplPopulation popHouseholds = ReadPopulationsUtils.readFromCSVFile(
				"src/test/resources/yang/dwelling_household_toy/list_households.csv", 
				dicoHouseholds);
	
		// load the probabilities
		GosplSurveyFactory gsf = new GosplSurveyFactory();

		@SuppressWarnings("unused")
		IGSSurvey survey = null;
		try {
			survey = gsf.getSurvey(
					"src/test/resources/yang/dwelling_household_toy/surface vs size.csv", 
					GSSurveyType.LocalFrequencyTable);
		} catch (InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
		
		IGenstarDictionary<Attribute<? extends IValue>> dicoMerge = 
				dicoDwellings.merge(dicoHouseholds);
		
		GenstarConfigurationFile gcf = new GenstarConfigurationFile();
		gcf.setBaseDirectory(FileSystems.getDefault().getPath("."));
		gcf.setDictionary(dicoMerge);
		

		GSSurveyWrapper surveyWrapper = new GSSurveyWrapper(
				FileSystems.getDefault().getPath("src/test/resources/yang/dwelling_household_toy/surface vs size.csv"), 
				GSSurveyType.GlobalFrequencyTable,
				';',
				1,
				1
				);
		gcf.addSurveyWrapper(surveyWrapper);
		
		GosplInputDataManager gidm = new GosplInputDataManager(gcf);
		try {
			gidm.buildDataTables();
		} catch (InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata;
		try {
			 ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		YangComposerAlgo yang = new YangComposerAlgo(
				popDwellings,
				popHouseholds,
				ndata
				);
		yang.computeExpectedChildrenProperties();
		
		//fail("Not yet implemented");
	}

}
