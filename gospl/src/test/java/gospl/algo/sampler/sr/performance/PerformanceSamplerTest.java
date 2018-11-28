package gospl.algo.sampler.sr.performance;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import gospl.GosplEntity;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.generator.util.GSUtilGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.sr.GosplAliasSampler;
import gospl.sampler.sr.GosplBasicSampler;
import gospl.sampler.sr.GosplBinarySampler;

public class PerformanceSamplerTest {

	GSUtilPopulation gsup;
	
	GosplAliasSampler gas;
	GosplBasicSampler gbass;
	GosplBinarySampler gbis;
	
	static public int POPSIZE = (int) Math.pow(10, 8);
	static public double PRECISION = Math.pow(10, 4);
	
	@Before
	public void setup() {
		gsup = new GSUtilPopulation(new GSUtilGenerator(2, 5).generate(100));
		AFullNDimensionalMatrix<Double> distribution = gsup.getFrequency();
		
		gas = new GosplAliasSampler();
		gas.setDistribution(distribution);
		
		gbass = new GosplBasicSampler();
		gbass.setDistribution(distribution);
		
		gbis = new GosplBinarySampler();
		gbis.setDistribution(distribution);
	}
	
	@Test
	public void performanceTest() {
		
		System.out.println("\n-----------------------------\n"
				+ "PERFOMANCE ASSESMENT FOR GOSPL SAMPLER"
				+ "\n-----------------------------\n");
		
		double temp = System.currentTimeMillis();
		IntStream.range(0, POPSIZE).forEach(i -> new GosplEntity(gas.draw().getMap()));
		double secTime = (System.currentTimeMillis() - temp) / 1000;
		double gasExecTime = Math.round(secTime * PRECISION) / PRECISION;
		System.out.println("Gospl alias sampler: "
				+gasExecTime+"s. for "+POPSIZE+" draws");
		
		temp = System.currentTimeMillis();
		IntStream.range(0, POPSIZE).forEach(i -> new GosplEntity(gbass.draw().getMap()));
		secTime = (System.currentTimeMillis() - temp) / 1000;
		double gbassExecTime = Math.round(secTime * PRECISION) / PRECISION;
		System.out.println("Gospl basic sampler: "
				+gbassExecTime+"s. for "+POPSIZE+" draws");
		
		temp = System.currentTimeMillis();
		IntStream.range(0, POPSIZE).forEach(i -> new GosplEntity(gbis.draw().getMap()));
		secTime = (System.currentTimeMillis() - temp) / 1000;
		double gbisExecTime = Math.round(secTime * PRECISION) / PRECISION;
		System.out.println("Gospl binary sampler: "
				+gbisExecTime+"s. for "+POPSIZE+" draws");
		
	}
	
}
