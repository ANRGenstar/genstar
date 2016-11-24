package spll.datamapper.normalizer;

import java.util.Map;

import core.io.geo.entity.GSFeature;

/**
 * TODO: make top value control, not just floor value 
 * 
 * @author kevinchapuis
 *
 */
public abstract class ASPLNormalizer {
	
	protected static final int ITER_LIMIT = 1000000;
	protected static final double EPSILON = 0.001;
	public static boolean LOGSYSO = true;
	
	protected double floorValue;
	
	protected Number noData;
	
	/**
	 * TODO: javadoc
	 * 
	 * @param floorValue
	 * @param noData
	 */
	public ASPLNormalizer(double floorValue, Number noData){
		this.floorValue = floorValue;
		this.noData = noData;
	}
	
	/**
	 * Normalize the content of a pixel format spll output <br>
	 * HINT: {@code float} type is forced by Geotools implementation of raster file
	 * 
	 * 
	 * @param matrix
	 * @param output
	 * @return
	 */
	public abstract float[][] normalize(float[][] matrix, float output);
	
	/**
	 * Round the value of pixels to fit integer value (stay in float format)
	 * 
	 * @param matrix
	 * @param output
	 * @return
	 */
	public abstract float[][] round(float[][] matrix, float output);
	
	/**
	 * TODO
	 * 
	 * @param featureOutput
	 * @param output
	 * @return
	 */
	public abstract Map<GSFeature, Double> normalize(Map<GSFeature, Double> featureOutput, double output);
	
	/**
	 * Round double values to integer and control sum to fit required output
	 * 
	 * @param featureOutput
	 * @param output
	 * @return
	 */
	public abstract Map<GSFeature, Integer> round(Map<GSFeature, Double> featureOutput, double output);
	
	// ------------------ shared utility ------------------ //
	
	protected boolean equalEpsilon(float value, double target) {
		return Math.abs(value - target) < EPSILON ? true : false;
	}

	protected boolean equalEpsilon(double value, double target) {
		return Math.abs(value - target) < EPSILON ? true : false;
	}
	
}
