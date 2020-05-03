package gospl.algo.sampler.sr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import gospl.GosplPopulation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.sr.GosplAliasSampler;
import gospl.sampler.sr.GosplBasicSampler;
import gospl.sampler.sr.GosplBinarySampler;
import gospl.validation.GosplIndicatorFactory;

/**
 * Sampler test make assumption like: 10^-2 error is acceptable and the more you draw
 * better should be the result
 * 
 * @author kevinchapuis
 *
 */
public class GosplSamplerTest {
	
	public static double DELTA = Math.pow(10, -2); 
	public static int POPSIZE = (int) Math.pow(10, 4);
	
	@Test
	public void testAlias() {
		
		SamplerTestSetup<GosplAliasSampler> sts = new SamplerTestSetup<>(new GosplAliasSampler());
		AFullNDimensionalMatrix<Double> actualDistribution = sts.getBasePopulationUtil().getFrequency();
		
		Map<Integer, GosplPopulation> pops = this.getPops(sts, 10, 100, 1000);
		
		System.out.println("----------------\n"
				+ "Setup Alias sampling method"
				+ "\n----------------");
		
		double aapdError = GosplIndicatorFactory.getFactory().getAAPD(actualDistribution, pops.get(0));
		System.out.println("Average Absolute Percentage Difference (AAPD): "+aapdError);
		assertThat(aapdError, lessThan(POPSIZE * DELTA));
		
		Map<Integer, Double> taeMap = this.getTAE(actualDistribution, pops);

		System.out.println("Total Absolute Error (TAE): "+taeMap.get(0));		
		taeMap.keySet().stream().filter(df -> df != 0)
			.forEach(df -> assertThat(taeMap.get(0), lessThan(taeMap.get(df))));
				
	}

	@Test
	public void testBasic() {
		
		SamplerTestSetup<GosplBasicSampler> sts = new SamplerTestSetup<>(new GosplBasicSampler());
		AFullNDimensionalMatrix<Double> actualDistribution = sts.getBasePopulationUtil().getFrequency();
		
		Map<Integer, GosplPopulation> pops = this.getPops(sts, 10, 100, 1000);
		
		System.out.println("----------------\n"
				+ "Setup Classic Monte Carlo sampling method"
				+ "\n----------------");
		
		double aapdError = GosplIndicatorFactory.getFactory().getAAPD(actualDistribution, pops.get(0));
		System.out.println("Basic Monte Carlo method AAPD: "+aapdError);
		assertThat(aapdError, lessThan(POPSIZE * DELTA));
		
		Map<Integer, Double> taeMap = this.getTAE(actualDistribution, pops);

		System.out.println("Sampling Total Absolute Error (TAE): "+taeMap.get(0));		
		taeMap.keySet().stream().filter(df -> df != 0)
			.forEach(df -> assertThat(taeMap.get(0), lessThan(taeMap.get(df))));
		
	}
	
	@Test
	public void testBinary() {
		
		SamplerTestSetup<GosplBinarySampler> sts = new SamplerTestSetup<>(new GosplBinarySampler());
		AFullNDimensionalMatrix<Double> actualDistribution = sts.getBasePopulationUtil().getFrequency();
		
		Map<Integer, GosplPopulation> pops = this.getPops(sts, 10, 100, 1000);
		
		System.out.println("----------------\n"
				+ "Setup Binary search sampling method"
				+ "\n----------------");
		
		double aapdError = GosplIndicatorFactory.getFactory().getAAPD(actualDistribution, pops.get(0));
		System.out.println("Sampling Average Absolute Percentage Difference (AAPD): "+aapdError);
		assertThat(aapdError, lessThan(POPSIZE * DELTA));
		
		Map<Integer, Double> taeMap = this.getTAE(actualDistribution, pops);

		System.out.println("Sampling Total Absolute Error (TAE): "+taeMap.get(0));		
		taeMap.keySet().stream().filter(df -> df != 0)
			.forEach(df -> assertThat(taeMap.get(0), lessThan(taeMap.get(df))));	
		
	}
	
	private Map<Integer, GosplPopulation> getPops(SamplerTestSetup<?> sampler, int... divisionFactors){
		Map<Integer, GosplPopulation> res = new HashMap<>();
		
		if(!Lists.newArrayList(Ints.asList(divisionFactors)).contains(0))
			res.put(0, sampler.drawPopulation(POPSIZE));
		
		for(int nb : divisionFactors) {
			res.put(nb, sampler.drawPopulation(POPSIZE/nb));
		}
		
		return res;
	}
	
	private Map<Integer, Double> getTAE(AFullNDimensionalMatrix<Double> actualDistribution, 
			Map<Integer, GosplPopulation> pops){
		Map<Integer, Double> taeMap = new HashMap<>();
		
		for(int df : pops.keySet()) {
			taeMap.put(df, GosplIndicatorFactory.getFactory().getDoubleTAE(actualDistribution, 
					new GSUtilPopulation(pops.get(df)).getFrequency()));
		}
		
		return taeMap;
	}
	
}
