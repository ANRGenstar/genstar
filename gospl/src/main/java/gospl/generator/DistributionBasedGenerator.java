package gospl.generator;

import java.util.stream.Collectors;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.ISampler;
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
	
	private ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler;
	
	public DistributionBasedGenerator(ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler) throws GosplSamplerException {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) throws GosplSamplerException {
		GosplPopulation pop = new GosplPopulation();
		pop.addAll(sampler.draw(numberOfIndividual).parallelStream().map(coord -> new GosplEntity(coord.getMap())).collect(Collectors.toSet()));
		return pop;
	}

}
