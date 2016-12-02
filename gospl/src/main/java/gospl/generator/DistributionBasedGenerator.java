package gospl.generator;

import java.util.stream.Collectors;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import gospl.algo.sampler.ISampler;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.GenstarEntity;
import gospl.metamodel.GosplPopulation;

/**
 * A generator that take a defined distribution and a given sampler
 * 
 * TODO: make a builder > for example, build the sampler based on a distribution
 * 
 * @author kevinchapuis
 *
 */
public class DistributionBasedGenerator implements ISyntheticGosplPopGenerator {
	
	private ISampler<ACoordinate<AGenstarAttribute, AGenstarValue>> sampler;
	
	public DistributionBasedGenerator(ISampler<ACoordinate<AGenstarAttribute, AGenstarValue>> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		GosplPopulation pop = new GosplPopulation();
		pop.addAll(sampler.draw(numberOfIndividual).parallelStream().map(coord -> new GenstarEntity(coord.getMap())).collect(Collectors.toSet()));
		return pop;
	}

}
