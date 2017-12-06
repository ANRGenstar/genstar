package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;
import gospl.sampler.sr.GosplBasicSampler;

public class GosplIPFTest {

	public static double SEED_RATIO = Math.pow(10, -1);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;
	public static int SEGMENTATION = 3;

	//@Test
	public void simpleTest() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation("simpleDictionary.gns").buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = 
				new GosplNDimensionalMatrixFactory().createDistribution(
						new GSUtilPopulation("simpleDictionary.gns").buildPopulation(POPULATION_SIZE));

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}

	//@Test
	public void defaultTest() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = null;
		try {
			seed = new GSUtilPopulation().buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			marginals = new GosplNDimensionalMatrixFactory().createDistribution(
					new GSUtilPopulation().buildPopulation(POPULATION_SIZE));
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	//@Test
	public void defaultTestWithSegmentedMatrix() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = null;
		try {
			seed = new GSUtilPopulation().buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			GSUtilPopulation gaut = new GSUtilPopulation();
			gaut.buildPopulation(POPULATION_SIZE);
			marginals = gaut.getSegmentedFrequency(SEGMENTATION);
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	
	
	@Test
	public void mappedTest() {

		GSUtilPopulation gsup = new GSUtilPopulation("withMapDictionary.gns");
		Set<DemographicAttribute<? extends IValue>> attributes = gsup.getDictionary()
				.getAttributes().stream().filter(a -> a.getReferentAttribute().equals(a))
				.collect(Collectors.toSet());
		
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation(attributes).buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = 
				new GosplNDimensionalMatrixFactory().createDistribution(
					gsup.buildPopulation(POPULATION_SIZE));

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	//@Test
	public void mappedWithSegmentedTest() {

		GSUtilPopulation gsup = new GSUtilPopulation("withMapDictionary.gns");
		
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation(gsup.getDictionary()
						.getAttributes().stream().filter(a -> a.getReferentAttribute().equals(a))
						.collect(Collectors.toSet())).buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			GSUtilPopulation gaut = new GSUtilPopulation();
			gaut.buildPopulation(POPULATION_SIZE);
			marginals = gaut.getSegmentedFrequency(SEGMENTATION);
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	/*
	 * DO THE IPF
	 */
	private IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> doIPF(
			IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed,
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals){
		ISyntheticReconstructionAlgo<IDistributionSampler> inferenceAlgo = new SRIPFAlgo(seed);
		ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> sampler = null;
		try {
			sampler = inferenceAlgo.inferSRSampler(marginals, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ISyntheticGosplPopGenerator gosplGenerator = new DistributionBasedGenerator(sampler);
		return gosplGenerator.generate(GENERATION_SIZE);
	}
	
	

}
