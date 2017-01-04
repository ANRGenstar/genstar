package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.GosplPopulation;
import gospl.algo.GosplAlgoUtilTest;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.algo.sampler.sr.GosplBasicSampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;

public class GosplIPFTest {

	public static double SEED_RATIO = Math.pow(10, -2);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;

	public static IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> objectif;
	public static IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed;
	
	public static INDimensionalMatrix<APopulationAttribute,APopulationValue,Double> marginals;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		GosplAlgoUtilTest gaut = new GosplAlgoUtilTest();
		
		objectif = gaut.getPopulation(POPULATION_SIZE);
		
		List<APopulationEntity> collectionSeed = new ArrayList<>(objectif)
				.subList(0, (int)(POPULATION_SIZE * SEED_RATIO));
		seed = new GosplPopulation();
		Collections.shuffle(collectionSeed);
		collectionSeed.stream().forEach(entity -> seed.add(entity));
		
		marginals = GosplDistributionFactory.createDistribution(objectif);
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
		assertEquals(GENERATION_SIZE, popOut.size());
	}

}
