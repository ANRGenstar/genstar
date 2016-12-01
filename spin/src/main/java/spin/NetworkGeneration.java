package spin;

import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UniformRandomGenerator;
import spin.algo.factory.NetworkFactory;
import spin.algo.generator.NetworkEnumGenerator;
import spin.objects.SpinNetwork;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import core.io.survey.entity.ASurveyEntity;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;


/** Classe de main pour la génération de réseau sur une population
 * 
 *
 */
public class NetworkGeneration {

	/** Main.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int nbNode = 100;
		
//		// 1 instancier la factory
//		// 2 prendre une population en param
		ISyntheticGosplPopGenerator populationGenerator = new UniformRandomGenerator(4,2);
		IPopulation<ASurveyEntity, ASurveyAttribute, ASurveyValue> population =
				populationGenerator.generate(nbNode);
//		
//		// 3 la factory choisit un générator grace a une unum
//		// 4 le générator renvoi le réseau obtenu sur la population
//		SpinNetwork<IAttribute<IValue>,IValue> NEPASCOMMIT =
//		NetworkFactory.getNetwork(NetworkEnumGenerator.SmallWorld, population);
//		SpinNetwork NEPASCOMMIT =
//				NetworkFactory.getNetwork(NetworkEnumGenerator.SmallWorld, population);
		SpinNetwork NEPASCOMMIT =
				NetworkFactory.getNetwork(NetworkEnumGenerator.SmallWorld, population);
 		
	}
}
