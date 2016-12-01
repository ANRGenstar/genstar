package gospl.generator;

import java.util.stream.Collectors;

import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import gospl.algo.sampler.ISampler;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.GosplEntity;
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
	
	private ISampler<ACoordinate<ASurveyAttribute, ASurveyValue>> sampler;
	
	public DistributionBasedGenerator(ISampler<ACoordinate<ASurveyAttribute, ASurveyValue>> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		GosplPopulation pop = new GosplPopulation();
		pop.addAll(sampler.draw(numberOfIndividual).parallelStream().map(coord -> new GosplEntity(coord.getMap())).collect(Collectors.toSet()));
		return pop;
	}

}
