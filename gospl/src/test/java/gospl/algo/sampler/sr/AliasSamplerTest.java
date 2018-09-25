package gospl.algo.sampler.sr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import core.util.GSPerformanceUtil;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.sr.GosplAliasSampler;
import gospl.validation.GosplIndicatorFactory;

public class AliasSamplerTest {

	SamplerTestSetup sts;
	GosplAliasSampler gas;
	
	public static double DELTA = Math.pow(10, -2); 
	
	@Before
	public void setup() {
		sts = new SamplerTestSetup();
		gas = new GosplAliasSampler();
		gas.setDistribution(sts.uPop.getFrequency());
	}
	
	@Test
	public void test() {

		GSPerformanceUtil gspu = new GSPerformanceUtil("Test alias sampler", Level.DEBUG);
		gspu.sysoStempMessage("Start sampling population of size "+sts.popSize);
		
		GosplPopulation res = new GosplPopulation(IntStream.range(0, sts.popSize)
				.parallel()
				.mapToObj(i -> new GosplEntity(gas.draw().getMap()))
				.collect(Collectors.toList()));
		
		gspu.sysoStempMessage("Population sampled");
		
		GSUtilPopulation gup = new GSUtilPopulation(res);
		
		gspu.sysoStempMessage("Start assesing sampling quality");
		
		AFullNDimensionalMatrix<Double> actualDistribution = sts.uPop.getFrequency();
		AFullNDimensionalMatrix<Double> sampleDistribution = gup.getFrequency();
		
		//System.out.println(actualDistribution);
		//System.out.println(sampleDistribution);
		
		double aapdError = GosplIndicatorFactory.getFactory().getAAPD(actualDistribution, res);
		double taeError = GosplIndicatorFactory.getFactory().getDoubleTAE(actualDistribution, sampleDistribution);
		
		gspu.sysoStempMessage("Sampling Average Absolute Percentage Difference (AAPD): "+aapdError);
		gspu.sysoStempMessage("Sampling Total Absolute Error (TAE): "+taeError);
		
		assertThat(aapdError, lessThan(sts.popSize * DELTA));
		assertThat(taeError, lessThan(actualDistribution.size() * DELTA));
		
	}

}
