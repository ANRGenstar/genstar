package gospl.distribution.matrix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;

public abstract class ASegmentedNDimensionalMatrix<T extends Number> implements
		INDimensionalMatrix<APopulationAttribute, APopulationValue, T> {

	protected final Set<AFullNDimensionalMatrix<T>> jointDistributionSet;
	
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

	@Override
	public GSSurveyType getMetaDataType() {
		Set<GSSurveyType> mdtSet = jointDistributionSet.stream().map(jd -> jd.getMetaDataType()).collect(Collectors.toSet());
		if(mdtSet.size() != 1)
			return null;
		return mdtSet.iterator().next();
	}
	
	@Override
	public boolean isSegmented(){
		return true;
	}
	
	// ---------------- Getters ---------------- //

	@Override
	public Set<APopulationAttribute> getDimensions() {
		return jointDistributionSet.stream().flatMap(jd -> jd.getDimensions().stream()).collect(Collectors.toSet());
	}

	@Override
	public Map<APopulationAttribute, Set<APopulationValue>> getDimensionsAsAttributesAndValues() {
		Map<APopulationAttribute, Set<APopulationValue>>  res = new HashMap<>();
		for (AFullNDimensionalMatrix<T> m: jointDistributionSet) {
			res.putAll(m.getDimensionsAsAttributesAndValues());
		}
		return res;
	}
	
	@Override
	public APopulationAttribute getDimension(APopulationValue aspect) {
		return getDimensions().stream().filter(d -> d.getValues().contains(aspect)).findFirst().get();
	}
	
	@Override
	public Set<APopulationValue> getAspects() {
		return getDimensions().stream().flatMap(d -> d.getValues().stream()).collect(Collectors.toSet());
	}

	@Override
	public Set<APopulationValue> getAspects(APopulationAttribute dimension) {
		return Collections.unmodifiableSet(dimension.getValues());
	}

	@Override
	public int size() {
		return jointDistributionSet.stream().mapToInt(AFullNDimensionalMatrix::size).sum();
	}
	
	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> getEmptyCoordinate() {
		return jointDistributionSet.iterator().next().getEmptyCoordinate();
	}
	
	// ---------------------- Matrix accessors ---------------------- //
	
	/**
	 * Return the partitioned view of this matrix, i.e. the collection
	 * of inner full matrices
	 * 
	 * @return
	 */
	public Collection<AFullNDimensionalMatrix<T>> getMatrices(){
		return Collections.unmodifiableSet(jointDistributionSet);
	}
	
	@Override
	public Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> getMatrix(){
		Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> matrix = new HashMap<>();
		for(AFullNDimensionalMatrix<T> jd : jointDistributionSet)
			matrix.putAll(jd.getMatrix());
		return matrix;
	}
	
	public LinkedHashMap<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> getOrderedMatrix(){
		LinkedHashMap<ACoordinate<APopulationAttribute, APopulationValue>, AControl<T>> matrix = 
				new LinkedHashMap<>();
		for(AFullNDimensionalMatrix<T> jd : jointDistributionSet)
			matrix.putAll(jd.getOrderedMatrix());
		return matrix;
	}
	
	@Override
	public AControl<T> getVal() {
		AControl<T> result = getNulVal();
		for(AFullNDimensionalMatrix<T> distribution : jointDistributionSet) {
			for (AControl<T> control: distribution.matrix.values()) {
				getSummedControl(result, control);	
			}
		}
		return result;
	}
	
	@Override
	public AControl<T> getVal(ACoordinate<APopulationAttribute, APopulationValue> coordinate) {
		return getVal(coordinate.values());
	}

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

	@Override
	public AControl<T> getVal(Collection<APopulationValue> aspects) {
		
		// Setup output with identity product value
		AControl<T> conditionalProba = this.getIdentityProductVal();
		
		// Setup a record of visited dimension to avoid duplicated probabilities
		Set<APopulationAttribute> remainingDimension = aspects.stream()
				.map(aspect -> aspect.getAttribute()).collect(Collectors.toSet());
		
		// Select matrices that contains at least one concerned dimension and ordered them
		// in decreasing order of the number of matches
		List<AFullNDimensionalMatrix<T>> concernedMatrices = jointDistributionSet.stream()
				.filter(matrix -> matrix.getDimensions().stream().anyMatch(dimension -> remainingDimension.contains(dimension)))
				.sorted((m1, m2) -> m1.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count() >=
						m2.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count() ? -1 : 1)
				.collect(Collectors.toList());
		
		for(AFullNDimensionalMatrix<T> mat : concernedMatrices){
			if(!mat.getDimensions().stream().anyMatch(dimension -> remainingDimension.contains(dimension)))
				continue;
			// Setup concerned values
			Set<APopulationValue> concernedValues = aspects.stream()
					.filter(a -> mat.getDimensions().contains(a.getAttribute()))
					.collect(Collectors.toSet());
			// Update conditional probability
			conditionalProba.multiply(mat.getVal(concernedValues));
			// Update visited probability
			remainingDimension.removeAll(concernedValues
					.stream().map(a -> a.getAttribute()).collect(Collectors.toSet()));
		}
		return conditionalProba;
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
	
	@Override
	public String toString(){
		String s = "Segmented matrix with "+jointDistributionSet.size()+" inner full matrices:\n";
		s += jointDistributionSet.stream().map(m -> m.toString()+"\n").reduce("", (s1, s2) -> s1 + s2);
		return s;
	}
	
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

	/**
	 * Returns the matrices which involve this val
	 * @param val
	 */
	public Set<AFullNDimensionalMatrix<T>> getMatricesInvolving(APopulationAttribute att) {
		return this.jointDistributionSet.stream().filter(matrix -> matrix.getDimensions().contains(att)).collect(Collectors.toSet());
	}
	
}
