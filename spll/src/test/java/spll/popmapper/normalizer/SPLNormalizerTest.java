package spll.popmapper.normalizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Test;

import core.util.stats.GSBasicStats;
import core.util.stats.GSEnumStats;
import spll.datamapper.normalizer.ASPLNormalizer;
import spll.datamapper.normalizer.SPLUniformNormalizer;

public class SPLNormalizerTest {

	private static int nbTest = 1000;

	private static float[][] testMatrix;
	private static int bound = 100;
	private static double valueMin = -20d;
	private static double valueMax = 50d;
	private static int objectif;

	private static double floor = 0d;
	private static Number noData = valueMin-1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testMatrix = new float[ThreadLocalRandom.current().nextInt(bound/10, bound)][];
		int column = ThreadLocalRandom.current().nextInt(bound/10, bound);

		// Fill in the matrix
		for(int i = 0; i < testMatrix.length; i++){
			testMatrix[i] = new float[column];
			for(int j = 0; j < testMatrix[i].length; j++){
				testMatrix[i][j] = (float) ThreadLocalRandom.current().nextDouble(valueMin, valueMax);
			}
		}

		// Put some noData value in matrix
		IntStream.range(0, testMatrix.length).forEach(col -> 
		new Random().ints(1+testMatrix[col].length/10, 0, testMatrix[col].length).forEach(row -> 
		testMatrix[col][row] = noData.floatValue()));

		// Compute sum of values (noData is not included)
		objectif = GSBasicStats.transpose(testMatrix).parallelStream()
				.filter(val -> val != noData.floatValue()).reduce(0d, Double::sum).intValue();
		objectif += objectif * 0.5;
	}

	@Test
	public void test() { 
		long nbNoData = GSBasicStats.transpose(testMatrix)
				.parallelStream().filter(val -> noData.floatValue() == val).count();

		ASPLNormalizer splNorm = new SPLUniformNormalizer(floor, noData);
		splNorm.normalize(testMatrix, objectif);
		this.doTest(testMatrix, nbNoData); 
		splNorm.round(testMatrix, objectif);
		//this.doTest(testMatrix, nbNoData);
	}

	@Test
	public void testNTime(){
		IntStream.range(0, nbTest).forEach(i -> test());
	}

	private void doTest(float[][] matrix, long nbNoData){
		GSBasicStats<Double> gsbs = new GSBasicStats<>(GSBasicStats.transpose(matrix), Arrays.asList(noData.doubleValue()));

		// Test if min value is equal to define bottom
		int min = (int) gsbs.getStat(GSEnumStats.min)[0];
		assertTrue("Test if minimum value "+min+" is greater than or equal to bottom "+floor, (int)floor <= min);

		// Test if (the number of) noData have been left untouched
		long finalNoData = gsbs.getNoDataCount();
		assertEquals(nbNoData, finalNoData);

		// Test if summed value equals to objectif 
		int sum = (int) gsbs.getStat(GSEnumStats.sum)[0];
		assertEquals("Test if summed value is equal to the defined objectif", objectif, sum, 0.0005 * objectif);	
	}

}
