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
public class NetworkFactory {
	
	/** Renvoi un spinNetwork sur une population passé en paramètre, en prenant une population
	 * en entrée.
	 * 
	 * @param typeGenerator Type du réseau généré
	 * @param population Population en parametre. 
	 * @return
	 */
	public static SpinNetwork getNetwork(ENetworkEnumGenerator typeGenerator, 
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		if(typeGenerator.equals(ENetworkEnumGenerator.SmallWorld))
			return new SWGenerator().generateNetwork(population);
//		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
//			return new SFGenerator().generateNetwork(population);
		return null;
	}
 
	
}
