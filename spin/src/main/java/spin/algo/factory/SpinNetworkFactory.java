package spin.algo.factory;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
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
	
	// TODO [stage] depuis une population obtenir un SPinPop avec le network correspondant
	
	/** Creation d'une SpinPopulation dont le network correspond a la population passee en parametre
	 * @param population
	 * @return une SpinPopulation. 
	 */
	public SpinPopulation loadPopulation(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		// Create a SpinNetwork with nodes linked to population entities
		// The SpinNetwork has all the needed nodes and no links
		SpinNetwork myNetwork = new SpinNetwork();
		int i = 0;		
		
		// create all the nodes 
		for (APopulationEntity entity : population) {
			myNetwork.putNode(String.valueOf(i), entity);
			i++;
		}
		
		// Create the SpinPopulation
		SpinPopulation spinPop = new SpinPopulation(population, myNetwork);
		return spinPop;
	}
	
	/** Renvoi un spinNetwork sur une population passe en parametre, en prenant une population
	 * en entree.
	 * 
	 * @param typeGenerator Type du reseau genere
	 * @param population Population en parametre. 
	 * @return
	 */
	public SpinNetwork generateNetwork(ENetworkGenerator typeGenerator, SpinPopulation spinPop){
		if(typeGenerator.equals(ENetworkGenerator.SmallWorld))
			network = new SWNetworkGenerator().generateNetwork(spinPop,4, .1); 
		if(typeGenerator.equals(ENetworkGenerator.Random))	
			network = new RandomNetworkGenerator().generateNetwork(spinPop, .1);
		if(typeGenerator.equals(ENetworkGenerator.Regular))	
			network = new RegularNetworkGenerator().generateNetwork(spinPop, 4);
		if(typeGenerator.equals(ENetworkGenerator.ScaleFree))	
			network = new SFNetworkGenerator().generateNetwork(spinPop);
		
		return network;
	}
	
	public SpinNetwork getSpinNetwork(){
		return this.network;
	}	
}
