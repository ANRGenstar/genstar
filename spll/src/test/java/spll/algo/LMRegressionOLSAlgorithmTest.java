package spll.algo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class LMRegressionOLSAlgorithmTest {

	private static RegressionSetup rs;
	private static double DELTA = 0.001;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rs = RegressionSetup.getInstance();
		rs.setupRandom();
	}

	@Test
	public void testIntercept() {
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		reg.newSampleData(rs.observation, rs.variables);
		double[] coef = reg.estimateRegressionParameters(); 
		System.out.println(Arrays.toString(coef));
		double[] corr = reg.estimateResiduals();
		System.out.println(Arrays.toString(corr));
		for(int i = 0; i < rs.observation.length; i++){
			double estimatedValue = coef[0];
			for(int j = 0; j < rs.variables[i].length; j++)
				estimatedValue += rs.variables[i][j] * coef[j+1];
			assertEquals(rs.observation[i], estimatedValue + corr[i], DELTA * rs.observation[i]);
		}
	}
	
	@Test
	public void testNoIntercept(){
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		reg.setNoIntercept(true);
		reg.newSampleData(rs.observation, rs.variables);
		double[] coef = reg.estimateRegressionParameters(); 
		System.out.println(Arrays.toString(coef));
		double[] corr = reg.estimateResiduals();
		System.out.println(Arrays.toString(corr));
		for(int i = 0; i < rs.observation.length; i++){
			double estimatedValue = 0d;
			for(int j = 0; j < rs.variables[i].length; j++)
				estimatedValue += rs.variables[i][j] * coef[j];
			assertEquals(rs.observation[i], estimatedValue + corr[i], DELTA * rs.observation[i]);
		}
	}
	
	@Test
	@Ignore
	public void testSpllRegression() {
		
	}


}
