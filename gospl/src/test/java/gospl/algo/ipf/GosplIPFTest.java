package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.GosplAlgoUtilTest;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.generator.DistributionBasedGenerator;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.algo.sampler.sr.GosplBasicSampler;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.validation.GosplIndicatorFactory;

public class GosplIPFTest {

	public static double SEED_RATIO = Math.pow(10, -1);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;

	public static IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed;	
	public static INDimensionalMatrix<APopulationAttribute,APopulationValue,Double> marginals;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		GosplAlgoUtilTest gaut = new GosplAlgoUtilTest();

		seed = gaut.buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		marginals = new GosplNDimensionalMatrixFactory().createDistribution(gaut.buildPopulation(POPULATION_SIZE));
	}

	@Test
	public void test() {
		ISyntheticReconstructionAlgo<IDistributionSampler> inferenceAlgo = new DistributionInferenceIPFAlgo(seed);
		ISampler<ACoordinate<APopulationAttribute, APopulationValue>> sampler = null;
		try {
			sampler = inferenceAlgo.inferSRSampler(marginals, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ISyntheticGosplPopGenerator gosplGenerator = new DistributionBasedGenerator(sampler);
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> popOut = gosplGenerator.generate(GENERATION_SIZE);
		
		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
		
		GosplIndicatorFactory gif = GosplIndicatorFactory.getFactory();
		double srmse = gif.getSRMSE(marginals, popOut);
		double aapd = gif.getAAPD(marginals, popOut);
		double rsszStar = gif.getRSSZstar(marginals, popOut);
		// Extended test based on validation criteria
		assertTrue("SRMSE = "+srmse, srmse > 0d && srmse < 1d);
		assertTrue("AAPD = "+aapd, aapd > 0d && aapd < 1d);
		assertTrue("AAPD = "+rsszStar, rsszStar > 0d);
		System.out.println("SRMSE = "+srmse+" | AAPD = "+aapd+" | RSSZ* = "+rsszStar);
	}

}
