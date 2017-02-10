package spin.networkProperties;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.generator.UtilGenerator;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;
import spin.algo.factory.StatFactory;
import spin.interfaces.ENetworkEnumGenerator;
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
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population =
		generator.generate(100000);
	
		System.out.println("Debut de la génération de réseau SmallWorld");
		SpinNetwork networkSW = SpinNetworkFactory.getInstance().generateNetwork(ENetworkEnumGenerator.SmallWorld, population);
		System.out.println("Fin de génération de réseau SmallWorld");
	
		SpinPopulation populationWithNetwork = new SpinPopulation(population, networkSW, StatFactory.getInstance());
		
		
		// TEST
		System.out.println(StatFactory.getInstance().getDensity());
		 
		System.out.println(StatFactory.getInstance().getAPL());
		
		
		
		
	}

}
