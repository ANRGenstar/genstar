package gospl.sampler.multilayer;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.GosplMultiLayerCoordinate;
import gospl.sampler.ISampler;

/**
 * SR based abstract sampler for several layer of synthetic population
 * 
 * @author kevinchapuis
 *
 */
public interface IMultiLayerSampler extends ISampler<GosplMultiLayerCoordinate> {

	/**
	 * Add new layer distribution data
	 * 
	 * @param level
	 * @param distribution
	 */
	public void addDistribution(int level,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution
			);
	
}
