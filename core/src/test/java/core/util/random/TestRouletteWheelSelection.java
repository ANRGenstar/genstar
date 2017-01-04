package core.util.random;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import core.util.random.roulette.ARouletteWheelSelection;
import core.util.random.roulette.RouletteWheelSelectionFactory;

public class TestRouletteWheelSelection {

	private Logger logger = LogManager.getLogger();
	
	private <X extends Number> void testDistribution(List<X> distribution, int samples, double epsilon) {

		logger.debug("required wheel {}", distribution);
		
		ARouletteWheelSelection<X> roulette = RouletteWheelSelectionFactory.getRouletteWheel(distribution);
		
		final double totalRequired =  distribution.stream().collect(Collectors.summingDouble(n -> n.doubleValue()));

		//logger.debug("required distribution {}", distribution.stream().map(n->n.doubleValue()/totalRequired).collect(Collectors.toList()));

		// count the amount of occurences
		int[] occurences = new int[distribution.size()];		
		for (int i=0; i<samples; i++) {		
			int idx = roulette.drawIndex();
			occurences[idx]++;
		}
		
		// 
		logger.debug("dist {}", occurences);
		List<Double> obtained = new ArrayList<>(distribution.size());
		final double totalObtained =  distribution.stream().collect(Collectors.summingDouble(n -> n.doubleValue()));
		for (int i=0;i<occurences.length;i++) {
			obtained.add((double)occurences[i]/(double)samples*totalObtained);
		}
		logger.debug("obtained distribution {}", obtained);

		for (int i=0; i<occurences.length; i++) {
			if (Math.abs(obtained.get(i)/totalObtained-distribution.get(i).doubleValue()/totalRequired) > epsilon) {
				fail("difference between expectation "+distribution+" and obtained distributions "+obtained+" is too high");
			}
			
		}
	}
	
	@Test
	public void testIndexDistributionInDouble() {
		
		List<Double> l = Arrays.asList(0.5,0.3,0.2,10.0);
		testDistribution(l,10000,0.01);

	}
	
	@Test
	public void testIndexDistributionInDoubleLong() {
		
		List<Double> l = new ArrayList<>();
		for (int i=0; i<100; i++)
			l.add((double)i/1000);
		testDistribution(l,100000,0.01);

	}


	@Test
	public void testIndexDistributionInInteger() {
		
		List<Integer> l = Arrays.asList(12,28,32,46);
		testDistribution(l,10000,0.01);

	}

}
 