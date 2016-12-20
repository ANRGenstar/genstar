package gospl.evaluation;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;

/**
 * Tags samplers which can be evaluated. It typically means the characteristics of the generated population
 * can be compared with the parameters. 
 * 
 * @author Samuel Thiriot
 *
 */
public interface IEvaluableSampler {


	/**
	 * Evaluate a population generated using this sampler with the current parameters. 
	 * 
	 * @param population
	 */
	public ISamplingEvaluation evaluateQuality(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population);
	
}
