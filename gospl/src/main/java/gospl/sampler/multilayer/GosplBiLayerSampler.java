package gospl.sampler.multilayer;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.GosplMultiLayerCoordinate;

/**
 * A sampler based on SR techniques that will draw a two layer entity, e.g. individuals in household. 
 * 
 * @author kevinchapuis
 *
 */
public class GosplBiLayerSampler implements IMultiLayerSampler {
	
	private INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> baseDistribution;
	private INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> bottomDistribution;

	@Override
	public GosplMultiLayerCoordinate draw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GosplMultiLayerCoordinate> draw(int numberOfDraw) {
		return IntStream.range(0,numberOfDraw).mapToObj(i -> this.draw()).collect(Collectors.toList());
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 *\p
	 * If no distribution have been provided, the first one will represent the top layer and
	 * associated to level 0 (int). If any layer with a lower level is added, it will automatically
	 * become the top layer. 
	 */
	@Override
	public void addDistribution(int level,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution) {
		if(baseDistribution == null || level < 0)
			baseDistribution = distribution;
		else
			bottomDistribution = distribution;
	}

}
