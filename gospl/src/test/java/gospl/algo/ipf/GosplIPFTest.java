package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.algo.GosplAlgoUtilTest;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;
import gospl.sampler.sr.GosplBasicSampler;
import gospl.validation.GosplIndicatorFactory;

public class GosplIPFTest {

	public static double SEED_RATIO = Math.pow(10, -1);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;

	public static IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed;	
	public static INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		GosplAlgoUtilTest gaut = new GosplAlgoUtilTest();

		seed = gaut.buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		marginals = new GosplNDimensionalMatrixFactory().createDistribution(gaut.buildPopulation(POPULATION_SIZE));
	}

	@Test
	public void test() {
		ISyntheticReconstructionAlgo<IDistributionSampler> inferenceAlgo = new SRIPFAlgo(seed);
		ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> sampler = null;
		try {
			sampler = inferenceAlgo.inferSRSampler(marginals, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ISyntheticGosplPopGenerator gosplGenerator = new DistributionBasedGenerator(sampler);
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = gosplGenerator.generate(GENERATION_SIZE);
		
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
