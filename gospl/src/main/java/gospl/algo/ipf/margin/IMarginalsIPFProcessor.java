package gospl.algo.ipf.margin;

import java.util.Collection;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

public interface IMarginalsIPFProcessor<T extends Number> {

	public Collection<AMargin<T>> buildCompliantMarginals(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix,
			AFullNDimensionalMatrix<T> seed, boolean parallel);
	
}
