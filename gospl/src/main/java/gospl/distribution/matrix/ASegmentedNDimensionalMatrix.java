package gospl.distribution.matrix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
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
		Map<APopulationAttribute, Collection<APopulationValue>> coordinates = new HashMap<>();
		for(APopulationValue val : aspects){
			if(coordinates.containsKey(val.getAttribute()))
				coordinates.get(val.getAttribute()).add(val);
			else
				coordinates.put(val.getAttribute(), Stream.of(val).collect(Collectors.toSet()));
		}
		AControl<T> conditionalProba = this.getIdentityProductVal();
		Set<APopulationValue> includedProbaDimension = new HashSet<>();
		for(APopulationAttribute att : coordinates.keySet()){
			AControl<T> localProba = getNulVal();
			for(AFullNDimensionalMatrix<T> distribution : jointDistributionSet
					.stream().filter(jd -> jd.getDimensions().contains(att)).collect(Collectors.toList())){
				Set<APopulationAttribute> hookAtt = distribution.getDimensions()
						.stream().filter(d -> includedProbaDimension.contains(d)).collect(Collectors.toSet());
				if(hookAtt.isEmpty()){
					localProba = distribution.getVal(coordinates.get(att));  
				} else {
					Set<APopulationValue> hookVals = hookAtt.stream().flatMap(a -> a.getValues().stream()).collect(Collectors.toSet());
					Set<APopulationValue> localVals = new HashSet<>(hookVals);
					localVals.addAll(coordinates.get(att));
					localProba.multiply(distribution.getVal(localVals)
							.getRowProduct(new ControlFrequency(1d / distribution.getVal(hookVals).getValue().doubleValue())));
				}
			}	
			includedProbaDimension.addAll(coordinates.get(att));
			conditionalProba.multiply(localProba);
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
	
}
