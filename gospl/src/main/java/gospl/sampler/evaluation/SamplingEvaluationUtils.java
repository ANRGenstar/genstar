package gospl.sampler.evaluation;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.INDimensionalMatrix;

public class SamplingEvaluationUtils {

	public static double computeMeanSquarredError(
			INDimensionalMatrix<DemographicAttribute<IValue>, IValue, Double> ref, 
			INDimensionalMatrix<DemographicAttribute<IValue>, IValue, Double> computed
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
