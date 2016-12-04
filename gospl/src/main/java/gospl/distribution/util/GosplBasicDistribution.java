package gospl.distribution.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

public class GosplBasicDistribution implements Map<ACoordinate<APopulationAttribute, APopulationValue>, Double> {

	private final Map<ACoordinate<APopulationAttribute, APopulationValue>, Double> innermap; 

	public GosplBasicDistribution(Map<Set<APopulationValue>, Double> sampleDistribution) {
		if(sampleDistribution.isEmpty())
			throw new IllegalArgumentException("Sample distribution cannot be empty");
		innermap = sampleDistribution.entrySet().parallelStream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(e -> new GosplCoordinate(e.getKey()), e -> e.getValue(), 
						(e1, e2) -> e1, LinkedHashMap::new));
	}

	public GosplBasicDistribution(AFullNDimensionalMatrix<Double> distribution) {
		innermap = distribution.getMatrix().entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValue(), 
						(e1, e2) -> e1, LinkedHashMap::new));
	}

	@Override
	public int size() {
		return innermap.size();
	}

	@Override
	public boolean isEmpty() {
		return innermap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return innermap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return innermap.containsValue(value);
	}

	@Override
	public Double get(Object key) {
		return innermap.get(key);
	}

	@Override
	public Double put(ACoordinate<APopulationAttribute, APopulationValue> key, Double value) {
		return innermap.put(key, value);
	}

	@Override
	public Double remove(Object key) {
		return innermap.remove(key);
	}

	@Override
	public void putAll(Map<? extends ACoordinate<APopulationAttribute, APopulationValue>, ? extends Double> m) {
		innermap.putAll(m);
	}

	@Override
	public void clear() {
		innermap.clear();
	}

	@Override
	public Set<ACoordinate<APopulationAttribute, APopulationValue>> keySet() {
		return innermap.keySet();
	}

	@Override
	public Collection<Double> values() {
		return innermap.values();
	}

	@Override
	public Set<java.util.Map.Entry<ACoordinate<APopulationAttribute, APopulationValue>, Double>> entrySet() {
		return innermap.entrySet();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(); 
		sb.append("[");
		sb.append(GosplBasicDistribution.class.getSimpleName());
		sb.append("] ");
		sb.append(innermap.entrySet().parallelStream().map(sc -> sc.getKey().getDimensions()).collect(Collectors.toSet()));
		sb.append(" card=");
		sb.append(innermap.size());
		return sb.toString();
	}

}
