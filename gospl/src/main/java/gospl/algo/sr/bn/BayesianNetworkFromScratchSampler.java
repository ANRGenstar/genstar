package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;
import gospl.sampler.ISampler;

public class BayesianNetworkFromScratchSampler implements ISampler<ACoordinate<APopulationAttribute, APopulationValue>> {

	private final CategoricalBayesianNetwork bn;
	private final AbstractInferenceEngine engine;
	private final Map<String,APopulationAttribute> bnVariable2popAttribute;
	
	public BayesianNetworkFromScratchSampler(CategoricalBayesianNetwork bn) throws GSIllegalRangedData {
		this.bn = bn;
		this.engine = new BestInferenceEngine(bn);
		
		// will probably evolve in the future
		

		// Setup the factory that build attribute
		GosplAttributeFactory attf = new GosplAttributeFactory();

		this.bnVariable2popAttribute = new HashMap<>(bn.getNodes().size());
		for (NodeCategorical n: bn.getNodes()) {
			bnVariable2popAttribute.put(
					n.name, 
					attf.createAttribute(
						n.getName(), 
						GSEnumDataType.String,
						n.getDomain(),
						GSEnumAttributeType.unique
						)
					);
		}
		
	}
	
	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> draw() {

		
		Map<APopulationAttribute,APopulationValue> att2value = new HashMap<>(bn.getNodes().size());

		
		// sample one
		Map<NodeCategorical,String> variable2value = engine.sampleOne();

		// convert the result from the BAyesian network to a valid type
		for (Map.Entry<NodeCategorical,String> e: variable2value.entrySet()) {
			APopulationAttribute a = bnVariable2popAttribute.get(e.getKey().getName());
			att2value.put(a, a.getValue(e.getValue()));		
		}
		
		return new GosplCoordinate(new HashSet<>(att2value.values()));
	}

	@Override
	public List<ACoordinate<APopulationAttribute, APopulationValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
