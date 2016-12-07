package gospl.algo.ipf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

public class DistributionInferenceIPFAlgo extends GosplIPF<Double> implements IDistributionInferenceAlgo<IDistributionSampler> {
	
	private int step = MAX_STEP;
	private double delta = MAX_DELTA;

	public DistributionInferenceIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed) {
		super(seed);
	}
	
	public DistributionInferenceIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed,
			int step, double delta) {
		super(seed);
		this.step = step;
		this.delta = delta;
	}

	@Override
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferDistributionSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
			IDistributionSampler sampler)
			throws IllegalDistributionCreation {
		
		super.setMarginalMatrix(matrix);
		sampler.setDistribution(process(delta, step));
		
		return sampler;
	}

	@Override
	public AFullNDimensionalMatrix<Double> process(double delta, int step) {
		if(this.matrix == null || this.matrix.getMatrix().isEmpty()) 
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" must define a matrix to setup marginals");
		
		// Setup output distribution using seed population
		AFullNDimensionalMatrix<Double> distribution = new GosplDistributionFactory().createDistribution(seed);
		
		// Setup IPF main argument
		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<Double>>> marginals = super.getMarginalValues(true);
		List<APopulationAttribute> attributesList = new ArrayList<>(this.matrix.getDimensions());
		
		// First: establish convergence criteria
		List<Double> errors = IntStream.range(0, this.matrix.getDimensions().size())
				.mapToObj(i -> Double.POSITIVE_INFINITY).collect(Collectors.toList());
		List<Double> deltas = attributesList.stream().map(att -> marginals.get(att).values()
				.stream().mapToDouble(control -> control.getValue()).sum() * delta)
				.collect(Collectors.toList());
		
		// Iterate while one of the criteria is not reach
		while(step-- > 0 || IntStream.range(0, attributesList.size())
				.allMatch(i -> errors.get(i) <= deltas.get(i))){
			// For each dimension
			for(APopulationAttribute attribute : attributesList){
				// For each marginal
				for(Entry<Set<APopulationValue>, AControl<Double>> entry : 
					marginals.get(attribute).entrySet()){
					// Compute correction factor
					double factor = entry.getValue().getValue() / distribution.getVal(entry.getKey()).getValue();
					// For each value
					for(APopulationValue value : attribute.getValues()){
						distribution.getVal(new GosplCoordinate(Stream.concat(entry.getKey().stream(), 
								Stream.of(value)).collect(Collectors.toSet()))).multiply(factor);
					}
				}
			}
		}
		return distribution;
	}

}
