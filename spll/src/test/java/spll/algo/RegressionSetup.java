package spll.algo;

import java.util.concurrent.ThreadLocalRandom;

public class RegressionSetup {

	public static int BOUND = 100; 
	
	public double[] observation;
	public double[][] variables;
	
	private static RegressionSetup INSTANCE = new RegressionSetup();
	
	private RegressionSetup() {}
	
	public static RegressionSetup getInstance() {
		return INSTANCE;
	}
	
	
	// ---------------- utilities ---------------- //
	
	public void setupRandom(){
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
	
	public void setupPerfect(){
		observation =  new double[]{2,4,6};
		variables = new double[][]{{1, 2},{2, 4},{3, 6}};
	}
	
	public double computeEstimation(double x, double[] coef){
		double result = 0;
	    for(int i = 0; i < coef.length; ++i){
	        result += coef[i] * Math.pow(x, i);
	    }
	    return result;
	}
	
}
