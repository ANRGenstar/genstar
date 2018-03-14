package gospl.algo.sr.bn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.sampler.ISampler;

public class BayesianNetworkFromScratchSampler implements ISampler<ACoordinate<Attribute<? extends IValue>, IValue>> {

	private final CategoricalBayesianNetwork bn;
	private final AbstractInferenceEngine engine;
	private final Map<String,Attribute<? extends IValue>> bnVariable2popAttribute;
	
	public BayesianNetworkFromScratchSampler(CategoricalBayesianNetwork bn) throws GSIllegalRangedData {
		this.bn = bn;
		this.engine = new BestInferenceEngine(bn);
		
		// will probably evolve in the future

		this.bnVariable2popAttribute = new HashMap<>(bn.getNodes().size());
		for (NodeCategorical n: bn.getNodes()) {
			bnVariable2popAttribute.put(
					n.name, 
					AttributeFactory.getFactory().createAttribute(
						n.getName(), 
						GSEnumDataType.Nominal,
						n.getDomain()
						)
					);
		}
		
	}
	
	@Override
	public ACoordinate<Attribute<? extends IValue>, IValue> draw() {

		
		Map<Attribute<? extends IValue>,IValue> att2value = new HashMap<>(bn.getNodes().size());

		
		// sample one
		Map<NodeCategorical,String> variable2value = engine.sampleOne();

		// convert the result from the BAyesian network to a valid type
		for (Map.Entry<NodeCategorical,String> e: variable2value.entrySet()) {
			Attribute<? extends IValue> a = bnVariable2popAttribute.get(e.getKey().getName());
			att2value.put(a, a.getValueSpace().addValue(e.getValue()));		
		}
		
		return new GosplCoordinate(att2value);
	}

	@Override
	public Collection<ACoordinate<Attribute<? extends IValue>, IValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
