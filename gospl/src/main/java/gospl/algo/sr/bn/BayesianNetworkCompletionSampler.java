package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.Map;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.sampler.ICompletionSampler;

/**
 * A sampler which is completing missing attributes from an entity which already exists
 * using a Bayesian network.
 * 
 * @author Samuel Thiriot
 */
public class BayesianNetworkCompletionSampler implements ICompletionSampler<ADemoEntity> {

	private final CategoricalBayesianNetwork bn;
	private final AbstractInferenceEngine engine;
	private final Map<String,DemographicAttribute<? extends IValue>> bnVariable2popAttribute;
	private final Map<DemographicAttribute<? extends IValue>,NodeCategorical> popAttribute2bnVariable;

	public BayesianNetworkCompletionSampler(CategoricalBayesianNetwork bn) throws GSIllegalRangedData {
		this(bn, new EliminationInferenceEngine(bn));
	}
	
	public BayesianNetworkCompletionSampler(CategoricalBayesianNetwork bn, AbstractInferenceEngine engine) throws GSIllegalRangedData {
		this.bn = bn;
		this.engine = engine;// BestInferenceEngine(bn);
		
		// will probably evolve in the future
		

		// Setup the factory that build attribute

		this.bnVariable2popAttribute = new HashMap<>(bn.getNodes().size());
		for (NodeCategorical n: bn.getNodes()) {
			bnVariable2popAttribute.put(
					n.name, 
					DemographicAttributeFactory.getFactory().createAttribute(
						n.getName(), 
						GSEnumDataType.Nominal,
						n.getDomain()
						)
					);
		}
		this.popAttribute2bnVariable = new HashMap<>(bn.getNodes().size());
		
	}
	

	protected NodeCategorical getBNVariableForAttribute(DemographicAttribute<? extends IValue> a) {
		
		NodeCategorical n = null;
		
		if (!popAttribute2bnVariable.containsKey(a)) {
			// we don't know this attribute ! Let's try to find it
			n = bn.getVariable(a.getAttributeName());
			// note that n might be null if it does not exist
			// in this case we will still put null in the map so we don't search again another time
			popAttribute2bnVariable.put(a, n);
			bnVariable2popAttribute.put(n.getName(), a);
		} else {
			n = popAttribute2bnVariable.get(a);
		}
		
		return n;
		
	}
	
	protected DemographicAttribute<? extends IValue> getPopulationAttributeForBNVariable(NodeCategorical n) {
		
		DemographicAttribute<? extends IValue> a = bnVariable2popAttribute.get(n.name);
		
		if (!bnVariable2popAttribute.containsKey(n.name)) {
			// we don't have any counterpart for this population attribute now
			// let's create it !
			try {
				a = DemographicAttributeFactory.getFactory().createAttribute(
						n.getName(), 
						GSEnumDataType.Nominal,
						n.getDomain()
						);
			} catch (GSIllegalRangedData e) {
				throw new RuntimeException("unable to create attribute", e);
			}
			bnVariable2popAttribute.put(n.getName(), a);
		} else {
			a = bnVariable2popAttribute.get(n.name);
		}
		
		return a;
		
	}
	
	@Override
	public ADemoEntity complete(ADemoEntity originalEntity) {

		// we already have the original entity
		
		// let's use it as evidence
		for (DemographicAttribute<? extends IValue> aOriginal: originalEntity.getAttributes()) {
			
			NodeCategorical n = getBNVariableForAttribute(aOriginal);
			
			if (n == null)
				// this piece of data does not exist into the BN; no evidence for this one
				continue;
			
			engine.addEvidence(n, originalEntity.getValueForAttribute(aOriginal).getStringValue());	
		}
		
		System.err.println("inference with evidence : "+engine.evidenceVariable2value);
		System.err.println("p(evidence): "+engine.getProbabilityEvidence());

		// now we can generate the missing values
		Map<NodeCategorical,String> variable2value = engine.sampleOne();
		
		// let's take these values and use them inside our BN
		// TODO should clone here !!!
		ADemoEntity resultEntity = (ADemoEntity) originalEntity.clone(); 
		
		for (Map.Entry<NodeCategorical,String> eNew: variable2value.entrySet()) {
			
			DemographicAttribute<? extends IValue> a = getPopulationAttributeForBNVariable(eNew.getKey());
			
			// skip the known attributes
			if (resultEntity.hasAttribute(a)) {
				// the entity already contains a value; it is useless to update it
				continue;
			}
			
			resultEntity.setAttributeValue(a, a.getValueSpace().addValue(eNew.getValue()));
			
		}
		
		return resultEntity;
	}
	


	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
