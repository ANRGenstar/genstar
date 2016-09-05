package gospl.generator;

import java.util.stream.Collectors;

import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.ISampler;
import gospl.distribution.matrix.coordinate.ACoordinate;
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
	
	public DistributionBasedGenerator(ISampler<ACoordinate<IAttribute, IValue>> sampler) throws GosplSamplerException {
		this.sampler = sampler;
	}
	
	@Override
	public IPopulation generate(int numberOfIndividual) throws GosplSamplerException {
		IPopulation pop = new GosplPopulation();
		pop.addAll(sampler.draw(numberOfIndividual).parallelStream().map(coord -> new GosplEntity(coord.getMap())).collect(Collectors.toSet()));
		return pop;
	}

}
