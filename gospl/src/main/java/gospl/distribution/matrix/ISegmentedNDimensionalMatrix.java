package gospl.distribution.matrix;

import java.util.Collection;
import java.util.Set;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;

public interface ISegmentedNDimensionalMatrix<T extends Number> extends INDimensionalMatrix<APopulationAttribute, APopulationValue, T> {

	/**
	 * Return the partitioned view of this matrix, i.e. the collection
	 * of inner full matrices
	 * 
	 * @return
	 */
	public Collection<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatrices();


	/**
	 * Returns the matrices which involve this val
	 * @param val
	 */
	public Set<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatricesInvolving(APopulationAttribute att);

}