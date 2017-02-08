package spin;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.generator.UtilGenerator;
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
		ISyntheticGosplPopGenerator populationGenerator = new UtilGenerator(4,2);
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population = populationGenerator.generate(nbNode);

//		// 3 la factory choisit un générator grace a une unum
//		// 4 le générator renvoi le réseau obtenu sur la population
		@SuppressWarnings("unused")
		SpinNetwork aNetwork = NetworkFactory.getNetwork(NetworkEnumGenerator.SmallWorld, population);
 		
	}
}
