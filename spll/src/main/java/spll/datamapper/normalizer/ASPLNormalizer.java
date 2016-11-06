package spll.datamapper.normalizer;

import java.util.Map;

import core.io.geo.entity.GSFeature;

public abstract class ASPLNormalizer {
	
	private static double EPSILON = 10^-4;
	public static boolean LOGSYSO = true;
	
	protected double floorValue;
	protected double overload;
	
	protected boolean isInteger;
	
	public ASPLNormalizer(double floorValue, boolean isInteger){
		this.floorValue = floorValue;
		this.isInteger = isInteger;
	}
	
	/**
	 * Normalize the content of a pixel format spll output, that is it forced 
	 * 
	 * HINT: {@code float} type is forced by Geotools implementation of raster file
	 * 
	 * WARNING: parallel implementation
	 * 
	 * @param pixelOutput
	 * @param output
	 * @return
	 */
	public abstract float[][] normalize(float[][] pixelOutput, float output);
	
	public abstract Map<GSFeature, Double> normalize(Map<GSFeature, Double> featureOutput, double output);
	
	// ------------------ shared utility ------------------ //
	
	protected boolean equalEpsilon(float value, double target) {
		return Math.abs(value - target) < EPSILON ? true : false;
	}

	protected boolean equalEpsilon(double value, double target) {
		return Math.abs(value - target) < EPSILON ? true : false;
	}
	
}
