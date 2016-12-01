package spin.algo.factory;

import spin.algo.generator.NetworkEnumGenerator;
import spin.algo.generator.SWGenerator;
import spin.objects.SpinNetwork;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import core.io.survey.entity.ASurveyEntity;
import core.metamodel.IEntity;

/** Propose de générer des réseaux 
 * 
 *
 */
public class NetworkFactory {
//	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
//			IPopulation<? extends IEntity<ASurveyAttribute, AValue>, ASurveyAttribute, AValue> population){
	public static SpinNetwork getNetwork(NetworkEnumGenerator typeGenerator, 
			IPopulation<ASurveyEntity, ASurveyAttribute, ASurveyValue> population){
		if(typeGenerator.equals(NetworkEnumGenerator.SmallWorld))
			return new SWGenerator().generateNetwork(population);
//		if(typeGenerator.equals(NetworkEnumGenerator.ScaleFree))
//			return new SFGenerator().generateNetwork(population);
		return null;
	}
	
//	public<V extends IValue, A extends IAttribute<V>> static SpinNetwork<V,A> 
	
}
