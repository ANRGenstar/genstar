package spin.generator;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.generator.UtilGenerator;
import spin.algo.factory.SpinNetworkFactory;
import spin.interfaces.ENetworkEnumGenerator;
import spin.objects.SpinNetwork;

public class TestOnGenerator {

	/** Test pour les fonctions de génération de réseau sur une population
	 * A - Génération d'une population de taille variable
	 * B - Génération d'un graphe
	 * C - Affichage du graphe
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ISyntheticGosplPopGenerator generator = new UtilGenerator(2, 4);
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population =
		generator.generate(100000);
		
		System.out.println("Debut de la génération de réseau régulier");
		SpinNetwork networkRegular = SpinNetworkFactory.getInstance().generateNetwork(ENetworkEnumGenerator.Regular, population);
		System.out.println("Fin de génération de réseau régulier");

		System.out.println("Debut de la génération de réseau Scale Free");
		SpinNetwork networkSF = SpinNetworkFactory.getInstance().generateNetwork(ENetworkEnumGenerator.ScaleFree, population);
		System.out.println("Fin de génération de réseau Scale Free");

		System.out.println("Debut de la génération de réseau Random");
		SpinNetwork networkRandom = SpinNetworkFactory.getInstance().generateNetwork(ENetworkEnumGenerator.Random, population);
		System.out.println("Fin de génération de réseau Scale Random");

		System.out.println("Debut de la génération de réseau SmallWorld");
		SpinNetwork networkSW = SpinNetworkFactory.getInstance().generateNetwork(ENetworkEnumGenerator.SmallWorld, population);
		System.out.println("Fin de génération de réseau SmallWorld");
	}

}
