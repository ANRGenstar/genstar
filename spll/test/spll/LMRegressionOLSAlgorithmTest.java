package spll;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.junit.BeforeClass;
import org.junit.Test;

public class LMRegressionOLSAlgorithmTest {

	public static int BOUND = 100; 
	public static double DELTA = 0.001;
	
	public static double[] observation;
	public static double[][] variables;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setupRandom();
	}

	@Test
	public void testIntercept() {
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		reg.newSampleData(observation, variables);
		double[] coef = reg.estimateRegressionParameters(); 
		System.out.println(Arrays.toString(coef));
		double[] corr = reg.estimateResiduals();
		System.out.println(Arrays.toString(corr));
		for(int i = 0; i < observation.length; i++){
			double estimatedValue = coef[0];
			for(int j = 0; j < variables[i].length; j++)
				estimatedValue += variables[i][j] * coef[j+1];
			assertEquals(observation[i], estimatedValue + corr[i], DELTA * observation[i]);
		}
	}
	
	@Test
	public void testNoIntercept(){
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		reg.setNoIntercept(true);
		reg.newSampleData(observation, variables);
		double[] coef = reg.estimateRegressionParameters(); 
		System.out.println(Arrays.toString(coef));
		double[] corr = reg.estimateResiduals();
		System.out.println(Arrays.toString(corr));
		for(int i = 0; i < observation.length; i++){
			double estimatedValue = 0d;
			for(int j = 0; j < variables[i].length; j++)
				estimatedValue += variables[i][j] * coef[j];
			assertEquals(observation[i], estimatedValue + corr[i], DELTA * observation[i]);
		}
	}
	
	// ---------------- utilities ---------------- //
	
	private static void setupRandom(){
		ThreadLocalRandom tlr = ThreadLocalRandom.current();
		observation = new double[]{tlr.nextInt(BOUND)/5d, tlr.nextInt(BOUND)/4d, tlr.nextInt(BOUND)/3d, tlr.nextInt(BOUND)/2d, tlr.nextInt(BOUND)};
		variables = new double[][]{
			{tlr.nextInt(BOUND)/10d, tlr.nextInt(BOUND)/5d, tlr.nextInt(BOUND)/2d},
			{tlr.nextInt(BOUND)/5d, tlr.nextInt(BOUND)/4d, tlr.nextInt(BOUND)/1.5},
			{tlr.nextInt(BOUND)/4d, tlr.nextInt(BOUND)/3d, tlr.nextInt(BOUND)/1.3},
			{tlr.nextInt(BOUND)/3d, tlr.nextInt(BOUND)/2d, tlr.nextInt(BOUND)/1.1},
			{tlr.nextInt(BOUND)/2d, tlr.nextInt(BOUND), tlr.nextInt(BOUND)}
			};
	}
	
	@SuppressWarnings("unused")
	private static void setupPerfect(){
		observation =  new double[]{2,4,6};
		variables = new double[][]{{1, 2},{2, 4},{3, 6}};
	}
	
	@SuppressWarnings("unused")
	private static double computeEstimation(double x, double[] coef){
		double result = 0;
	    for(int i = 0; i < coef.length; ++i){
	        result += coef[i] * Math.pow(x, i);
	    }
	    return result;
	}

}
