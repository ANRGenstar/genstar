package spin.algo.factory;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.algo.generator.SWGenerator;
import spin.interfaces.ENetworkEnumGenerator;
import spin.objects.SpinNetwork;

/** Propose de générer des réseaux 
 *
 */
public class SpinNetworkFactory {
	
	private SpinNetwork network;
	
	// Singleton
	private static SpinNetworkFactory INSTANCE;
	
	public static SpinNetworkFactory getIntance(){
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
	public SpinNetwork generateNetwork(ENetworkEnumGenerator typeGenerator, IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		if(typeGenerator.equals(ENetworkEnumGenerator.SmallWorld))
			network = new SWGenerator().generateNetwork(population,4, .1); 
			
//		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
//			return new SFGenerator().generateNetwork(population);
		
		return network;
	}
	
	public SpinNetwork getSpinNetwork(){
		return this.network;
	}
	
}
