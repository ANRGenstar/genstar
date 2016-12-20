package gospl.algo.ipf;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.GosplPopulation;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.algo.sampler.sr.GosplBasicSampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UtilGenerator;

public class GosplIPFTest {
	
	public static double SEED_RATIO = Math.pow(10, -2);
	public static int POPULATION_SIZE = (int) Math.pow(10, 5);
	public static int GENERATION_SIZE = 1000;

	public static IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> objectif;
	public static IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed;
	public static Set<APopulationAttribute> attributes;
	
	public static INDimensionalMatrix<APopulationAttribute,APopulationValue,Double> marginals;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GosplAttributeFactory gaf = new GosplAttributeFactory();
		attributes = new HashSet<>();
		attributes.add(gaf.createAttribute("Genre", GSEnumDataType.String, 
				Arrays.asList("Homme", "Femme"), GSEnumAttributeType.unique));
		attributes.add(gaf.createAttribute("Age", GSEnumDataType.Integer, 
				Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus"), GSEnumAttributeType.range));
		attributes.add(gaf.createAttribute("Couple", GSEnumDataType.Boolean, 
				Arrays.asList("oui", "non"), GSEnumAttributeType.unique));
		/*
		attributes.add(gaf.createAttribute("Education", GSEnumDataType.String, 
				Arrays.asList("pre-bac", "bac", "licence", "master et plus"), GSEnumAttributeType.unique));
		attributes.add(gaf.createAttribute("Activité", GSEnumDataType.String, 
				Arrays.asList("inactif", "chomage", "employé", "fonctionnaire", "indépendant", "retraité"), GSEnumAttributeType.unique));
		*/
		
		ISyntheticGosplPopGenerator generator = new UtilGenerator(attributes);
		objectif = generator.generate(POPULATION_SIZE);
		
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
