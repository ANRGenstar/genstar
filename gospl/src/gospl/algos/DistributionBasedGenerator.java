package gospl.algos;

import java.util.stream.Collectors;

import gospl.algos.exception.GosplSampleException;
import gospl.algos.sampler.GosplBasicSampler;
import gospl.algos.sampler.ISampler;
import gospl.distribution.INDimensionalMatrix;
import gospl.distribution.coordinate.ACoordinate;
import gospl.metamodel.GosplEntity;
import gospl.metamodel.GosplPopulation;
import gospl.metamodel.IPopulation;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

/**
 * A generator that take a defined distribution and a given sampler
 * 
 * TODO: make a builder > for example, build the sampler based on a distribution
 * 
 * @author kevinchapuis
 *
 */
public class DistributionBasedGenerator implements ISyntheticPopGenerator {
	
	private ISampler<ACoordinate<IAttribute, IValue>> sampler;
	
	protected DistributionBasedGenerator(INDimensionalMatrix<IAttribute, IValue, Double> distribution) throws GosplSampleException {
		// TODO use the builder to initialize sampler
		this.sampler = new GosplBasicSampler(distribution);
	}
	
	@Override
	public IPopulation generate(int numberOfIndividual) throws GosplSampleException {
		IPopulation pop = new GosplPopulation();
		pop.addAll(sampler.draw(numberOfIndividual).parallelStream().map(coord -> new GosplEntity(coord.getMap())).collect(Collectors.toSet()));
		return pop;
	}

}
