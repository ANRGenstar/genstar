package spin.algo.factory;

import spin.algo.generator.NetworkEnumGenerator;
import spin.algo.generator.SFGenerator;
import spin.algo.generator.SWGenerator;
import spin.objects.SpinNetwork;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;

/** Propose de générer des réseaux 
 * 
 *
 */
public class NetworkFactory {
	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
			IPopulation<? extends IEntity<ASurveyAttribute, AValue>, ASurveyAttribute, AValue> population){
		if(typeGenerator.equals(NetworkEnumGenerator.SmallWorld))
			return null;
//			return new SWGenerator().generateNetwork(population);
		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
			return new SFGenerator().generateNetwork(population);
		return null;
	}
}
