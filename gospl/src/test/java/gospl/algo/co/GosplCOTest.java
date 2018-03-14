package gospl.algo.co;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.randomwalk.RandomWalk;
import gospl.algo.co.simannealing.SimulatedAnnealing;
import gospl.algo.co.tabusearch.TabuList;
import gospl.algo.co.tabusearch.TabuSearch;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.SampleBasedGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.IEntitySampler;
import gospl.sampler.co.CombinatorialOptimizationSampler;

public class GosplCOTest {

	public static int MAX_ITERATION = (int) Math.pow(10, 3);
	// TABU SPECIFI
	public static int TABULIST_SIZE = (int) Math.pow(10, 1);
	
	public static boolean DATA_BASED_POP = false;
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static double SAMPLE_RATIO = 0.2;
	
	public static String[] DICTIONNARIES = new String[]{"defaultDictionary.gns", "simpleDictionary.gns", "withMapDictionary.gns"};
	public static int DICO = 1;
	
	public static IPopulation<ADemoEntity, Attribute<? extends IValue>> SAMPLE;
	public static INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> MARGINALS;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String dico = DICTIONNARIES[DICO];
		
		// BUILD A RANDOM (UNIFORM) SAMPLE FROM A DICTIONNAIRE
		SAMPLE = new GSUtilPopulation(dico).buildPopulation((int)(POPULATION_SIZE * SAMPLE_RATIO));
		
		// BUILD RANDOM (UNIFORM) MARGINALS FROM A DICTIONNAIRE
		MARGINALS = new GosplNDimensionalMatrixFactory()
				.createContingency(new GSUtilPopulation(dico).buildPopulation(POPULATION_SIZE)); 
	}

	@Test
	public void simulatedAnnealingTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new SimulatedAnnealing(), SAMPLE,
						DATA_BASED_POP));
		
		sampler.addObjectives(MARGINALS);
		
		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);
		
		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}
	
	@Test
	public void tabuSearchTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new TabuSearch(new TabuList(TABULIST_SIZE), MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP));
		
		sampler.addObjectives(MARGINALS);
		
		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);
		
		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}

	@Test
	public void randomWalkTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new RandomWalk(MAX_ITERATION), SAMPLE, DATA_BASED_POP));
		
		sampler.addObjectives(MARGINALS);
		
		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);
		
		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}
}
