package gospl.algo.co;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.algo.co.hillclimbing.HillClimbing;
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

	public static int ITER_COMP = 2;

	public static int MAX_ITERATION = (int) Math.pow(10, 4);
	// TABU SPECIFI
	public static int TABULIST_SIZE = 40;

	public static boolean DATA_BASED_POP = false;
	public static int POPULATION_SIZE = (int) Math.pow(10, 3);
	public static double SAMPLE_RATIO = 0.1;

	public static String[] DICTIONNARIES = new String[]{"defaultDictionary.gns", "simpleDictionary.gns", "withMapDictionary.gns"};
	public static int DICO = 1;

	public static IPopulation<ADemoEntity, Attribute<? extends IValue>> POPULATION;
	public static IPopulation<ADemoEntity, Attribute<? extends IValue>> SAMPLE;
	public static INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> MARGINALS;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String dico = DICTIONNARIES[DICO];

		// BUILD A RANDOM POPULATION
		POPULATION = new GSUtilPopulation(dico).buildPopulation((int)(POPULATION_SIZE));

		// BUILD MARGINALS FROM THE RANDOM POPULATION
		MARGINALS = new GosplNDimensionalMatrixFactory().createContingency(POPULATION);

		// TAKE OF SAMPLE OF THE FIRST (POPULATION_SIZE * SAMPLE_RATIO) ENTITY OF THE POPULATION
		SAMPLE = new GosplPopulation(POPULATION.stream()
				.limit((int)(POPULATION_SIZE*SAMPLE_RATIO))
				.collect(Collectors.toSet())); 
	}

	@Test
	@Ignore
	public void simulatedAnnealingTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new SimulatedAnnealing(), SAMPLE,
						DATA_BASED_POP, false));

		sampler.addObjectives(MARGINALS);

		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);

		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}

	@Test
	public void tabuSearchTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new TabuSearch(new TabuList(TABULIST_SIZE), 
								POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, false));

		sampler.addObjectives(MARGINALS);

		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);

		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}

	@Test
	@Ignore
	public void hillClimbingTest() {
		IEntitySampler sampler = new SampleBasedAlgorithm()
				.setupCOSampler(SAMPLE, new CombinatorialOptimizationSampler<>(
						new HillClimbing(POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, false));

		sampler.addObjectives(MARGINALS);

		ISyntheticGosplPopGenerator gosplGenerator = new SampleBasedGenerator(sampler);

		assertEquals(POPULATION_SIZE, gosplGenerator.generate(POPULATION_SIZE).size());
	}

	@Test
	@Ignore
	public void comparisonWithOrWithoutAggregatedSolution() {

		Map<String, IEntitySampler[]> algos = new HashMap<>();

		algos.put("HC", new IEntitySampler[] {
				new CombinatorialOptimizationSampler<>(
						new HillClimbing(POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, false),
				new CombinatorialOptimizationSampler<>(
						new HillClimbing(POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, true)
		});

		algos.put("TABU", new IEntitySampler[] {
				new CombinatorialOptimizationSampler<>(
						new TabuSearch(new TabuList(TABULIST_SIZE), 
								POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, false),
				new CombinatorialOptimizationSampler<>(
						new TabuSearch(new TabuList(TABULIST_SIZE), 
								POPULATION_SIZE * 0.01, MAX_ITERATION), 
						SAMPLE, DATA_BASED_POP, true)
		});

		algos.put("SIM", new IEntitySampler[] {
				new CombinatorialOptimizationSampler<>(
						new SimulatedAnnealing(), SAMPLE,
						DATA_BASED_POP, false),
				new CombinatorialOptimizationSampler<>(
						new SimulatedAnnealing(), SAMPLE,
						DATA_BASED_POP, true)
		});


		double timeWOA = 0d;
		double timeWA = 0d;

		for(int i = 0; i < ITER_COMP; i++) {
			for(String algo : algos.keySet()) {
				IEntitySampler swoa = new SampleBasedAlgorithm()
						.setupCOSampler(SAMPLE, algos.get(algo)[0]);
				swoa.addObjectives(MARGINALS);
				
				IEntitySampler swa = new SampleBasedAlgorithm()
						.setupCOSampler(SAMPLE, algos.get(algo)[1]);
				swa.addObjectives(MARGINALS);
				
				double timeStemp = System.currentTimeMillis();
				new SampleBasedGenerator(swoa).generate(POPULATION_SIZE);
				double cwoa = System.currentTimeMillis() - timeStemp;
				timeWOA = timeWOA == 0d ? cwoa : timeWOA + (cwoa - timeWOA) / i;

				timeStemp = System.currentTimeMillis();
				new SampleBasedGenerator(swa).generate(POPULATION_SIZE);
				double cwa = System.currentTimeMillis() - timeStemp;
				timeWA = timeWA == 0d ? cwa : timeWA + (cwoa - timeWOA) / i;
			}
		}

		System.out.println("Aggregated solution make optimization process "
				+Math.round(Math.round(timeWOA / timeWA * 100))/100d+"x faster !");
		assertTrue(timeWOA > timeWA);

	}
}
