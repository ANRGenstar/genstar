package gospl.algo.sampler.evaluation;

import gospl.distribution.matrix.AFullNDimensionalMatrix;

public class SamplingEvaluationUtils {

	public static double computeMeanSquarredError(
			AFullNDimensionalMatrix<Double> ref, 
			AFullNDimensionalMatrix<Double> computed
			) {
				
		double sumSquaredErrors = ref.getMatrix().entrySet().stream()
				// compute the squared error
				.mapToDouble(e -> Math.pow(e.getValue().getValue() - computed.getVal(e.getKey()).getValue(), 2) )
				// sum them
				.sum()
				;
		
		ref.getMatrix().entrySet().stream()
				.forEach(
						e -> 
						System.err.println(
								"diff "+e.getKey()+
								" / "+e.getValue().getValue()+
								" "+computed.getVal(e.getKey()).getValue()
								) )
				;
		
		return sumSquaredErrors/ref.getMatrix().size();
		
	}
	
	private SamplingEvaluationUtils() {
	}

}
