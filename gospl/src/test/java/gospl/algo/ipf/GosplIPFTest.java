package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Ignore;
import org.junit.Test;

import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;
import gospl.sampler.sr.GosplBasicSampler;

public class GosplIPFTest {

	public static double SEED_RATIO = Math.pow(10, -1);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;
	public static int SEGMENTATION = 3;
	
	public static Path PATH_TO_DICO = FileSystems.getDefault().getPath("src","test","resources","attributedictionary");

	@Test
	public void simpleTest() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation("simpleDictionary.gns")
				.buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = 
				new GosplNDimensionalMatrixFactory().createDistribution(
						new GSUtilPopulation("simpleDictionary.gns").buildPopulation(POPULATION_SIZE));

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}

	@Test
	@Ignore
	public void defaultTest() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = null;
		try {
			seed = new GSUtilPopulation().buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			marginals = new GosplNDimensionalMatrixFactory().createDistribution(
					new GSUtilPopulation().buildPopulation(POPULATION_SIZE));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	@Test
	@Ignore
	public void defaultTestWithSegmentedMatrix() {

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = null;
		try {
			seed = new GSUtilPopulation().buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);

		}

		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			GSUtilPopulation gaut = new GSUtilPopulation();
			gaut.buildPopulation(POPULATION_SIZE);
			marginals = gaut.getSegmentedFrequency(SEGMENTATION);
		} catch (IllegalDistributionCreation | GSIllegalRangedData e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	
	
	@Test
	@Ignore
	public void mappedTest() {

		Set<DemographicAttribute<? extends IValue>> refAttributes = new HashSet<>();
		Set<DemographicAttribute<? extends IValue>> mappedAttributes = new HashSet<>();
		try {
			@SuppressWarnings("unchecked")
			DemographicDictionary<DemographicAttribute<? extends IValue>> gju = new GenstarJsonUtil()
					.unmarshalFromGenstarJson(PATH_TO_DICO.resolve("withMapDictionary.gns"), 
					DemographicDictionary.class);
			refAttributes.addAll(gju.getAttributes().stream()
					.map(DemographicAttribute::getReferentAttribute)
				.collect(Collectors.toSet()));
			mappedAttributes.addAll(gju.getAttributes().stream()
					.filter(a -> !a.getReferentAttribute().equals(a))
					.collect(Collectors.toSet()));
			mappedAttributes.addAll(refAttributes.stream()
					.filter(a -> mappedAttributes.stream().noneMatch(ma -> ma.getReferentAttribute().equals(a)))
					.collect(Collectors.toSet()));
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation(refAttributes).buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));

		AFullNDimensionalMatrix<Double> marginals = 
				new GosplNDimensionalMatrixFactory().createDistribution(
					new GSUtilPopulation(mappedAttributes).buildPopulation(POPULATION_SIZE));

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		final GosplSurveyFactory sf = new GosplSurveyFactory(0, ';', 1, 1);
		try {
			sf.createSummary(PATH_TO_DICO.getParent().resolve("outputTest.csv").toFile(), GSSurveyType.GlobalFrequencyTable, popOut);
			sf.createSummary(PATH_TO_DICO.getParent().resolve("inputTest.csv"), marginals);
		} catch (InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// Basic test of population size generation
		assertEquals(GENERATION_SIZE, popOut.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void mappedWithSegmentedTest() {

		DemographicDictionary<DemographicAttribute<? extends IValue>> gju = null;
		try {
			gju = new GenstarJsonUtil()
					.unmarshalFromGenstarJson(PATH_TO_DICO.resolve("withMapDictionary.gns"), 
					DemographicDictionary.class);
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		Set<DemographicAttribute<? extends IValue>> refAttributes = gju.getAttributes().stream()
				.map(DemographicAttribute::getReferentAttribute)
			.collect(Collectors.toSet());

		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed = 
				new GSUtilPopulation(refAttributes).buildPopulation((int)(POPULATION_SIZE * SEED_RATIO));
		
		INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> marginals = null;
		try {
			GSUtilPopulation gaut = new GSUtilPopulation(gju);
			gaut.buildPopulation(POPULATION_SIZE);
			marginals = gaut.getSegmentedFrequency(SEGMENTATION);
		} catch (IllegalDistributionCreation e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		}
		
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popOut = doIPF(seed, marginals);

		final GosplSurveyFactory sf = new GosplSurveyFactory(0, ';', 1, 1);
		try {
			sf.createSummary(
					PATH_TO_DICO.getParent().resolve("outputTest.csv").toFile(), 
					GSSurveyType.GlobalFrequencyTable, 
					popOut);
			sf.createSummary(
					PATH_TO_DICO.getParent().resolve("inputTest.csv"), 
					marginals);
		} catch (InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		ISyntheticGosplPopGenerator gosplGenerator = new DistributionBasedGenerator(sampler);
		return gosplGenerator.generate(GENERATION_SIZE);
	}
	
	

}
