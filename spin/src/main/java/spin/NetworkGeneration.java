package spin;

import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UniformRandomGenerator;
import spin.algo.factory.NetworkFactory;
import spin.algo.generator.NetworkEnumGenerator;
import spin.objects.SpinNetwork;


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
		IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population =
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
