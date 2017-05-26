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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;

/**
 * Represent the higher order abstraction for segmented matrix: it is a record
 * of matrices with diverging dimensional coverage. Look at the exemple, where
 * matrices are refer as {@code m} and dimensions as {@code d}:
 * <p>
 * {@code
 * m_seg = m1,m2,m3;} the segmented matrix contains 3 inner full matrices <br>
 * {@code
 * m_seg.getDimensions() == d1,d2,d3,d4,d5;} the segmented matrix contains 5 dimensions<br>
 * {@code
 * m1.getDimensions() == d1,d2,d3;} 1st inner full matrix contains 3 dimensions<br>
 * {@code
 * m2.getDimensions() == d1,d4;} 2nd inner full matrix contains 2 dimensions<br>
 * {@code
 * m1.getDimensions() == d5;} 3th inner full matrix contains only one dimensions <br> 
 * <p>
 * 
 * WARNING: Abstract segmented matrix only concerns distribution matrix, i.e.
 * matrix that store probabilities
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public abstract class ASegmentedNDimensionalMatrix<T extends Number> implements ISegmentedNDimensionalMatrix<T> {

	protected final Set<AFullNDimensionalMatrix<T>> jointDistributionSet;


	protected String label = null;
	
	protected List<String> genesis = new LinkedList<>();
	
	// -------------------- Constructor -------------------- //

	private ASegmentedNDimensionalMatrix(){
		jointDistributionSet = new HashSet<>();
	}

	public ASegmentedNDimensionalMatrix(Set<AFullNDimensionalMatrix<T>> jointDistributionSet) throws IllegalDistributionCreation {
		this();
		if(jointDistributionSet.isEmpty())
			throw new IllegalArgumentException("Not any distributions to fill in the conditional distribution"); 
		this.jointDistributionSet.addAll(jointDistributionSet);
		if(jointDistributionSet.stream().map(jd -> jd.getMetaDataType()).collect(Collectors.toSet()).size() > 1)
			throw new IllegalDistributionCreation("Divergent frame of reference among sub joint distribution");
	}

	// ------------------------- META DATA ------------------------ //

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getMetaDataType()
	 */
	@Override
	public GSSurveyType getMetaDataType() {
		Set<GSSurveyType> mdtSet = jointDistributionSet.stream().map(jd -> jd.getMetaDataType()).collect(Collectors.toSet());
		if(mdtSet.size() != 1)
			return null;
		return mdtSet.iterator().next();
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#isSegmented()
	 */
	@Override
	public boolean isSegmented(){
		return true;
	}

	// ---------------- Getters ---------------- //

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getDimensions()
	 */
	@Override
	public Set<APopulationAttribute> getDimensions() {
		return jointDistributionSet.stream().flatMap(jd -> jd.getDimensions().stream()).collect(Collectors.toSet());
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getDimensionsAsAttributesAndValues()
	 */
	@Override
	public Map<APopulationAttribute, Set<APopulationValue>> getDimensionsAsAttributesAndValues() {
		Map<APopulationAttribute, Set<APopulationValue>>  res = new HashMap<>();
		for (AFullNDimensionalMatrix<T> m: jointDistributionSet) {
			res.putAll(m.getDimensionsAsAttributesAndValues());
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getDimension(core.metamodel.pop.APopulationValue)
	 */
	@Override
	public APopulationAttribute getDimension(APopulationValue aspect) {
		return getDimensions().stream().filter(d -> d.getValues().contains(aspect)).findFirst().get();
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getAspects()
	 */
	@Override
	public Set<APopulationValue> getAspects() {
		return getDimensions().stream().flatMap(d -> d.getValues().stream()).collect(Collectors.toSet());
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getAspects(core.metamodel.pop.APopulationAttribute)
	 */
	@Override
	public Set<APopulationValue> getAspects(APopulationAttribute dimension) {
		return Collections.unmodifiableSet(dimension.getValues());
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#size()
	 */
	@Override
	public int size() {
		return jointDistributionSet.stream().mapToInt(AFullNDimensionalMatrix::size).sum();
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getEmptyCoordinate()
	 */
	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> getEmptyCoordinate() {
		return jointDistributionSet.iterator().next().getEmptyCoordinate();
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getCoordinates(java.util.Set)
	 */
	@Override
	public Collection<ACoordinate<APopulationAttribute, APopulationValue>> getCoordinates(Set<APopulationValue> values){
		Map<APopulationAttribute, Set<APopulationValue>> attValues = values.stream()
				.collect(Collectors.groupingBy(value -> value.getAttribute(),
						Collectors.mapping(Function.identity(), Collectors.toSet())));
		List<AFullNDimensionalMatrix<T>> concernedJointDistributions = jointDistributionSet.stream()
				.filter(matrix -> matrix.getDimensions().stream()
						.anyMatch(dim -> attValues.keySet().stream().anyMatch(att -> dim.isLinked(att))))
				.collect(Collectors.toList());
		if(concernedJointDistributions.size() == 1)
			return concernedJointDistributions.get(0).getCoordinates(values);

		Map<APopulationAttribute, Set<APopulationValue>> overallMapLink = new HashMap<>();
		Set<APopulationAttribute> overallAttributes = concernedJointDistributions
				.stream().flatMap(mat -> mat.getDimensions().stream()).collect(Collectors.toSet());
		for(APopulationAttribute attribute : attValues.keySet()){
			for(APopulationAttribute mapAtt : overallAttributes.stream()
					.filter(att -> att.isLinked(attribute)).collect(Collectors.toList())){
				Set<APopulationValue> mapVals = new HashSet<>();
				for(APopulationValue val : attValues.get(attribute))
					mapVals.addAll(mapAtt.findMappedAttributeValues(val));
				overallMapLink.put(mapAtt, mapVals);
			}
		}

		Collection<ACoordinate<APopulationAttribute, APopulationValue>> coords = new ArrayList<>();
		for(ACoordinate<APopulationAttribute, APopulationValue> coord : concernedJointDistributions
				.stream().flatMap(matrix -> matrix.getMatrix().keySet().stream()).collect(Collectors.toSet())){ 
			if(coord.getMap().entrySet().stream().filter(entry -> overallMapLink.keySet().contains(entry.getKey()))
					.allMatch(entry -> overallMapLink.get(entry.getKey()).contains(entry.getValue())))
				coords.add(coord);
		}

		return coords;
	}

	// ---------------------- Matrix accessors ---------------------- //

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getMatrices()
	 */
	@Override
	public Collection<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatrices(){
		return Collections.unmodifiableSet(jointDistributionSet);
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getMatrix()
	 */
	@Override
	public Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> getMatrix(){
		Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> matrix = new HashMap<>();
		for(AFullNDimensionalMatrix<T> jd : jointDistributionSet)
			matrix.putAll(jd.getMatrix());
		return matrix;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getOrderedMatrix()
	 */
	@Override
	public LinkedHashMap<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> getOrderedMatrix(){
		LinkedHashMap<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> matrix = 
				new LinkedHashMap<>();
		for(AFullNDimensionalMatrix<T> jd : jointDistributionSet)
			matrix.putAll(jd.getOrderedMatrix());
		return matrix;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getVal()
	 */
	@Override
	public AControl<T> getVal() {
		AControl<T> result = this.getNulVal();
		for(AFullNDimensionalMatrix<T> distribution : jointDistributionSet) {
			for (AControl<T> control: distribution.matrix.values()) {
				getSummedControl(result, control);	
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getVal(gospl.distribution.matrix.coordinate.ACoordinate)
	 */
	@Override
	public AControl<T> getVal(ACoordinate<APopulationAttribute, APopulationValue> coordinate) {
		return getVal(new HashSet<>(coordinate.values()));
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getVal(core.metamodel.pop.APopulationValue)
	 */
	@Override
	public AControl<T> getVal(APopulationValue aspect) throws IllegalNDimensionalMatrixAccess {
		AControl<T> val = null;
		for(AFullNDimensionalMatrix<T> distribution : jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().contains(aspect.getAttribute())).collect(Collectors.toList()))
			if(val == null)
				val = distribution.getVal(aspect);
			else if(!val.getValue().equals(distribution.getVal(aspect).getValue()))
				throw new IllegalNDimensionalMatrixAccess("Incongruent probability in underlying distributions");
		return val;
	}

	

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getVal(core.metamodel.pop.APopulationValue)
	 */
	@Override
	public final AControl<T> getVal(APopulationValue... aspects) {
		return getVal(Arrays.asList(aspects));
	}



	// ---------------------- Inner utilities ---------------------- //

	/**
	 * Inner utility method that add an encapsulated {@link AControl} value into the first
	 * given in argument
	 * 
	 * @param controlOne
	 * @param controlTwo
	 * @return
	 */
	private AControl<T> getSummedControl(AControl<T> controlOne, AControl<T> controlTwo){
		return controlOne.add(controlTwo);
	}

	// ----------------------- utility ----------------------- //

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#toString()
	 */
	@Override
	public String toString(){
		String s = "Segmented matrix with "+jointDistributionSet.size()+" inner full matrices:\n";
		s += jointDistributionSet.stream().map(m -> m.toString()+"\n").reduce("", (s1, s2) -> s1 + s2);
		return s;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#toCsv(char)
	 */
	@Override
	public String toCsv(char csvSeparator){
		String s = "";
		for(AFullNDimensionalMatrix<T> matrix : jointDistributionSet){
			String matrixHeader = "-- Matrix: "+matrix.getDimensions().size()+" dimensions and "
					+matrix.getDimensions().stream().map(dim -> dim.getValues()).mapToInt(Collection::size).sum()
					+" aspects (theoretical size:"+this.size()+")--\n"; 
			if(s.isEmpty())
				s += matrixHeader+matrix.toCsv(csvSeparator);
			else
				s += "\n"+matrixHeader+matrix.toCsv(csvSeparator);
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getMatricesInvolving(core.metamodel.pop.APopulationAttribute)
	 */
	@Override
	public Set<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatricesInvolving(APopulationAttribute att) {
		return this.jointDistributionSet.stream().filter(matrix -> matrix.getDimensions().contains(att)).collect(Collectors.toSet());
	}

	// ----------------------- String accessors ----------------------- //

	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getCoordinates(java.lang.String)
	 */
	@Override
	public Collection<ACoordinate<APopulationAttribute, APopulationValue>> getCoordinates(String... keyAndVal)
			throws IllegalArgumentException {

		
		return getCoordinates(getValues(keyAndVal));
	}
	


	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getValues(java.lang.String)
	 */
	@Override
	public Set<APopulationValue> getValues(String... keyAndVal) throws IllegalArgumentException {

		Set<APopulationValue> coordinateValues = new HashSet<>();
		
		// collect all the attributes and index their names
		Map<String,APopulationAttribute> name2attribute = getDimensionsAsAttributesAndValues().keySet().stream()
															.collect(Collectors.toMap(APopulationAttribute::getAttributeName,Function.identity()));

		if (keyAndVal.length % 2 != 0) {
			throw new IllegalArgumentException("you should pass pairs of attribute name and corresponding value, "
					+ "such as attribute 1 name, value for attribute 1, attribute 2 name, value for attribute 2...");
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



	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getCoordinate(java.lang.String)
	 */
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


	/* (non-Javadoc)
	 * @see gospl.distribution.matrix.ISegmentedNDimensionalMatrix#getDimension(java.lang.String)
	 */
	@Override
	public APopulationAttribute getDimension(String name) throws IllegalArgumentException {
		
		for (AFullNDimensionalMatrix<T> m: jointDistributionSet) {
			for (APopulationAttribute a: m.getDimensions()) {
				if (a.getAttributeName().equals(name)) {
					return a;
				}
			}
		}
		
		throw new IllegalArgumentException(
				"No dimension named "+name+
				"; available dimensions are: "+
				jointDistributionSet
					.stream().map(
							m -> m.getDimensions()
									.stream()
									.map(d -> d.getAttributeName())
									.reduce("", (u,t)->u+","+t)
									)
					.reduce("", (u,t)->u+","+t)
				);
		
	}
	

	@Override
	public String getLabel() {
		return label;
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
	
}
