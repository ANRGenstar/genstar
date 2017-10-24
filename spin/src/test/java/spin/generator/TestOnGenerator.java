package spin.generator;

import org.graphstream.graph.Graph;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UtilGenerator;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;
import spin.interfaces.ENetworkGenerator;
import spin.objects.SpinNetwork;

public class TestOnGenerator {

	/** Test pour les fonctions de generation de reseau sur une population
	 * A - Generation d'une population de taille variable
	 * B - Generation d'un graphe
	 * C - Affichage du graphe
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ISyntheticGosplPopGenerator generator = new UtilGenerator(2, 4);
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population = generator.generate(100);
/*		
		System.out.println("Debut de la generation de reseau regulier");
		SpinPopulation populationWithNetworkRegular = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.Regular, population);
		SpinNetwork networkRegular = populationWithNetworkRegular.getNetwork();
		System.out.println("Fin de generation de reseau regulier");
		
		System.out.println("Debut de la generation de reseau Scale Free");
		SpinPopulation populationWithNetworkSF = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.ScaleFree, population);
		SpinNetwork networkSF = populationWithNetworkSF.getNetwork();
		System.out.println("Fin de generation de reseau Scale Free");
		
		System.out.println("Debut de la generation de reseau Random");
		SpinPopulation populationWithNetworkRandom = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.Random, population);
		SpinNetwork networkRandom = populationWithNetworkRandom.getNetwork();
		System.out.println("Fin de generation de reseau Random");
		
		System.out.println("Debut de la generation de reseau SmallWorld");
		SpinPopulation populationWithNetworkSW = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.SmallWorld, population);
		SpinNetwork networkSW = populationWithNetworkSW.getNetwork();
		System.out.println("Fin de generation de reseau SmallWorld");
*/		
		SpinPopulation populationWithNetwork = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.ScaleFree, population);
		SpinNetwork networkTest = populationWithNetwork.getNetwork();
		networkTest.network.display();
		
		Graph sampleGraph = networkTest.randomWalkSample(50);
		sampleGraph.display();
	}
}
