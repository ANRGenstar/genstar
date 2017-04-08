package gospl.algo.sampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarXmlSerializer;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.util.GSPerformanceUtil;
import gospl.GosplPopulation;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.generator.DistributionBasedGenerator;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.distribution.GosplDistributionBuilder;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.entity.attribute.GosplAttributeFactory;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;

public abstract class AbstractTestBasedOnRouenCase<SamplerType extends ISampler<ACoordinate<APopulationAttribute, APopulationValue>>> {

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

		 // Setup the serializer that save configuration file
		 GenstarXmlSerializer gxs = null;
		 try {
			 gxs = new GenstarXmlSerializer();
		 } catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		
		 // Setup the factory that build attribute
		 @SuppressWarnings("unused")
		GosplAttributeFactory attf = new GosplAttributeFactory();
		
		 GenstarConfigurationFile gcf = null;
		 try {
			 gcf = gxs.deserializeGSConfig(new File("../../template/target/classes/rouen/gospl/data/GSC_Rouen_IS.xml"));
		 } catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 System.out.println("Deserialize Genstar data configuration contains:\n"+
						 gcf.getAttributes().size()+" attributs\n"+
						 gcf.getSurveyWrappers().size()+" data files");
						
		 return gcf;
	}

	
	@Test
	public void test() {
		
		// parameters of the test
		int targetPopulationSize = 100;
		GenstarConfigurationFile confFile = this.getConfigurationFile();

		// THE POPULATION TO BE GENERATED
		GosplPopulation population = null;

		// INSTANCIATE FACTORY
		GosplDistributionBuilder df = new GosplDistributionBuilder(confFile);
		
		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS
		try {
			df.buildDistributions();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InvalidSurveyFormatException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (final RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvalidSurveyFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator

		// so we collapse all distribution build from the data
		INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (final IllegalControlTotalException e1) {
			e1.printStackTrace();
		}

		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final ISyntheticReconstructionAlgo<SamplerType> distributionInfAlgo = this.getInferenceAlgoToTest();
		ISampler<ACoordinate<APopulationAttribute,APopulationValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferSRSampler(
					distribution, 
					this.getSamplerToTest()
					);

		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}


		final GSPerformanceUtil gspu =
				new GSPerformanceUtil("Start generating synthetic population of size " + targetPopulationSize);

		// BUILD THE GENERATOR
		final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator((IHierarchicalSampler) sampler);

		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulationSize);
			gspu.sysoStempPerformance("End generating synthetic population: elapse time", this);
			
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}
		
		TemporaryFolder tmpDir = new TemporaryFolder();
		

		// MAKE REPORT

		// TODO: move to core io => generic method to export report of IPopolution or any other IEntity collection
		try {
			tmpDir.create();
			System.out.println("will export population into: "+tmpDir.getRoot().getAbsolutePath());

			File exportFile = tmpDir.newFile("PopExport.csv");
			File reportFile = tmpDir.newFile("PopReport.csv");
			
			GosplSurveyFactory sf = new GosplSurveyFactory();
			sf.createSurvey(exportFile, GSSurveyType.Sample, population);
			sf.createSurvey(reportFile, GSSurveyType.GlobalFrequencyTable, population);
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
