package gospl.algo.ipf.margin;

import java.util.Collection;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

/**
 * Higher order abstraction to build marginals based on sparse matrix with
 * custom T extends Number value type content
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public interface IMarginalsIPFBuilder<T extends Number> {

	public Collection<Margin<T>> buildCompliantMarginals(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T> matrix,
			AFullNDimensionalMatrix<T> seed, boolean parallel);
	
}
