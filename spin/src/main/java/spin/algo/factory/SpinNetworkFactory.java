package spin.algo.factory;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.algo.generator.RandomNetworkGenerator;
import spin.algo.generator.RegularNetworkGenerator;
import spin.algo.generator.SFNetworkGenerator;
import spin.algo.generator.SWNetworkGenerator;
import spin.interfaces.ENetworkGenerator;
import spin.objects.SpinNetwork;

/** Propose de générer des réseaux 
 * Si le réseau est non orienté, chaque edges n'est mis qu'une fois, donc pas d'aller retour implicite. 
 *
 */
public class SpinNetworkFactory {
	
	// SpinNetwork est le réseau courant sur la population, donc pas plusieurs type de SpinNetwork
	// contrairement a GraphStreamFactory possédant plusieurs graphes 
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
	
	/** Renvoi un spinNetwork sur une population passé en paramètre, en prenant une population
	 * en entrée.
	 * 
	 * @param typeGenerator Type du réseau généré
	 * @param population Population en parametre. 
	 * @return
	 */
	public SpinNetwork generateNetwork(ENetworkGenerator typeGenerator, IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		if(typeGenerator.equals(ENetworkGenerator.SmallWorld))
			network = new SWNetworkGenerator().generateNetwork(population,4, .1); 
		if(typeGenerator.equals(ENetworkGenerator.Random))	
			network = new RandomNetworkGenerator().generateNetwork(population, .1);
		if(typeGenerator.equals(ENetworkGenerator.Regular))	
			network = new RegularNetworkGenerator().generateNetwork(population, 2);
		if(typeGenerator.equals(ENetworkGenerator.ScaleFree))	
			network = new SFNetworkGenerator().generateNetwork(population);
		
		return network;
	}
	
	public SpinNetwork getSpinNetwork(){
		return this.network;
	}
	
}
