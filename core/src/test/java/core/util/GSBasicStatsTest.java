package core.util;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import core.util.stats.GSBasicStats;
import core.util.stats.GSEnumStats;

public class GSBasicStatsTest {
	
	private static final int NB_OF_DRAW = 10000; 
	private static final double EPSILON = 0.02;
	
	private static final int ARRAY_MAX_SIZE = 100;
	private static final int ARRAY_MIN_SIZE = 10;
	
	private static final int MAXIMUM = 100;
	private static final int MINIMUM = 0;
	
	private static float[][] testPixelMatrix;

	@Test
	public void test() {
		EnumMap<GSEnumStats, Double> output = new EnumMap<>(GSEnumStats.class);
		for(int i = 0; i < NB_OF_DRAW; i++){
			GSBasicStats<Double> basicStat = new GSBasicStats<>(GSBasicStats.transpose(this.fillMatrix()));
			basicStat.setFloatingPrecision(0.1);
			for(GSEnumStats stat : GSEnumStats.values()){
				double avStat = basicStat.getStat(stat)[0];
				if(output.containsKey(stat))
					avStat = output.get(stat) + (avStat - output.get(stat)) / i;
				output.put(stat, avStat);
			}
			if(i % (NB_OF_DRAW * 0.1) == 0d){
				System.out.println("Draw n°"+i+":\n"
						+ Arrays.toString(GSEnumStats.values())
						+"\n"+Arrays.toString(Arrays.asList(GSEnumStats.values())
								.stream().mapToDouble(stat -> output.get(stat)).toArray()));
			}
		}
		for(GSEnumStats stat : GSEnumStats.values())
			Assert.assertTrue("Statistic "+stat+" expected to be "+getStatRequirement(stat)
				+" but is "+output.get(stat), getStatValidation(stat, output.get(stat)));
	}

	private float[][] fillMatrix(){
		testPixelMatrix  = new float[ThreadLocalRandom.current().nextInt(ARRAY_MIN_SIZE, ARRAY_MAX_SIZE+1)]
				[ThreadLocalRandom.current().nextInt(ARRAY_MIN_SIZE, ARRAY_MAX_SIZE+1)];
		IntStream.range(0, testPixelMatrix.length).parallel().forEach(x ->
			IntStream.range(0, testPixelMatrix[x].length).forEach(y -> 
				testPixelMatrix[x][y] = ThreadLocalRandom.current().nextInt(MINIMUM, MAXIMUM+1))
		);
		return testPixelMatrix;
	}
	
	private boolean getStatValidation(GSEnumStats stat, double val){
		switch (stat) {
			case av: return Math.abs(val - getStatRequirement(GSEnumStats.av)) < MAXIMUM * EPSILON ? true : false;
			case med: return Math.abs(val - getStatRequirement(GSEnumStats.med)) < MAXIMUM * EPSILON ? true : false;
			case min: return Math.abs(val - getStatRequirement(GSEnumStats.min)) < MAXIMUM * EPSILON ? true : false;
			case max: return Math.abs(val - getStatRequirement(GSEnumStats.max)) < MAXIMUM * EPSILON ? true : false;
			case sum: return Math.abs(val - getStatRequirement(GSEnumStats.sum)) < getStatRequirement(GSEnumStats.sum) * EPSILON ? true : false;
			case q_one: return Math.abs(val - getStatRequirement(GSEnumStats.q_one)) < MAXIMUM * EPSILON ? true : false;
			case q_two: return Math.abs(val - getStatRequirement(GSEnumStats.q_two)) < MAXIMUM * EPSILON ? true : false;
			case q_three: return Math.abs(val - getStatRequirement(GSEnumStats.q_three)) < MAXIMUM * EPSILON ? true : false;
			case q_four: return Math.abs(val - getStatRequirement(GSEnumStats.q_four)) < MAXIMUM * EPSILON ? true : false;
		}
		return false;
	}
	
	private double getStatRequirement(GSEnumStats stat){
		switch (stat) {
			case av: return MAXIMUM / 2d;
			case med: return MAXIMUM / 2d;
			case min: return MINIMUM;
			case max: return MAXIMUM;
			case sum: return (MAXIMUM / 2d) * Math.pow((ARRAY_MIN_SIZE+ARRAY_MAX_SIZE)/2d, 2d); 
			case q_one: return MAXIMUM / 5d;
			case q_two: return MAXIMUM * 2 / 5d;
			case q_three: return MAXIMUM * 3 / 5d;
			case q_four: return MAXIMUM * 4 / 5d;
		}
		return -9999d;
	}
	
}
