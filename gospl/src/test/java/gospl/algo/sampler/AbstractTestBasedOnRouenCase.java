package gospl.algo.sampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarJsonUtil;
import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplInputDataManager;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.sampler.IHierarchicalSampler;
import gospl.sampler.ISampler;

public abstract class AbstractTestBasedOnRouenCase<SamplerType extends ISampler<ACoordinate<Attribute<? extends IValue>, IValue>>> {

	public static String INDIV_CLASS_PATH = "Rouen_insee_indiv";
	public static String INDIV_EXPORT = "GSC_RouenIndividual";
	public static String indiv1 = "Age & Couple-Tableau 1.csv";
	public static String indiv2 = "Age & Sexe & CSP-Tableau 1.csv";
	public static String indiv3 = "Age & Sexe-Tableau 1.csv";
	
	public static String HHOLD_CLASS_PATH = "Rouen_insee_menage";
	public static String HHOLD_EXPORT = "GSC_RouenHoushold";
	public static String menage1 = "Ménage & Enfants-Tableau 1.csv";
	public static String menage2 = "Taille ménage & CSP référent-Tableau 1.csv";
	public static String menage3 = "Taille ménage & Sex & Age-Tableau 1.csv";
	
	public static String SAMPLE_CLASS_PATH = "Rouen_sample";
	public static String SAMPLE_EXPORT = "GSC_RouenSample";
	public static String sample1 = "Rouen_sample_IRIS.csv";

	/**
	 * Inherited test cases should provide the sampler to test
	 * @return
	 */
	protected abstract SamplerType getSamplerToTest();
	
	
	protected abstract ISyntheticReconstructionAlgo<SamplerType> getInferenceAlgoToTest();
	
	/**
	 * Provides the configuration file for Rouen
	 * @return
	 */
	protected GenstarConfigurationFile getConfigurationFile() {
		
		 // Setup the factory that build attribute
		
		 GenstarConfigurationFile gcf = null;
		 try {
			 gcf = new GenstarJsonUtil().unmarchalConfigurationFileFromGenstarJson(
					 Paths.get("src/test/resources/rouen_demographics/rouen_demographics.gns"));
		 } catch (IllegalArgumentException | IOException e) {
			 e.printStackTrace();
			 throw new RuntimeException("cannot find the configuration file or one of its subfiles: "+e.getMessage(), e);
		 } 
		 System.out.println("Deserialize Genstar data configuration contains:\n"+
						 gcf.getDemoDictionary().getAttributes().size()+" attributs\n"+
						 gcf.getSurveyWrappers().size()+" data files");
						
		 return gcf;
	}

	
	@Test
	public void testGenerateWithoutFailure() {
		
		// parameters of the test
		int targetPopulationSize = 500;
		GenstarConfigurationFile confFile = this.getConfigurationFile();

		// THE POPULATION TO BE GENERATED
		GosplPopulation population = null;

		// INSTANCIATE FACTORY
		GosplInputDataManager df = new GosplInputDataManager(confFile);
		
		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS
		/*try {
			df.buildDataTables();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InvalidSurveyFormatException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildDataTables();	
		} catch (RuntimeException|InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 

		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator


		final long timestampStart = System.currentTimeMillis();
		
		// so we collapse all distribution build from the data
		INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution = null;
		try {
			distribution = df.collapseDataTablesIntoDistribution();
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (final IllegalControlTotalException e1) {
			e1.printStackTrace();
		}

		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final ISyntheticReconstructionAlgo<SamplerType> distributionInfAlgo = this.getInferenceAlgoToTest();
		ISampler<ACoordinate<Attribute<? extends IValue>,IValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferSRSampler(
					distribution, 
					this.getSamplerToTest()
					);

		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}


		// BUILD THE GENERATOR
		final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator((IHierarchicalSampler) sampler);

		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulationSize);
			
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}

		final long durationMs = (System.currentTimeMillis() - timestampStart);
		
		double timePerIndic = (double)durationMs/(population.size());
		
		System.out.println("generated in "+durationMs+"ms --- "+timePerIndic+"ms per individal");
		
		TemporaryFolder tmpDir = new TemporaryFolder();
		

		// MAKE REPORT

		// TODO: move to core io => generic method to export report of IPopolution or any other IEntity collection
		try {
			tmpDir.create();
			System.out.println("will export population into: "+tmpDir.getRoot().getAbsolutePath());

			File exportFile = tmpDir.newFile("PopExport.csv");
			File reportFile = tmpDir.newFile("PopReport.csv");
			
			GosplSurveyFactory sf = new GosplSurveyFactory();
			sf.createSummary(exportFile, GSSurveyType.Sample, population);
			sf.createSummary(reportFile, GSSurveyType.GlobalFrequencyTable, population);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidSurveyFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		

//		fail("Not yet implemented - yet.");
		
	}

}
