package gospl.distribution;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * A set of joint distributions with links of dependancy between them. 
 * 
 * @author Kevin Chapuis
 */
public class GosplConditionalDistribution extends ASegmentedNDimensionalMatrix<Double> {

	Logger logger = LogManager.getLogger();

	protected GosplConditionalDistribution(Set<AFullNDimensionalMatrix<Double>> jointDistributionSet) throws IllegalDistributionCreation {
		super(jointDistributionSet);
	}
	

	// --------------- Main contract --------------- //

	/**
	 * {@inheritDoc}
	 * <p>
	 * Provide the most informed control associated with the given set of aspects
	 * <br>
	 * TODO: describe the whole process
	 */
	@Override
	public AControl<Double> getVal(Collection<APopulationValue> aspects) {
		// Setup output with identity product value
		AControl<Double> conditionalProba = this.getIdentityProductVal();
		// Setup a record of visited dimension to avoid duplicated probabilities
		Set<APopulationAttribute> remainingDimension = aspects.stream()
				.map(aspect -> aspect.getAttribute()).collect(Collectors.toSet());

		// Select matrices that contains at least one concerned dimension and ordered them
		// in decreasing order of the number of matches
		List<AFullNDimensionalMatrix<Double>> concernedMatrices = jointDistributionSet.stream()
				.filter(matrix -> matrix.getDimensions().stream().anyMatch(dimension -> remainingDimension.contains(dimension)))
				.sorted((m1, m2) -> m1.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count() >=
				m2.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count() ? -1 : 1)
				.collect(Collectors.toList());

		// Store visited dimension to compute conditional probabilities
		Set<APopulationAttribute> assignedDimension = new HashSet<>();

		for(AFullNDimensionalMatrix<Double> mat : concernedMatrices){
			if(mat.getDimensions().stream()
					.noneMatch(dimension -> remainingDimension.contains(dimension)))
				continue;

			// Setup concerned values
			Set<APopulationValue> concernedValues = aspects.stream()
					.filter(a -> mat.getDimensions().contains(a.getAttribute()))
					.collect(Collectors.toSet());

			// COMPUTE CONDITIONAL PROBABILITY
			// Setup conditional values (known probability to compute conditional probability)
			Set<APopulationValue> conditionalValues = concernedValues.stream()
					.filter(val -> assignedDimension.stream().anyMatch(dim -> dim.getValues().contains(val)))
					.collect(Collectors.toSet());

			// add bottom up & top down conditional values
			// WARNING: make false assumption about probability manipulation
			// Hence issues arise: Either conditional probability are over or under estimated, 
			// because referent binding is not force to be complete (a set of value referees to some other set, 
			// while only a subset can be of target here)
			Map<Set<APopulationValue>, AControl<Double>> bottomup = this.estimateBottomUpReferences(mat, aspects, assignedDimension);
			Map<Set<APopulationValue>, AControl<Double>> topdown = this.estimateTopDownReferences(mat, aspects, assignedDimension); 
			
			conditionalValues.addAll(Stream.concat(bottomup.keySet().stream().flatMap(set -> set.stream()),
					topdown.keySet().stream().flatMap(set -> set.stream())).collect(Collectors.toSet()));
			
			// If there is any empty value associated with mapped attribute, then exit with empty value
			if(conditionalValues.stream().anyMatch(value -> value.getAttribute().getEmptyValue().equals(value)))
				return this.getNulVal();
			
			AControl<Double> conditionalProbability = conditionalValues.isEmpty() ? 
					this.getIdentityProductVal() : mat.getVal(conditionalValues); 
			if(conditionalProbability.getValue() > 1)
				throw new IllegalArgumentException("Conditional probability is above 1: "+conditionalProbability.getValue());
			
			// Adjust conditional probability
			conditionalProbability.multiply(Stream.concat(bottomup.values().stream(), topdown.values().stream())
					.reduce(this.getIdentityProductVal(), (c1, c2) -> c1.multiply(c2)));

			// COMPUTE BRUT PROBABILITY
			AControl<Double> newProbability = mat.getVal(concernedValues).multiply(1 / conditionalProbability.getValue());

			// Update conditional probability
			conditionalProba.multiply(newProbability);

			// Update visited probability
			Set<APopulationAttribute> updateDimension = concernedValues
					.stream().map(a -> a.getAttribute()).collect(Collectors.toSet()); 
			assignedDimension.addAll(updateDimension);
			remainingDimension.removeAll(updateDimension);
		}
		return conditionalProba;
	}

	// ------------------ Setters ------------------ //

