package gospl.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Ignore;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.io.insee.ReadINSEEDictionaryUtils;
import gospl.io.util.ReadDictionaryUtils;
import gospl.io.util.ReadPopulationsUtils;

public class TestReadMatriciesFromCSV {

	protected GosplInputDataManager readFromFile(
			String filenameMatrix,
			IGenstarDictionary<Attribute<? extends IValue>> dictionnary,
			GSSurveyType surveyType) {

		// load the probabilities
		GenstarConfigurationFile gcf = new GenstarConfigurationFile();
		gcf.setBaseDirectory(FileSystems.getDefault().getPath("."));
		gcf.setDictionary(dictionnary);
		

		GSSurveyWrapper surveyWrapper = new GSSurveyWrapper(
				FileSystems.getDefault().getPath(filenameMatrix), 
				surveyType,
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
		
		return gidm;
		
	}
	
	/**
	 * Reads the data "Agent & Couple-contingency-Tableau1.csv" which contains a contingency table 
	 * based on the "Dico-Tableau 1.gns" dictionnary
	 * @return
	 */
	protected GosplInputDataManager readDatasetContingencyAgeCoupleTableau1() {

		IGenstarDictionary<Attribute<? extends IValue>> dictionnary = 
				ReadDictionaryUtils.readFromGenstarConfig(
						"src/test/resources/tables/Dico-Tableau 1.gns");
		
		GosplInputDataManager gidm = readFromFile(
				"src/test/resources/tables/Age & Couple-contingency-Tableau 1.csv", 
				dictionnary,
				GSSurveyType.ContingencyTable
				);

		return gidm;
	}
	
	/**
	 * Reads the data "Agent & Couple-local-frequency-Tableau1.csv" which contains a contingency table 
	 * based on the "Dico-Tableau 1.gns" dictionnary
	 * @return
	 */
	protected GosplInputDataManager readDatasetLocalFrequencyyAgeCoupleTableau1() {

		IGenstarDictionary<Attribute<? extends IValue>> dictionnary = 
				ReadDictionaryUtils.readFromGenstarConfig(
						"src/test/resources/tables/Dico-Tableau 1.gns");
		
		GosplInputDataManager gidm = readFromFile(
				"src/test/resources/tables/Age & Couple-local-frequency-Tableau 1.csv", 
				dictionnary,
				GSSurveyType.LocalFrequencyTable
				);

		return gidm;
	}
	


	/**
	 * Reads the data "Agent & Couple-global-frequency-Tableau1.csv" which contains a contingency table 
	 * based on the "Dico-Tableau 1.gns" dictionnary
	 * @return
	 */
	protected GosplInputDataManager readDatasetGlobalFrequencyAgeCoupleTableau1() {

		IGenstarDictionary<Attribute<? extends IValue>> dictionnary = 
				ReadDictionaryUtils.readFromGenstarConfig(
						"src/test/resources/tables/Dico-Tableau 1.gns");
		
		GosplInputDataManager gidm = readFromFile(
				"src/test/resources/tables/Age & Couple-global-frequency-Tableau 1.csv", 
				dictionnary,
				GSSurveyType.GlobalFrequencyTable
				);

		return gidm;
	}
	
	protected GosplInputDataManager readDatasetGlobalFrequencySurfaceAndSize() {

		// load the list of dwellings
		IGenstarDictionary<Attribute<? extends IValue>> dicoDwellings = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/yang/dwelling_household_toy/MOD_DWELLING.txt");
		
		@SuppressWarnings("unused")
		GosplPopulation popDwellings = ReadPopulationsUtils.readFromCSVFile(
				"src/test/resources/yang/dwelling_household_toy/list_dwellings.csv", 
				dicoDwellings);
	
		// load the list of households
		IGenstarDictionary<Attribute<? extends IValue>> dicoHouseholds = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/yang/dwelling_household_toy/MOD_HOUSEHOLD.txt");
		
		@SuppressWarnings("unused")
		GosplPopulation popHouseholds = ReadPopulationsUtils.readFromCSVFile(
				"src/test/resources/yang/dwelling_household_toy/list_households.csv", 
				dicoHouseholds);
	

		IGenstarDictionary<Attribute<? extends IValue>> dicoMerge = 
				dicoDwellings.merge(dicoHouseholds);
		
		GosplInputDataManager gidm = readFromFile(
				"src/test/resources/yang/dwelling_household_toy/surface vs size.csv", 
				dicoMerge,
				GSSurveyType.GlobalFrequencyTable
				);

		return gidm;
	}
	
	/**
	 * tests the reading of attributes using the direct attributes (without mapping)
	 */
	@Test
	public void testContingencyDirectAttributes() {
		
		GosplInputDataManager gidm = readDatasetContingencyAgeCoupleTableau1();
		
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata = null;
		try {
			ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		final double TOLERANCE = 0.000001;
		
		// ensure the sum is 1. as expected
		assertEquals(1.0, 
				ndata.getVal().getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare at the cell scale the values read from the file 
		// and compare them with what is expected
		assertEquals(1261d/510833, 
					ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
					TOLERANCE
					);
		assertEquals(40923d/510833, 
				ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
				
		
		assertEquals(89551d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(34756d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals(12073d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(20765d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare the more aggregate values
		assertEquals( (89551d+34756d)/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals( (12073d+20765d)/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals( (297100d)/510833, 
				ndata.getVal(
							"Couple",	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
	}
	


	/**
	 * tests the reading of attributes using the direct attributes (without mapping)
	 */
	@Test
	public void testContingencyDirectAttributesSurfaceSize() {
		
		GosplInputDataManager gidm = readDatasetGlobalFrequencySurfaceAndSize();
		
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata = null;
		try {
			ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		final double TOLERANCE = 0.000001;
		
		// ensure the sum is 1. as expected
		assertEquals(1.0, 
				ndata.getVal().getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare at the cell scale the values read from the file 
		// and compare them with what is expected
		assertEquals(0.8/5, 
					ndata.getVal(
							"SURF",		"moins de 10sm",
							"NBPERS", 	"1"
							).getValue().doubleValue(),
					TOLERANCE
					);
		
	}
	

	
	/**
	 * tests the reading of attributes using the mapped attributes 
	 */
	@Test
	@Ignore
	public void testContingencyMappedAttributes() {
		
		GosplInputDataManager gidm = readDatasetContingencyAgeCoupleTableau1();
		
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata = null;
		try {
			ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		final double TOLERANCE = 0.000001;
		
		// ensure the sum is 1. as expected
		assertEquals(1.0, 
				ndata.getVal().getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare at the cell scale the values read from the file 
		// and compare them with what is expected
		assertEquals(
					ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
					ndata.getVal(
							"Age_3",	"15 à 19 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
					TOLERANCE
					);
		assertEquals(40923d/510833, 
				ndata.getVal(
							"Age_3",	"15 à 19 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
				
		
		assertEquals(89551d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(34756d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals(12073d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(20765d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare the more aggregate values
		assertEquals( (89551d+34756d)/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals( (12073d+20765d)/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals( (297100d)/510833, 
				ndata.getVal(
							"Couple",	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
	}


	/**
	 * tests the reading of attributes using the direct attributes (without mapping)
	 */
	@Test
	public void testGlobalFrequencyDirectAttributes() {
		
		GosplInputDataManager gidm = readDatasetGlobalFrequencyAgeCoupleTableau1();
		
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata = null;
		try {
			ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		final double TOLERANCE = 0.0001;
		
		// ensure the sum is 1. as expected
		assertEquals(1.0, 
				ndata.getVal().getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare at the cell scale the values read from the file 
		// and compare them with what is expected
		assertEquals(1261d/510833, 
					ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
					TOLERANCE
					);
		assertEquals(40923d/510833, 
				ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
				
		
		assertEquals(89551d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(34756d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals(12073d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(20765d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare the more aggregate values
		assertEquals( (89551d+34756d)/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals( (12073d+20765d)/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals( (297100d)/510833, 
				ndata.getVal(
							"Couple",	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
	}
	

	/**
	 * tests the reading of attributes using the direct attributes (without mapping)
	 */
	@Test
	@Ignore // disabled as the meaning of local frequency means "needs a global frequency table"s
	public void testLocalFrequencyDirectAttributes() {
		
		GosplInputDataManager gidm = readDatasetLocalFrequencyyAgeCoupleTableau1();
		
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> ndata = null;
		try {
			ndata = gidm.collapseDataTablesIntoDistribution();
		} catch (IllegalDistributionCreation | IllegalControlTotalException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} 
		
		final double TOLERANCE = 0.000001;
		
		// ensure the sum is 1. as expected
		/*assertEquals(1.0, 
				ndata.getVal().getValue().doubleValue(),
				TOLERANCE
				);
		*/
		
		// compare at the cell scale the values read from the file 
		// and compare them with what is expected
		assertEquals(1261d/510833, 
					ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
					TOLERANCE
					);
		assertEquals(40923d/510833, 
				ndata.getVal(
							"Age_2",	"15 à 19 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
				
		
		assertEquals(89551d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(34756d/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals(12073d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals(20765d/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus",
							"Couple", 	"Ne vivant pas en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		// compare the more aggregate values
		assertEquals( (89551d+34756d)/510833, 
				ndata.getVal(
							"Age_2",	"40 à 54 ans"
							).getValue().doubleValue(),
				TOLERANCE
				);
		assertEquals( (12073d+20765d)/510833, 
				ndata.getVal(
							"Age_2",	"80 ans ou plus"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
		assertEquals( (297100d)/510833, 
				ndata.getVal(
							"Couple",	"Vivant en couple"
							).getValue().doubleValue(),
				TOLERANCE
				);
		
	}
	
	@Test
	public void testRecordAttribute() throws IOException, InvalidFormatException, 
		InvalidSurveyFormatException, IllegalDistributionCreation, IllegalControlTotalException {
		
		GenstarConfigurationFile gcf = new GenstarJsonUtil().unmarchalConfigurationFileFromGenstarJson(
				Paths.get("src","test","resources","rouen_demographics","rouen_iris.gns").toAbsolutePath());
		
		assertTrue(gcf.getDictionary().size() > 0);
		assertTrue(gcf.getDictionary().getRecords().size() > 0);
		
		GosplInputDataManager gidm = new GosplInputDataManager(gcf);
		
		// Read data
		gidm.buildDataTables();
		// Has correctly collapse record attribute
		assertTrue(gidm.getContingencyTables().stream().flatMap(matrix -> matrix.getDimensions().stream())
				.noneMatch(d -> d.getClass().equals(RecordAttribute.class)));
	}
	
}
