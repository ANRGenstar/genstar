package spin;

import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UniformRandomGenerator;
import spin.algo.factory.NetworkFactory;
import spin.algo.generator.NetworkEnumGenerator;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
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
		IPopulation<? extends IEntity<ASurveyAttribute, AValue>, ASurveyAttribute, AValue> population =
				populationGenerator.generate(nbNode);
//		
//		// 3 la factory choisit un générator grace a une unum
//		// 4 le générator renvoi le réseau obtenu sur la population
		SpinNetwork<IValue,IAttribute<IValue>> NEPASCOMMIT =
//		SpinNetwork<IEntity, NetworkNode<IEntity, IValue, IAttribute<IValue>>>, NetworkLink> network = 
		NetworkFactory.getNetwork(NetworkEnumGenerator.SmallWorld, population);
 		
	}
}