	@Override
	public boolean addValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, AControl<? extends Number> value) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinates.getDimensions())).collect(Collectors.toSet());
		return jds.iterator().next().addValue(coordinates, value);
	}

	@Override
	public final boolean addValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, Double value) {
		return addValue(coordinates, new ControlFrequency(value));
	}


	@Override
	public final boolean addValue(Double value, String... coordinates) {
		return addValue(GosplCoordinate.createCoordinate(this.getDimensions(), coordinates), value);
	}



	@Override
	public boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, AControl<? extends Number> value) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinates.getDimensions())).collect(Collectors.toSet());
		if(jds.size() != 1)
			return false;
		return jds.iterator().next().setValue(coordinates, value);
	}

	@Override
	public final boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinate, Double value) {
		return setValue(coordinate, new ControlFrequency(value));
	}
	
	@Override
	public final boolean setValue(Double value, String... coordinates) {
		return setValue(GosplCoordinate.createCoordinate(this.getDimensions(), coordinates), value);
	}

	
	// ------------------ Side contract ------------------ //  

	@Override
	public AControl<Double> getNulVal() {
		return new ControlFrequency(0d);
	}

	@Override
	public AControl<Double> getIdentityProductVal() {
		return new ControlFrequency(1d);
	}

	// -------------------- Utilities -------------------- //

	@Override
	public boolean isCoordinateCompliant(ACoordinate<APopulationAttribute, APopulationValue> coordinate) {
		return jointDistributionSet.stream().anyMatch(jd -> jd.isCoordinateCompliant(coordinate));
	}

	@Override
	public AControl<Double> parseVal(GSDataParser parser, String val) {
		if(parser.getValueType(val).equals(GSEnumDataType.String) || parser.getValueType(val).equals(GSEnumDataType.Boolean))
			return getNulVal();
		return new ControlFrequency(Double.valueOf(val));
	}

	// -------------------- Inner Utilities -------------------- //

	private Map<Set<APopulationValue>, AControl<Double>> estimateBottomUpReferences(
			AFullNDimensionalMatrix<Double> mat, Collection<APopulationValue> aspects,
			Set<APopulationAttribute> assignedDimension){
		// Setup conditional bottom up values: value for which this matrix has partial 
		// bottom-up information, i.e. one attribute of this matrix has for referent
		// one value attribute for which probability has already be defined 
		Map<APopulationAttribute, APopulationAttribute> refAttributeToBottomup = mat.getDimensions().stream()
				.filter(att -> !att.getReferentAttribute().equals(att) && !att.isRecordAttribute()
						&& assignedDimension.contains(att.getReferentAttribute()))
				.collect(Collectors.toMap(att -> att.getReferentAttribute(), Function.identity()));
		// Transpose top down value set to control proportional referent
		return computeControlReferences(refAttributeToBottomup, aspects, assignedDimension);
	}

	private Map<Set<APopulationValue>, AControl<Double>> estimateTopDownReferences(
			AFullNDimensionalMatrix<Double> mat, Collection<APopulationValue> aspects,
			Set<APopulationAttribute> assignedDimension){
		// Setup conditional top down values: value for which this matrix has partial 
		// top down information, i.e. one attribute of this matrix is the referent of
		// one value attribute for which probability has already be defined
		Map<APopulationAttribute, APopulationAttribute> assignedAttributeToTopdown = assignedDimension.stream()
				.filter(att -> !att.getReferentAttribute().equals(att) && !att.isRecordAttribute()
						&& mat.getDimensions().contains(att))
				.collect(Collectors.toMap(Function.identity(), att -> att.getReferentAttribute()));
		// Transpose bottom up value set to control proportional referent
		return computeControlReferences(assignedAttributeToTopdown, aspects, assignedDimension);
	}
	
	private Map<Set<APopulationValue>, AControl<Double>> computeControlReferences(
			Map<APopulationAttribute, APopulationAttribute> assignedAttToCurrentAtt,
			Collection<APopulationValue> aspects, Set<APopulationAttribute> assignedDimension){
		// Map of referent attribute and their already defined value
		Map<APopulationAttribute, Set<APopulationValue>> CurrentAttToAssignedValues = 
				aspects.stream().filter(as -> assignedDimension.contains(as.getAttribute())
						&& assignedAttToCurrentAtt.keySet().contains(as.getAttribute()))
				.collect(Collectors.groupingBy(as -> assignedAttToCurrentAtt.get(as.getAttribute()), 
						Collectors.mapping(Function.identity(), Collectors.toSet())));
		// Transpose bottom up value set to control proportional referent
		Map<Set<APopulationValue>, AControl<Double>> output = new HashMap<>();
		for(APopulationAttribute bottomupAtt : assignedAttToCurrentAtt.keySet()){
			APopulationAttribute topdownAtt = assignedAttToCurrentAtt.get(bottomupAtt);
			Set<APopulationValue> topdownVals = CurrentAttToAssignedValues.get(topdownAtt)
					.stream().flatMap(val -> topdownAtt.findMappedAttributeValues(val).stream())
					.collect(Collectors.toSet());
			Set<APopulationValue> bottomupVals = topdownVals.stream().flatMap(val -> 
					topdownAtt.findMappedAttributeValues(val).stream()).collect(Collectors.toSet());
			AFullNDimensionalMatrix<Double> matrix = this.jointDistributionSet.stream().filter(m -> 
					m.getDimensions().contains(bottomupAtt)).findAny().get();
			output.put(topdownVals, matrix.getVal(CurrentAttToAssignedValues.get(topdownAtt))
					.multiply(1d / matrix.getVal(bottomupVals).getValue()));
		}
		return output;
	}


	@Override
	public void normalize() throws IllegalArgumentException {

		throw new IllegalArgumentException("should not normalize a "+getMetaDataType());		
		
	}


	@Override
	public boolean checkAllCoordinatesHaveValues() {
		return false;
	}


	@Override
	public boolean checkGlobalSum() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
