package gospl.distribution.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * TODO: javadoc
 * <p>
 * WARNING: the inner data collection is concurrent friendly. This implied a low efficiency when no parallelism
 * <p>
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public abstract class AFullNDimensionalMatrix<T extends Number> implements INDimensionalMatrix<APopulationAttribute, APopulationValue, T> {

	private GSSurveyType dataType; 

	private final Map<APopulationAttribute, Set<APopulationValue>> dimensions;
	protected final Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> matrix;

	private ACoordinate<APopulationAttribute, APopulationValue> emptyCoordinate = null;

	protected String label = null;
	
	protected List<String> genesis = new LinkedList<>();
	
	// ----------------------- CONSTRUCTORS ----------------------- //

	/**
	 * TODO: javadoc
	 * 
	 * @param dimensionAspectMap
	 * @param metaDataType
	 */
	public AFullNDimensionalMatrix(Map<APopulationAttribute, Set<APopulationValue>> dimensionAspectMap, GSSurveyType metaDataType) {
		this.dimensions = new HashMap<>(dimensionAspectMap);
		this.matrix = new ConcurrentHashMap<>(dimensions.entrySet().stream()
				.mapToInt(d -> d.getValue().size())
				.reduce(1, (ir, dimSize) -> ir * dimSize) / 4);
		this.dataType = metaDataType;
		this.emptyCoordinate = new GosplCoordinate(Collections.<APopulationValue>emptySet());
		this.label = dimensionAspectMap.keySet().stream().map(dim -> dim.getAttributeName().length()>3?dim.getAttributeName().substring(0, 3):dim.getAttributeName())
				.collect(Collectors.joining(" x "));
	}
	
	public AFullNDimensionalMatrix(Set<APopulationAttribute> attributes, GSSurveyType metaDataType) {
		this(
				attributes.stream().collect(Collectors.toMap(Function.identity(),APopulationAttribute::getValues)),
				metaDataType
				);
	}

	/**
	 * Returns the genesis of the matrix, that is the successive steps that brought it to its 
	 * current state. Useful to expose meaningful error messages to the user.
	 * @return
	 */
	public List<String> getGenesisAsList() {
		return Collections.unmodifiableList(genesis);
	}
		
	/**
	 * Returns the genesis of the matrix, that is the successive steps that brought it to its 
	 * current state. Useful to expose meaningful error messages to the user.
	 * @return
	 */
	public String getGenesisAsString() {
		return String.join("->", genesis);
	}
	
	/**
	 * imports into this matrix the genesis of another one. 
	 * Should be called after creating a matrix to keep a memory of where it comes from.
	 * @param o
	 */
	public void inheritGenesis(AFullNDimensionalMatrix<?> o) {
		genesis.addAll(o.getGenesisAsList());
	}
	
	/**
	 * add one line to the genesis (history) of this matrix. 
	 * This line should better be kept quiet short for readibility.
	 * @param step
	 */
	public void addGenesis(String step) {
		genesis.add(step);
	}
	
	// ------------------------- META DATA ------------------------ //

	@Override
	public boolean isSegmented(){
		return false;
	}

	@Override
	public GSSurveyType getMetaDataType() {
		return dataType;
	}

	public boolean setMetaDataType(GSSurveyType metaDataType) {
		if(dataType == null || !dataType.equals(metaDataType))
			dataType = metaDataType;
		else 
			return false;
		return true;
	}

	/**
	 * Returns a human readable label, or null if undefined.
	 * @return
	 */
	public String getLabel() {
		return this.label;
	}
	
	/**
	 * Sets the label which describes the table.
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	// ---------------------- GLOBAL ACCESSORS ---------------------- //


	@Override
	public final boolean addValue(T value, String... coordinates) {
		return this.addValue(GosplCoordinate.createCoordinate(dimensions.keySet(), coordinates), value);
	}

	@Override
	public final boolean setValue(T value, String... coordinates) {
		return this.setValue(GosplCoordinate.createCoordinate(dimensions.keySet(), coordinates), value);
	}

	
	@Override
	public int size(){
		return matrix.size();
	}

	@Override
	public Set<APopulationAttribute> getDimensions(){
		return Collections.unmodifiableSet(dimensions.keySet());
	}
	
	@Override
	public Map<APopulationAttribute, Set<APopulationValue>> getDimensionsAsAttributesAndValues() {
		return Collections.unmodifiableMap(dimensions);
	}

	@Override
	public APopulationAttribute getDimension(APopulationValue aspect) {
		if(!dimensions.values().stream().flatMap(values -> values.stream()).collect(Collectors.toSet()).contains(aspect))
			throw new NullPointerException("aspect "+aspect+ " does not fit any known dimension");
		return dimensions.entrySet()
				.stream().filter(e -> e.getValue().contains(aspect))
				.findFirst().get().getKey();
	}

	@Override
	public Set<APopulationValue> getAspects(){
		return Collections.unmodifiableSet(dimensions.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
	}

	@Override
	public Set<APopulationValue> getAspects(APopulationAttribute dimension) {
		if(!dimensions.containsKey(dimension))
			throw new NullPointerException("dimension "+dimension+" is not present in the joint distribution");
		return Collections.unmodifiableSet(dimensions.get(dimension));
	}

	@Override
	public Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> getMatrix(){
		return Collections.unmodifiableMap(matrix);
	}
	
	@Override
	public LinkedHashMap<ACoordinate<APopulationAttribute,APopulationValue>, AControl<T>> getOrderedMatrix() {
		return matrix.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> getEmptyCoordinate(){
		return emptyCoordinate;
	}

	///////////////////////////////////////////////////////////////////
	// -------------------------- GETTERS -------------------------- //
	///////////////////////////////////////////////////////////////////

	@Override
	public AControl<T> getVal() {
		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.values())
			getSummedControl(result, control);
		return result;
	}
	
	@Override
	public AControl<T> getVal(ACoordinate<APopulationAttribute, APopulationValue> coordinate) {
		
		AControl<T> res = this.matrix.get(coordinate);
		
		if (res == null) {
			if (isCoordinateCompliant(coordinate)) {
				// return the default null value
				return this.getNulVal();
			} else {
				throw new NullPointerException("Coordinate "+coordinate+" is absent from this control table ("+this.hashCode()+")");
			}
		} 
			
		return res;
		 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AControl<T> getVal(APopulationValue aspect) {
		if(!matrix.keySet().stream().anyMatch(coord -> coord.contains(aspect)))
			throw new NullPointerException("Aspect "+aspect+" is absent from this control table ("+this.hashCode()+")");
		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.entrySet().stream()
				.filter(e -> e.getKey().values().contains(aspect))
				.map(Entry::getValue).collect(Collectors.toSet()))
			getSummedControl(result, control);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of parallelism through {@link Stream#parallel()}
	 */
	@Override
	public AControl<T> getVal(Collection<APopulationValue> aspects) {
		if(aspects.stream().allMatch(a -> matrix.keySet().stream().noneMatch(coord -> coord.contains(a))))
			throw new NullPointerException("Aspect collection "+Arrays.toString(aspects.toArray())+" of size "
					+ aspects.size()+" is absent from this matrix"
					+ " (size = "+this.size()+" - attribute = "+Arrays.toString(this.getDimensions().toArray())+")");

		Map<APopulationAttribute, Set<APopulationValue>> attAsp = aspects.stream()
				.collect(Collectors.groupingBy(aspect -> aspect.getAttribute(),
				Collectors.mapping(Function.identity(), Collectors.toSet())));
		
		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.entrySet().parallelStream()
				.filter(e -> attAsp.entrySet()
						.stream().allMatch(aa -> aa.getValue()
								.stream().anyMatch(a -> e.getKey().contains(a))))
				.map(Entry::getValue).collect(Collectors.toSet()))
			getSummedControl(result, control);
		return result;
	}
	
	public final AControl<T> getVal(String ... coordinates) {
		
		Collection<APopulationValue> l = new LinkedList<>();
		
		// collect all the attributes and index their names
		Map<String,APopulationAttribute> name2attribute = dimensions.keySet().stream()
															.collect(Collectors.toMap(APopulationAttribute::getAttributeName,Function.identity()));

		if (coordinates.length % 2 != 0) {
			throw new IllegalArgumentException("values should be passed in even count, such as attribute 1 name, value for attribute 1, attribute 2 name, value for attribute 2...");
		}
		
		// lookup values
		for (int i=0; i<coordinates.length; i=i+2) {
			
			final String attributeName = coordinates[i];
			final String attributeValueStr = coordinates[i+1];
			
			APopulationAttribute attribute = name2attribute.get(attributeName);
			if (attribute == null)
				throw new IllegalArgumentException("unknown attribute "+attributeName);
			l.add(attribute.getValue(attributeValueStr)); // will raise exception if the value is not ok
			
		}
		
		return getVal(l);
	}
	
	public final AControl<T> getVal(APopulationValue ... aspects) {
		return getVal(Arrays.asList(aspects));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ----------------------- COORDINATE MANAGEMENT ----------------------- //
	///////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isCoordinateCompliant(ACoordinate<APopulationAttribute, APopulationValue> coordinate) {
		List<APopulationAttribute> dimensionsAspects = new ArrayList<>();
		for(APopulationValue aspect : coordinate.values()){
			for(APopulationAttribute dim : dimensions.keySet()){
				if(dimensions.containsKey(dim)) {
					if(dimensions.get(dim).contains(aspect))
						dimensionsAspects.add(dim);
				} else if(dim.getEmptyValue() != null && dim.getEmptyValue().equals(aspect))
					dimensionsAspects.add(dim);
			}
		}
		Set<APopulationAttribute> dimSet = new HashSet<>(dimensionsAspects);
		if(dimensionsAspects.size() == dimSet.size())
			return true;
		System.out.println(Arrays.toString(dimensionsAspects.toArray()));
		return false;
	}
	
	@Override
	public Collection<ACoordinate<APopulationAttribute, APopulationValue>> getCoordinates(Set<APopulationValue> values){
		Map<APopulationAttribute, Set<APopulationValue>> attValues = values.stream()
				.filter(val -> this.getDimensions().contains(val.getAttribute()))
				.collect(Collectors.groupingBy(value -> value.getAttribute(),
				Collectors.mapping(Function.identity(), Collectors.toSet())));
		return this.matrix.keySet().stream().filter(coord -> attValues.values()
				.stream().allMatch(attVals -> attVals.stream().anyMatch(val -> coord.contains(val))))
				.collect(Collectors.toList());
	}

	private AControl<T> getSummedControl(AControl<T> controlOne, AControl<T> controlTwo){
		return controlOne.add(controlTwo);
	}

	// -------------------------- UTILITY -------------------------- //

	@Override
	public String toString(){
		int theoreticalSpaceSize = this.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2);
		StringBuffer sb = new StringBuffer();
		sb.append("-- Matrix: ").append(dimensions.size()).append(" dimensions and ").append(dimensions.values().stream().mapToInt(Collection::size).sum())
					.append(" aspects (theoretical size:").append(theoreticalSpaceSize).append(")--\n");
		AControl<T> empty = getNulVal();
		for(APopulationAttribute dimension : dimensions.keySet()){
			sb.append(" -- dimension: ").append(dimension.getAttributeName());
			sb.append(" with ").append(dimensions.get(dimension).size()).append(" aspects -- \n");
			for(APopulationValue aspect : dimensions.get(dimension)) {
				AControl<T> value = null;
				try {
					value = getVal(aspect);
				} catch (NullPointerException e) {
					//e.printStackTrace();
					value = empty;
				}
				sb.append("| ").append(aspect).append(": ").append(value).append("\n");
			}
		}
		sb.append(" ----------------------------------- \n");
		return sb.toString();
	}

	@Override
	public String toCsv(char csvSeparator) {
		List<APopulationAttribute> atts = new ArrayList<>(getDimensions());
		AControl<T> emptyVal = getNulVal();
		String csv = "";
		for(APopulationAttribute att :atts){
			if(!csv.isEmpty())
				csv += csvSeparator;
			csv+=att.getAttributeName();
		}
		csv += csvSeparator+"value\n";
		for(ACoordinate<APopulationAttribute, APopulationValue> coordVal : matrix.keySet()){
			String csvLine = "";
			for(APopulationAttribute att :atts){
				if(!csvLine.isEmpty())
					csvLine += csvSeparator;
				if(!coordVal.values()
						.stream().anyMatch(asp -> asp.getAttribute().equals(att)))
					csvLine += " ";
				else {
					String val = coordVal.values()
							.stream().filter(asp -> asp.getAttribute().equals(att))
							.findFirst().get().getStringValue();
					if(val.isEmpty())
						val = "empty value";
					csvLine += val;
				}
			}
			try {
				csv += csvLine+csvSeparator+getVal(coordVal).getValue()+"\n";
			} catch (NullPointerException e) {
				e.printStackTrace();
				csv += csvLine+csvSeparator+emptyVal+"\n";
			}
		}
		return csv;
	}


	@Override
	public boolean checkAllCoordinatesHaveValues() {
		
		return matrix.size() == dimensions.values().stream().mapToInt(Set::size).reduce(1, (a, b) -> a * b);

	}
	

	@Override
	public boolean checkGlobalSum() {
		
		switch (dataType) {
			
			case GlobalFrequencyTable:
				return (Double)getVal().getValue() == 1.0;
			case LocalFrequencyTable:
			case Sample:
			case ContingencyTable:
					return true;
			default:
				throw new IllegalStateException("unknown state "+dataType);
			
		}
		
	}


	@Override
	public APopulationAttribute getDimension(String name) throws IllegalArgumentException {
		
		for (APopulationAttribute a: dimensions.keySet())
			if (a.getAttributeName().equals(name))
				return a;

		throw new IllegalArgumentException(
				"unknown dimension "+name+"; available dimensions are "+
				dimensions.keySet().stream().map(d -> d.getAttributeName()).reduce("", (u,t)->u+","+t)
				);
	}


	@Override
	public Collection<ACoordinate<APopulationAttribute, APopulationValue>> getCoordinates(String... keyAndVal)
			throws IllegalArgumentException {

		return getCoordinates(getAttributes(keyAndVal));
	}
	


	@Override
	public Set<APopulationValue> getAttributes(String... keyAndVal) throws IllegalArgumentException {

		Set<APopulationValue> coordinateValues = new HashSet<>();
		
		// collect all the attributes and index their names
		Map<String,APopulationAttribute> name2attribute = getDimensionsAsAttributesAndValues().keySet().stream()
															.collect(Collectors.toMap(APopulationAttribute::getAttributeName,Function.identity()));

		if (keyAndVal.length/2 != name2attribute.size()) {
			throw new IllegalArgumentException("you should pass pairs of attribute name and corresponding value, such as attribute 1 name, value for attribute 1, attribute 2 name, value for attribute 2...");
		}
		
		// lookup values
		for (int i=0; i<keyAndVal.length; i=i+2) {
			final String attributeName = keyAndVal[i];
			final String attributeValueStr = keyAndVal[i+1];
			
			APopulationAttribute attribute = name2attribute.get(attributeName);
			if (attribute == null)
				throw new IllegalArgumentException("unknown attribute "+attributeName);
			coordinateValues.add(attribute.getValue(attributeValueStr)); // will raise exception if the value is not ok

		}
		
		return coordinateValues;
	}
	

	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> getCoordinate(String... keyAndVal)
			throws IllegalArgumentException {
		
		Collection<ACoordinate<APopulationAttribute, APopulationValue>> s = getCoordinates(keyAndVal);
				
		if (s.size() > 1) 
			throw new IllegalArgumentException("these coordinates do not map to a single cell of the matrix");
		
		if (s.isEmpty()) 
			throw new IllegalArgumentException("these coordinates do not map to any cell in the matrix");

		
		return s.iterator().next();
	}
	

}
