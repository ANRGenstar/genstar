package gospl.distribution.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import core.io.survey.GSSurveyType;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public abstract class AFullNDimensionalMatrix<T extends Number> implements INDimensionalMatrix<ASurveyAttribute, ASurveyValue, T> {

	private GSSurveyType dataType; 

	private final Map<ASurveyAttribute, Set<ASurveyValue>> dimensions;
	protected final Map<ACoordinate<ASurveyAttribute, ASurveyValue>, AControl<T>> matrix;

	private ACoordinate<ASurveyAttribute, ASurveyValue> emptyCoordinate = null;

	protected String label = null;
	
	// ----------------------- CONSTRUCTORS ----------------------- //

	public AFullNDimensionalMatrix(Map<ASurveyAttribute, Set<ASurveyValue>> dimensionAspectMap, GSSurveyType metaDataType) {
		this.dimensions = new HashMap<>(dimensionAspectMap);
		this.matrix = new HashMap<>(dimensions.entrySet().stream()
				.mapToInt(d -> d.getValue().size())
				.reduce(1, (ir, dimSize) -> ir * dimSize) / 4);
		this.dataType = metaDataType;
		this.emptyCoordinate = new GosplCoordinate(Collections.<ASurveyValue>emptySet());
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
	public int size(){
		return matrix.size();
	}

	@Override
	public Set<ASurveyAttribute> getDimensions(){
		return Collections.unmodifiableSet(dimensions.keySet());
	}

	@Override
	public ASurveyAttribute getDimension(ASurveyValue aspect) {
		if(!dimensions.values().stream().flatMap(values -> values.stream()).collect(Collectors.toSet()).contains(aspect))
			throw new NullPointerException("aspect "+aspect+ " does not fit any known dimension");
		return dimensions.entrySet()
				.stream().filter(e -> e.getValue().contains(aspect))
				.findFirst().get().getKey();
	}

	@Override
	public Set<ASurveyValue> getAspects(){
		return Collections.unmodifiableSet(dimensions.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
	}

	@Override
	public Set<ASurveyValue> getAspects(ASurveyAttribute dimension) {
		if(!dimensions.containsKey(dimension))
			throw new NullPointerException("dimension "+dimension+" is not present in the joint distribution");
		return Collections.unmodifiableSet(dimensions.get(dimension));
	}

	@Override
	public Map<ACoordinate<ASurveyAttribute, ASurveyValue>, AControl<T>> getMatrix(){
		return Collections.unmodifiableMap(matrix);
	}
	
	@Override
	public ACoordinate<ASurveyAttribute, ASurveyValue> getEmptyCoordinate(){
		return emptyCoordinate;
	}

	///////////////////////////////////////////////////////////////////
	// -------------------------- GETTERS -------------------------- //
	///////////////////////////////////////////////////////////////////

	@Override
	public AControl<T> getVal(ACoordinate<ASurveyAttribute, ASurveyValue> coordinate) {
		if(!matrix.containsKey(coordinate))
			throw new NullPointerException("Coordinate "+coordinate+" is absent from this control table ("+this.hashCode()+")");
		return this.matrix.get(coordinate);
	}

	@Override
	public AControl<T> getVal(ASurveyValue aspect) {
		if(!matrix.keySet().stream().anyMatch(coord -> coord.contains(aspect)))
			throw new NullPointerException("Aspect "+aspect+" is absent from this control table ("+this.hashCode()+")");
		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.entrySet().parallelStream()
				.filter(e -> e.getKey().values().contains(aspect))
				.map(Entry::getValue).collect(Collectors.toSet()))
			getSummedControl(result, control);
		return result;
	}

	@Override
	public AControl<T> getVal(Collection<ASurveyValue> aspects) {
		if(aspects.stream().allMatch(a -> !matrix.keySet().stream().anyMatch(coord -> coord.contains(a))))
			throw new NullPointerException("Aspect "+aspects+" is absent from this control table ("+this.getClass().getSimpleName()+" - "+this.hashCode()+")");

		Map<ASurveyAttribute, Set<ASurveyValue>> attAsp = new HashMap<>();
		for(ASurveyValue val : aspects){
			ASurveyAttribute att = val.getAttribute();
			if(attAsp.containsKey(att))
				attAsp.get(att).add(val);
			else {
				Set<ASurveyValue> valSet = new HashSet<>();
				valSet.add(val);
				attAsp.put(att, valSet);
			}
		}

		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.entrySet().parallelStream()
				.filter(e -> attAsp.entrySet()
						.stream().allMatch(aa -> aa.getValue()
								.stream().anyMatch(a -> e.getKey().contains(a))))
				.map(Entry::getValue).collect(Collectors.toSet()))
			getSummedControl(result, control);
		return result;
	}


	@Override
	public AControl<T> getVal() {
		AControl<T> result = getNulVal();
		for(AControl<T> control : this.matrix.values())
			getSummedControl(result, control);
		return result;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ----------------------- COORDINATE MANAGEMENT ----------------------- //
	///////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isCoordinateCompliant(ACoordinate<ASurveyAttribute, ASurveyValue> coordinate) {
		List<ASurveyAttribute> dimensionsAspects = new ArrayList<>();
		for(ASurveyValue aspect : coordinate.values()){
			for(ASurveyAttribute dim : dimensions.keySet()){
				if(dimensions.containsKey(dim)) {
					if(dimensions.get(dim).contains(aspect))
						dimensionsAspects.add(dim);
				} else if(dim.getEmptyValue() != null && dim.getEmptyValue().equals(aspect))
					dimensionsAspects.add(dim);
			}
			/* FUCK ONE LINER
			dimensionsAspects.addAll(dimensions.keySet()
					.stream().filter(d -> dimensions.get(d).contains(aspect) 
							|| d.getEmptyValue() != null && d.getEmptyValue().equals(aspect))
					.collect(Collectors.toList()));
					*/
		}
		Set<ASurveyAttribute> dimSet = new HashSet<>(dimensionsAspects);
		if(dimensionsAspects.size() == dimSet.size())
			return true;
		System.out.println(Arrays.toString(dimensionsAspects.toArray()));
		return false;
	}

	private AControl<T> getSummedControl(AControl<T> controlOne, AControl<T> controlTwo){
		return controlOne.add(controlTwo);
	}

	// -------------------------- UTILITY -------------------------- //

	@Override
	public String toString(){
		int theoreticalSpaceSize = this.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2);
		String s = "-- Matrix: "+dimensions.size()+" dimensions and "+dimensions.values().stream().mapToInt(Collection::size).sum()
				+" aspects (theoretical size:"+theoreticalSpaceSize+")--\n";
		AControl<T> empty = getNulVal();
		for(ASurveyAttribute dimension : dimensions.keySet()){
			s += " -- dimension: "+dimension.getAttributeName()+" with "+dimensions.get(dimension).size()+" aspects -- \n";
			for(ASurveyValue aspect : dimensions.get(dimension))
				try {
					s += "| "+aspect+": "+getVal(aspect)+"\n";
				} catch (NullPointerException e) {
					e.printStackTrace();
					s += "| "+aspect+": "+empty+"\n";
				}
		}
		s += " ----------------------------------- \n";
		return s;
	}

	@Override
	public String toCsv(char csvSeparator) {
		List<ASurveyAttribute> atts = new ArrayList<>(getDimensions());
		AControl<T> emptyVal = getNulVal();
		String csv = "";
		for(ASurveyAttribute att :atts){
			if(!csv.isEmpty())
				csv += csvSeparator;
			csv+=att.getAttributeName();
		}
		csv += csvSeparator+"value\n";
		for(ACoordinate<ASurveyAttribute, ASurveyValue> coordVal : matrix.keySet()){
			String csvLine = "";
			for(ASurveyAttribute att :atts){
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
	
}
