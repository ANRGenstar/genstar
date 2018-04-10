package gospl.algo.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.algo.sr.ds.DirectSamplingAlgo;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.sr.GosplBasicSampler;

public class DirectSamplingAlgoTest {
	
	public static ASegmentedNDimensionalMatrix<Double> partialDistribution; 
	
	public static int SEGMENT_SIZE = 100000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		partialDistribution = new GSUtilPopulation().getSegmentedFrequency(SEGMENT_SIZE);
	}

	@Test
	@Ignore
	public void test() {
		assertTrue(partialDistribution.isSegmented());
		assertTrue(!partialDistribution.getMatrix().isEmpty());
		
		System.out.println(partialDistribution.toString());
		
		IDistributionSampler sampler = new GosplBasicSampler();
		ISyntheticGosplPopGenerator generator = new DistributionBasedGenerator(sampler);
		ISyntheticReconstructionAlgo<IDistributionSampler> isAlgo = new DirectSamplingAlgo();
		
		try {
			isAlgo.inferSRSampler(partialDistribution, sampler);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> pop = generator.generate(SEGMENT_SIZE);

		assertEquals(pop.size(), SEGMENT_SIZE, 0.01);
		
	}

}
