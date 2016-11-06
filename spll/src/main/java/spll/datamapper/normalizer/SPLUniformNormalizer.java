package spll.datamapper.normalizer;

import java.util.Map;
import java.util.stream.IntStream;

import core.io.geo.entity.GSFeature;
import core.util.GSPerformanceUtil;

public class SPLUniformNormalizer extends ASPLNormalizer {
	
	private double overload = 0d;
	private int adjustableValue = 0;

	private GSPerformanceUtil gspu;
	private int countPerformance = 0;
	
	public SPLUniformNormalizer(double floorValue, boolean isInteger) {
		super(floorValue, isInteger);
		this.gspu = new GSPerformanceUtil("Normalize regression data to fit output format", LOGSYSO);
	}

	@Override
	public float[][] normalize(float[][] pixelOutput, float output) {
		gspu.setObjectif(output);
		// floor normalization: values below floorValue are up scale and residue are summed
		IntStream.range(0, pixelOutput.length).parallel()
			.forEach(col -> IntStream.range(0, pixelOutput[col].length)
				.forEach(row -> pixelOutput[col][row] = normalizedFloor(pixelOutput[col][row]))
		);
		// summed residue is spread to all non floor value
		IntStream.range(0, pixelOutput.length).unordered().parallel()
			.forEach(col -> IntStream.range(0, pixelOutput[col].length).unordered()
					.forEach(row -> pixelOutput[col][row] = normalizedOverload(pixelOutput[col][row]))
		);
		countPerformance = 0;
		return pixelOutput;
	}

	@Override
	public Map<GSFeature, Double> normalize(Map<GSFeature, Double> featureOutput, double output) {
		// floor normalization: values below floorValue are up scale and residue are summed
		featureOutput.keySet().parallelStream()
			.forEach(feature -> featureOutput.put(feature, normalizedFloor(featureOutput.get(feature))));
		// summed residue is spread to all non floor value
		featureOutput.keySet().parallelStream()
			.forEach(feature -> featureOutput.put(feature, normalizedOverload(featureOutput.get(feature))));
		return featureOutput;
	}
	
	// ---------------------- inner utility ---------------------- //

	private float normalizedFloor(float value) {
		if(value < floorValue){
			this.overload += Math.abs(value - floorValue);
			return (float) floorValue;
		} else
			this.adjustableValue++;
		return value;
	}
	
	private double normalizedFloor(double value) {
		if(value < floorValue){
			overload += Math.abs(value - floorValue);
			return floorValue;
		} else
			this.adjustableValue++;
		return value;
	}
	
	private float normalizedOverload(float value){
		//syso performance
		if(++countPerformance % (gspu.getObjectif() * 0.1) == 0d)
			gspu.sysoStempPerformance(0.1, this);
		//algo
		if(super.equalEpsilon(value, super.floorValue))
			return value;
		float newVal;
		if(isInteger){
			if(overload > 0d)
				newVal = (int) value + 1;
			else
				newVal = (int) value;
			this.overload -= newVal - value;
		} else {
			newVal = (float) (value + overload / adjustableValue);
		}
		return newVal;
	}
	
	private double normalizedOverload(double value){
		if(super.equalEpsilon(value, super.floorValue))
			return value;
		double newVal;
		if(isInteger){
			if(overload > 0)
				newVal = (int) value + 1d;
			else
				newVal = (int) value;
		} else {
			newVal = value + overload / adjustableValue;
		}
		return newVal;
	}
	
}
