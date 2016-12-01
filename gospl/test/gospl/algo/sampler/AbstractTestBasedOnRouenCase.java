package gospl.algo.sampler;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import core.io.configuration.GosplConfigurationFile;
import core.io.configuration.GosplXmlSerializer;
import core.io.exception.InvalidFileTypeException;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.AttributeFactory;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.util.GSPerformanceUtil;
import gospl.GosplSPTemplate;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.metamodel.GosplPopulation;

public abstract class AbstractTestBasedOnRouenCase<SamplerType extends ISampler<ACoordinate<AGenstarAttribute, AGenstarValue>>> {

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
	
	
	protected abstract IDistributionInferenceAlgo<SamplerType> getInferenceAlgoToTest();
	
	/**
	 * Provides the configuration file for Rouen
	 * @return
	 */
	protected GosplConfigurationFile getConfigurationFile() {

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
		
		 GosplConfigurationFile gcf = null;
		 try {
			 gcf = gxs.deserializeGSConfig(new File("testdata/rouen1/GSC_RouenIndividual.xml"));
		 } catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 System.out.println("Deserialize Genstar data configuration contains:\n"+
						 gcf.getAttributes().size()+" attributs\n"+
						 gcf.getDataFiles().size()+" data files");
						
		 return gcf;
	}

	
	@Test
	public void test() {
		
		// parameters of the test
		int targetPopulationSize = 100;
		GosplConfigurationFile confFile = this.getConfigurationFile();

		// THE POPULATION TO BE GENERATED
		GosplPopulation population = null;

		// INSTANCIATE FACTORY
		GosplDistributionFactory df = new GosplDistributionFactory(confFile);
		
		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS
		try {
			df.buildDistributions();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InvalidFileTypeException e) {
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
		} catch (final InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator

		// so we collapse all distribution build from the data
		INDimensionalMatrix<AGenstarAttribute, AGenstarValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (final IllegalControlTotalException e1) {
			e1.printStackTrace();
		}

		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final IDistributionInferenceAlgo<SamplerType> distributionInfAlgo = this.getInferenceAlgoToTest();
		ISampler<ACoordinate<AGenstarAttribute,AGenstarValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(
					distribution, 
					this.getSamplerToTest()
					);

		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}


		final GSPerformanceUtil gspu =
				new GSPerformanceUtil("Start generating synthetic population of size " + targetPopulationSize);

		// BUILD THE GENERATOR
		final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator(sampler);

		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulationSize);
			gspu.sysoStempPerformance("End generating synthetic population: elapse time",
					GosplSPTemplate.class.getName());
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}

		fail("Not yet implemented - yet.");
		
	}

}
