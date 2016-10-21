package gospl.distribution.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import io.metamodel.attribut.IAttribute;
import io.metamodel.attribut.value.IValue;

public class BasicDistribution implements SortedMap<ACoordinate<IAttribute, IValue>, Double> {

	private final SortedMap<ACoordinate<IAttribute, IValue>, Double> innermap; 
	
	public BasicDistribution() {
		this.innermap = new TreeMap<ACoordinate<IAttribute, IValue>, Double>(
				new Comparator<ACoordinate<IAttribute, IValue>>(){
					public int compare(ACoordinate<IAttribute, IValue> coord1, ACoordinate<IAttribute, IValue> coord2 ){
			            return innermap.get(coord1).compareTo(innermap.get(coord2));
			        }
				}
			);
	}

	public BasicDistribution(Map<Set<IValue>, Double> sampleDistribution) {
		this();
		innermap.putAll(sampleDistribution.entrySet()
				.parallelStream().collect(Collectors.toMap(e -> safeCoordinateCreation(e.getKey()), Entry::getValue)));
	}

	private ACoordinate<IAttribute, IValue> safeCoordinateCreation(Set<IValue> coordValues) {
		ACoordinate<IAttribute, IValue> coordinate = null;
		try {
			coordinate = new GosplCoordinate(coordValues);
		} catch (MatrixCoordinateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return coordinate;
	}

	public BasicDistribution(AFullNDimensionalMatrix<Double> distribution) {
		this();
		innermap.putAll(distribution.getMatrix().entrySet()
				.parallelStream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue())));
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
	public Double put(ACoordinate<IAttribute, IValue> key, Double value) {
		return innermap.put(key, value);
	}

	@Override
	public Double remove(Object key) {
		return innermap.remove(key);
	}

	@Override
	public void putAll(Map<? extends ACoordinate<IAttribute, IValue>, ? extends Double> m) {
		innermap.putAll(m);
	}

	@Override
	public void clear() {
		innermap.clear();
	}

	@Override
	public Comparator<? super ACoordinate<IAttribute, IValue>> comparator() {
		return innermap.comparator();
	}

	@Override
	public SortedMap<ACoordinate<IAttribute, IValue>, Double> subMap(ACoordinate<IAttribute, IValue> fromKey,
			ACoordinate<IAttribute, IValue> toKey) {
		return innermap.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<ACoordinate<IAttribute, IValue>, Double> headMap(ACoordinate<IAttribute, IValue> toKey) {
		return innermap.headMap(toKey);
	}

	@Override
	public SortedMap<ACoordinate<IAttribute, IValue>, Double> tailMap(ACoordinate<IAttribute, IValue> fromKey) {
		return innermap.tailMap(fromKey);
	}

	@Override
	public ACoordinate<IAttribute, IValue> firstKey() {
		return innermap.firstKey();
	}

	@Override
	public ACoordinate<IAttribute, IValue> lastKey() {
		return innermap.lastKey();
	}

	@Override
	public Set<ACoordinate<IAttribute, IValue>> keySet() {
		return innermap.keySet();
	}

	@Override
	public Collection<Double> values() {
		return innermap.values();
	}

	@Override
	public Set<java.util.Map.Entry<ACoordinate<IAttribute, IValue>, Double>> entrySet() {
		return innermap.entrySet();
	}

}
