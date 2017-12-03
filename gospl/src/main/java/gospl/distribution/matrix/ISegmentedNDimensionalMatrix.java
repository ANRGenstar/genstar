package gospl.distribution.matrix;

import java.util.Collection;
import java.util.Set;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;

public interface ISegmentedNDimensionalMatrix<T extends Number> extends INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T> {

	/**
	 * Return the partitioned view of this matrix, i.e. the collection
	 * of inner full matrices
	 * 
	 * @return
	 */
	public Collection<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> getMatrices();


	/**
	 * Returns the matrices which involve this val
	 * @param val
	 */
	public Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> getMatricesInvolving(DemographicAttribute<? extends IValue> att);

}