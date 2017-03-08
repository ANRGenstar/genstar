package spin.networkProperties;

import org.graphstream.graph.Graph;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.generator.UtilGenerator;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;
import spin.interfaces.ENetworkGenerator;
import spin.objects.SpinNetwork;
import useless.EGraphStreamNetwork;
import useless.GraphStreamFactory;
import useless.StatFactory;

public class TestOnProperties {

	/** TEST sur la génération des propriétés
	 * I - Generation d'un réseau ( avec ou sans population ) 
	 * II - Obtention d'une SpinPopulation 
	 * III - Obtention des différentes propriétés associées à la population
	 * @param args
	 */
	public static void main(String[] args) {
		ISyntheticGosplPopGenerator generator = new UtilGenerator(2, 4);
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population =
		generator.generate(100);
	
		System.out.println("Debut de la génération de réseau Regular");
		SpinNetwork networkSW = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.Regular, population);
		System.out.println("Fin de génération de réseau Regular");
	
		SpinPopulation populationWithNetwork = new SpinPopulation(population, networkSW);
		
		
		// TEST
		System.out.println(StatFactory.getInstance().getDensity());
		GraphStreamFactory.getIntance().initialiseGraphStreamFromSpin();
		System.out.println("Fin du graphStream");
		
		Graph lol = GraphStreamFactory.getIntance().getGraphStreamGraph(EGraphStreamNetwork.spinNetwork);
		lol.display();
		
		
		
		
	}

}
