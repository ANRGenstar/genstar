package spin.algo.factory;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.algo.generator.SWGenerator;
import spin.interfaces.NetworkEnumGenerator;
import spin.objects.SpinNetwork;

/** Propose de générer des réseaux 
 *
 */
public class NetworkFactory {
	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		if(typeGenerator.equals(NetworkEnumGenerator.SmallWorld))
			return new SWGenerator().generateNetwork(population);
//		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
//			return new SFGenerator().generateNetwork(population);
		return null;
	}
 
	
}
