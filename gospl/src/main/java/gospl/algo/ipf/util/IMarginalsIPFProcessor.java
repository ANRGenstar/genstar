package gospl.algo.ipf.util;

import java.util.Map;
import java.util.Set;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;

public interface IMarginalsIPFProcessor<T extends Number> {

	public Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> buildCompliantMarginals(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix,
			AFullNDimensionalMatrix<T> seed, boolean parallel);
	
}
