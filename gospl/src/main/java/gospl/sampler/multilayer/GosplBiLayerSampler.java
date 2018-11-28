package gospl.sampler.multilayer;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.algo.sr.ds.DirectSamplingAlgo;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.distribution.matrix.coordinate.GosplMultiLayerCoordinate;
import gospl.sampler.ISampler;
import gospl.sampler.sr.GosplBasicSampler;
import gospl.sampler.sr.GosplCompletionDirectSampling;

/**
 * A sampler based on SR techniques that will draw a two layer entity, e.g. individuals in household. 
 * 
 * @author kevinchapuis
 *
 */
public class GosplBiLayerSampler implements IMultiLayerSampler {
	
	private ISampler<ACoordinate<Attribute<? extends IValue>, IValue>> groupSampler;
	private GosplCompletionDirectSampling entitySampler = new GosplCompletionDirectSampling();

	@Override
	public GosplMultiLayerCoordinate draw() {
		GosplMultiLayerCoordinate coord = new GosplMultiLayerCoordinate(groupSampler.draw());
		Map<Attribute<? extends IValue>, IValue> deciders = coord.getMap().keySet().stream()
				.filter(a -> !a.getReferentAttribute().equals(a))
				.collect(Collectors.toMap(Function.identity(), k -> coord.getMap().get(k)));
		coord.addChild(new GosplMultiLayerCoordinate(entitySampler.complete(new GosplCoordinate(deciders))));
		return coord;
	}

	@Override
	public Collection<GosplMultiLayerCoordinate> draw(int numberOfDraw) {
		return IntStream.range(0,numberOfDraw).mapToObj(i -> this.draw()).collect(Collectors.toList());
	}

	/**
	 * Describe the distribution of attribute for group layer
	 * \p
	 * Mandatory in order to draw entities
	 * @param distribution
	 * @throws IllegalDistributionCreation
	 */
	public void setGroupLevelDistribution(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution) throws IllegalDistributionCreation {
		this.groupSampler = new DirectSamplingAlgo().inferSRSampler(distribution, new GosplBasicSampler());
	}

	/**
	 * Describe the distribution of attribute for entity layer
	 * \p
	 * Mandatory in order to draw entities
	 * @param distribution
	 */
	public void setEntityLevelDistribution(
			AFullNDimensionalMatrix<Double> distribution) {
		this.entitySampler.setDistribution(distribution);
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
