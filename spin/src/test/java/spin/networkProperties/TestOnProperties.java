package spin.networkProperties;

import core.metamodel.IPopulation;
import core.metamodel.pop.DemographicAttribute;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.APopulationValue;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UtilGenerator;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;
import spin.interfaces.ENetworkGenerator;
import spin.objects.SpinNetwork;

public class TestOnProperties {

	/** TEST sur la génération des propriétés
	 * I - Generation d'un réseau ( avec ou sans population ) 
	 * II - Obtention d'une SpinPopulation 
	 * III - Obtention des différentes propriétés associées à la population
	 * @param args
	 */
	public static void main(String[] args) {
		ISyntheticGosplPopGenerator generator = new UtilGenerator(2, 4);
		IPopulation<ADemoEntity, DemographicAttribute, APopulationValue> population =
		generator.generate(100);
	
		System.out.println("Debut de la génération de réseau Regular");
		SpinPopulation populationWithNetwork = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.Regular, population);
		SpinNetwork networkSW = populationWithNetwork.getNetwork();
		System.out.println("Fin de génération de réseau Regular");
	
		//SpinPopulation populationWithNetwork = new SpinPopulation(population, networkSW);
		
		
		// TEST
//		System.out.println(StatFactory.getInstance().getDensity());
//		GraphStreamFactory.getIntance().initialiseGraphStreamFromSpin();
//		System.out.println("Fin du graphStream");
//		
//		Graph lol = GraphStreamFactory.getIntance().getGraphStreamGraph(EGraphStreamNetwork.spinNetwork);
//		lol.display();
//		
		
		
		
	}

}
