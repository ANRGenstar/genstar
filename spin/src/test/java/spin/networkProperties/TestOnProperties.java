package spin.networkProperties;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.util.GSUtilGenerator;
import spin.SpinNetwork;
import spin.algo.generator.SpinRegularNetworkGenerator;

public class TestOnProperties {

	/** TEST sur la génération des propriétés
	 * I - Generation d'un réseau ( avec ou sans population ) 
	 * II - Obtention d'une SpinPopulation 
	 * III - Obtention des différentes propriétés associées à la population
	 * @param args
	 */
	public static void main(String[] args) {
		ISyntheticGosplPopGenerator generator = new GSUtilGenerator(2, 4);
		@SuppressWarnings("unused")
		IPopulation<ADemoEntity, Attribute<? extends IValue>> population = generator.generate(100);
	
		System.out.println("Debut de la génération de réseau Regular");
//		SpinPopulation populationWithNetwork = SpinNetworkFactory.getInstance().generateNetwork(ENetworkGenerator.Regular, population);
		
		SpinRegularNetworkGenerator<GosplEntity> spinPopGen = new SpinRegularNetworkGenerator<>("family",4);
		@SuppressWarnings("unused")
		SpinNetwork networkedPop = spinPopGen.generate(30);	
		
//		SpinNetwork networkSW = populationWithNetwork.getNetwork();
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
