package gospl.algo.is;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import gospl.algo.GosplAlgoUtilTest;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.sr.GosplBasicSampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;

public class IndependentHypothesisAlgoTest {
	
	public static ASegmentedNDimensionalMatrix<Double> partialDistribution; 
	
	public static int SEGMENT_SIZE = 10000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		partialDistribution = new GosplAlgoUtilTest().getSegmentedFrequency(SEGMENT_SIZE);
	}

	@Test
	public void test() {
		assertTrue(partialDistribution.isSegmented());
		assertTrue(!partialDistribution.getMatrix().isEmpty());
		
		System.out.println(partialDistribution.toString());
		
		IndependantHypothesisAlgo isAlgo = new IndependantHypothesisAlgo();
		IDistributionSampler aggSampler = new GosplBasicSampler();
		IDistributionSampler valSampler = new GosplBasicSampler();
		
		try {
			isAlgo.inferSRSampler(partialDistribution, valSampler);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			isAlgo.inferSRSamplerWithReferentProcess(partialDistribution, aggSampler);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: compare the two resulted sampler
	}

}
