package spin.algo.factory;

import spin.algo.generator.INetworkGenerator;
import spin.algo.generator.NetworkEnumGenerator;
import spin.algo.generator.SFGenerator;
import spin.algo.generator.SWGenerator;

/** Propose de générer des réseaux 
 * 
 *
 */
public class NetworkGeneratorFactory {
	public static INetworkGenerator getNetworkGenerator(NetworkEnumGenerator typeGenerator){
		if(typeGenerator.equals(NetworkEnumGenerator.SmallWorld))
			return new SWGenerator();
		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
			return new SFGenerator();
		return null;
	}
}
