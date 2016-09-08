package gospl.distribution.matrix;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.survey.GosplMetaDataType;

public abstract class ASegmentedNDimensionalMatrix<T extends Number> implements
		INDimensionalMatrix<IAttribute, IValue, T> {

	protected final Set<AFullNDimensionalMatrix<T>> jointDistributionSet;
	
// -------------------- Constructor -------------------- //
	
	private ASegmentedNDimensionalMatrix(){
		jointDistributionSet = new HashSet<>();
	}
	
	public ASegmentedNDimensionalMatrix(Set<AFullNDimensionalMatrix<T>> jointDistributionSet) throws IllegalDistributionCreation {
		this();
		if(jointDistributionSet.isEmpty())
			throw new IllegalDistributionCreation("Not any distributions to fill in the conditional distribution"); 
		this.jointDistributionSet.addAll(jointDistributionSet);
		if(jointDistributionSet.stream().map(jd -> jd.getMetaDataType()).collect(Collectors.toSet()).size() > 1)
			throw new IllegalDistributionCreation("Divergent frame of reference among sub joint distribution");
	}
	
// ------------------------- META DATA ------------------------ //

	@Override
	public GosplMetaDataType getMetaDataType() {
		Set<GosplMetaDataType> mdtSet = jointDistributionSet.stream().map(jd -> jd.getMetaDataType()).collect(Collectors.toSet());
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
	public Set<IAttribute> getDimensions() {
		return jointDistributionSet.stream().flatMap(jd -> jd.getDimensions().stream()).collect(Collectors.toSet());
	}
	
	@Override
	public IAttribute getDimension(IValue aspect) {
		return getDimensions().stream().filter(d -> d.getValues().contains(aspect)).findFirst().get();
	}
	
	@Override
	public Set<IValue> getAspects() {
		return getDimensions().stream().flatMap(d -> d.getValues().stream()).collect(Collectors.toSet());
	}

	@Override
	public Set<IValue> getAspects(IAttribute dimension) {
		return Collections.unmodifiableSet(dimension.getValues());
	}

	@Override
	public int size() {
		return jointDistributionSet.stream().mapToInt(AFullNDimensionalMatrix::size).sum();
	}
	
	@Override
	public ACoordinate<IAttribute, IValue> getEmptyCoordinate() {
		return jointDistributionSet.iterator().next().getEmptyCoordinate();
	}
	
	// ---------------------- Matrix accessors ---------------------- //
	
	public Collection<AFullNDimensionalMatrix<T>> getMatrices(){
		return Collections.unmodifiableSet(jointDistributionSet);
	}
	
	@Override
	public Map<ACoordinate<IAttribute, IValue>, AControl<T>> getMatrix(){
		Map<ACoordinate<IAttribute, IValue>, AControl<T>> matrix = new HashMap<>();
		for(AFullNDimensionalMatrix<T> jd : jointDistributionSet)
			matrix.putAll(jd.getMatrix());
		return matrix;
	}
	
	@Override
	public AControl<T> getVal(ACoordinate<IAttribute, IValue> coordinate) {
		return getVal(coordinate.values());
	}

	@Override
	public AControl<T> getVal(IValue aspect) throws IllegalNDimensionalMatrixAccess {
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
	public AControl<T> getVal(Collection<IValue> aspects) {
		Map<IAttribute, Collection<IValue>> coordinates = new HashMap<>();
		for(IValue val : aspects){
			if(coordinates.containsKey(val.getAttribute()))
				coordinates.get(val.getAttribute()).add(val);
			else
				coordinates.put(val.getAttribute(), new HashSet<>(Arrays.asList(val)));
		}
		AControl<T> conditionalProba = getIdentityProductVal();
		Set<IValue> includedProbaDimension = new HashSet<>();
		for(IAttribute att : coordinates.keySet()){
			AControl<T> localProba = getNulVal();
			for(AFullNDimensionalMatrix<T> distribution : jointDistributionSet
					.stream().filter(jd -> jd.getDimensions().contains(att)).collect(Collectors.toList())){
				Set<IAttribute> hookAtt = distribution.getDimensions()
						.stream().filter(d -> includedProbaDimension.contains(d)).collect(Collectors.toSet());
				if(hookAtt.isEmpty()){
					localProba = distribution.getVal(coordinates.get(att));  
				} else {
					Set<IValue> hookVals = hookAtt.stream().flatMap(a -> a.getValues().stream()).collect(Collectors.toSet());
					Set<IValue> localVals = new HashSet<>(hookVals);
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
