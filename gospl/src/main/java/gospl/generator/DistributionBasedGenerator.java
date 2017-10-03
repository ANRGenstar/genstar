package gospl.generator;

import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.ISampler;

/**
 * A generator that take a defined distribution and a given sampler
 * 
 * TODO: make a builder > for example, build the sampler based on a distribution
 * 
 * @author kevinchapuis
 *
 */
public class DistributionBasedGenerator implements ISyntheticGosplPopGenerator {
	
	private ISampler<ACoordinate<APopulationAttribute, APopulationValue>> sampler;
	
	public DistributionBasedGenerator(ISampler< ACoordinate<APopulationAttribute, APopulationValue>> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		return new GosplPopulation(sampler.draw(numberOfIndividual).stream()
				.map(coord -> new GosplEntity(coord.getMap())).collect(Collectors.toSet()));
	}

}
