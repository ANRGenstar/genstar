package gospl.distribution.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

public class BasicDistribution implements SortedMap<ACoordinate<ASurveyAttribute, AValue>, Double> {

	private final SortedMap<ACoordinate<ASurveyAttribute, AValue>, Double> innermap; 
	
	public BasicDistribution() {
		this.innermap = new TreeMap<ACoordinate<ASurveyAttribute, AValue>, Double>(
				new Comparator<ACoordinate<ASurveyAttribute, AValue>>(){
					public int compare(ACoordinate<ASurveyAttribute, AValue> coord1, ACoordinate<ASurveyAttribute, AValue> coord2 ){
			            return innermap.get(coord1).compareTo(innermap.get(coord2));
			        }
				}
			);
	}

	public BasicDistribution(Map<Set<AValue>, Double> sampleDistribution) {
		this();
		innermap.putAll(sampleDistribution.entrySet()
				.parallelStream().collect(Collectors.toMap(e -> safeCoordinateCreation(e.getKey()), Entry::getValue)));
	}

	private ACoordinate<ASurveyAttribute, AValue> safeCoordinateCreation(Set<AValue> coordValues) {
		ACoordinate<ASurveyAttribute, AValue> coordinate = null;
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
	public Double put(ACoordinate<ASurveyAttribute, AValue> key, Double value) {
		return innermap.put(key, value);
	}

	@Override
	public Double remove(Object key) {
		return innermap.remove(key);
	}

	@Override
	public void putAll(Map<? extends ACoordinate<ASurveyAttribute, AValue>, ? extends Double> m) {
		innermap.putAll(m);
	}

	@Override
	public void clear() {
		innermap.clear();
	}

	@Override
	public Comparator<? super ACoordinate<ASurveyAttribute, AValue>> comparator() {
		return innermap.comparator();
	}

	@Override
	public SortedMap<ACoordinate<ASurveyAttribute, AValue>, Double> subMap(ACoordinate<ASurveyAttribute, AValue> fromKey,
			ACoordinate<ASurveyAttribute, AValue> toKey) {
		return innermap.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<ACoordinate<ASurveyAttribute, AValue>, Double> headMap(ACoordinate<ASurveyAttribute, AValue> toKey) {
		return innermap.headMap(toKey);
	}

	@Override
	public SortedMap<ACoordinate<ASurveyAttribute, AValue>, Double> tailMap(ACoordinate<ASurveyAttribute, AValue> fromKey) {
		return innermap.tailMap(fromKey);
	}

	@Override
	public ACoordinate<ASurveyAttribute, AValue> firstKey() {
		return innermap.firstKey();
	}

	@Override
	public ACoordinate<ASurveyAttribute, AValue> lastKey() {
		return innermap.lastKey();
	}

	@Override
	public Set<ACoordinate<ASurveyAttribute, AValue>> keySet() {
		return innermap.keySet();
	}

	@Override
	public Collection<Double> values() {
		return innermap.values();
	}

	@Override
	public Set<java.util.Map.Entry<ACoordinate<ASurveyAttribute, AValue>, Double>> entrySet() {
		return innermap.entrySet();
	}

}
