package spin.algo.factory;

import spin.algo.generator.NetworkEnumGenerator;
import spin.algo.generator.SWGenerator;
import spin.objects.SpinNetwork;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import gospl.metamodel.GosplPopulation;

/** Propose de générer des réseaux 
 * 
 *
 */
public class NetworkFactory {
//	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
//			IPopulation<? extends IEntity<ASurveyAttribute, AValue>, ASurveyAttribute, AValue> population){
	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
			GosplPopulation population){
		if(typeGenerator.equals(NetworkEnumGenerator.SmallWorld))
			return new SWGenerator().generateNetwork(population);
//		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
//			return new SFGenerator().generateNetwork(population);
		return null;
	}
	
//	public<V extends IValue, A extends IAttribute<V>> static SpinNetwork<V,A> 
	
}
