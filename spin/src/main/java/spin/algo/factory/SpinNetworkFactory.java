package spin.algo.factory;

import core.metamodel.IPopulation;
import core.metamodel.pop.DemographicAttribute;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.APopulationValue;
import spin.SpinPopulation;
import spin.algo.generator.RandomNetworkGenerator;
import spin.algo.generator.RegularNetworkGenerator;
import spin.algo.generator.SFNetworkGenerator;
import spin.algo.generator.SWNetworkGenerator;
import spin.interfaces.ENetworkGenerator;
import spin.objects.SpinNetwork;

/** Propose de generer des reseaux 
 * Si le reseau est non oriente, chaque edges n'est mis qu'une fois, donc pas d'aller retour implicite. 
 */
public class SpinNetworkFactory {
	
	// SpinNetwork est le reseau courant sur la population, donc pas plusieurs type de SpinNetwork
	// contrairement a GraphStreamFactory possedant plusieurs graphes 
	private SpinNetwork network;
	
	// Singleton
	private static SpinNetworkFactory INSTANCE;
	
	public static SpinNetworkFactory getInstance(){
		if(INSTANCE == null)
			INSTANCE = new SpinNetworkFactory();
		return INSTANCE;
	}
	
	private SpinNetworkFactory(){
	}
	
	/** Création d'un SpinNetwork correspondant à la population passée en paramètre
	 * @param population
	 * @return SpinNetwork. 
	 */
	public SpinNetwork loadPopulation(IPopulation<? extends ADemoEntity, DemographicAttribute, APopulationValue> population){
		// Create a SpinNetwork with nodes linked to population entities
		// The SpinNetwork has all the needed nodes and no links
		SpinNetwork myNetwork = new SpinNetwork();
		int i = 0;		
		
		// create all the nodes 
		for (ADemoEntity entity : population) {
			myNetwork.putNode(String.valueOf(i), entity);
			i++;
		}
		
		return myNetwork;
	}
	
	/** Création d'une SpinPopulation dont le réseau correspond à la population passée en paramètre.
	 * Le réseau généré est du type passé en paramètre.
	 * 
	 * @param typeGenerator Type du reseau genere
	 * @param population Population en parametre. 
	 * @return SpinPopulation
	 */
	public SpinPopulation generateNetwork(ENetworkGenerator typeGenerator, 
			IPopulation<ADemoEntity, DemographicAttribute, APopulationValue> population){
		SpinNetwork baseNetwork = loadPopulation(population);
		if(typeGenerator.equals(ENetworkGenerator.SmallWorld))
			network = new SWNetworkGenerator().generateNetwork(baseNetwork,4, .1); 
		if(typeGenerator.equals(ENetworkGenerator.Random))	
			network = new RandomNetworkGenerator().generateNetwork(baseNetwork, .1);
		if(typeGenerator.equals(ENetworkGenerator.Regular))	
			network = new RegularNetworkGenerator().generateNetwork(baseNetwork, 4);
		if(typeGenerator.equals(ENetworkGenerator.ScaleFree))	
			network = new SFNetworkGenerator().generateNetwork(baseNetwork);
		
		// Create the SpinPopulation
		SpinPopulation spinPop = new SpinPopulation(population, network);
		return spinPop;
	}
	
	public SpinNetwork getSpinNetwork(){
		return this.network;
	}
}
