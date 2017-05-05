package gospl.distribution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
		
		// Test whether requested aspects are part of mapped attribute
		// If there exists a referent attribute & referents is complete, then replace
		// mapped attribute aspects by referent ones
		Map<APopulationAttribute, Set<APopulationValue>> mapAttToValues = aspects.stream()
				.filter(a -> !a.getAttribute().getReferentAttribute().equals(a.getAttribute())
						&& jointDistributionSet.stream().anyMatch(jd -> 
						jd.getDimensions().contains(a.getAttribute().getReferentAttribute())))
		.collect(Collectors.groupingBy(a -> a.getAttribute(), Collectors
				.mapping(Function.identity(), Collectors.toSet())));
				
		for(APopulationAttribute mAtt : mapAttToValues.keySet()){
			Set<APopulationValue> rValues = mapAttToValues.get(mAtt).stream()
					.flatMap(a -> mAtt.findMappedAttributeValues(a).stream()).collect(Collectors.toSet());
			Set<APopulationValue> rToMValues = rValues.stream()
					.flatMap(a -> mAtt.findMappedAttributeValues(a).stream())
					.collect(Collectors.toSet());
			if(mapAttToValues.get(mAtt).equals(rToMValues)){
				aspects.remove(mapAttToValues.get(mAtt));
				aspects.addAll(rValues);
			} else 
				throw new IllegalArgumentException("Elicit a value for which this n dimensional matrix "
						+ "("+this.getLabel()+") has divergent information about");
		}
		
		// Setup a record of visited dimension to avoid duplicated probabilities
		Set<APopulationAttribute> remainingDimension = aspects.stream()
				.map(aspect -> aspect.getAttribute()).collect(Collectors.toSet());

		// Select matrices that contains at least one concerned dimension and ordered them
		// in decreasing order of the number of matches
		List<AFullNDimensionalMatrix<Double>> concernedMatrices = jointDistributionSet.stream()
				.filter(matrix -> matrix.getDimensions().stream().anyMatch(dimension -> remainingDimension.contains(dimension)))
				.sorted((m1, m2) -> {
					int diffRef = (int) (m1.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count() -
							m2.getDimensions().stream().filter(dim -> remainingDimension.contains(dim)).count());
					if(diffRef > 0)
						return -1;
					if(diffRef == 0 && m1.getDimensions().stream().filter(dim -> remainingDimension.contains(dim.getReferentAttribute()) && 
								!dim.getReferentAttribute().equals(dim)).count() <=
							m2.getDimensions().stream().filter(dim -> remainingDimension.contains(dim.getReferentAttribute()) && 
									!dim.getReferentAttribute().equals(dim)).count())
						return -1;
					return 1;
				}).collect(Collectors.toList());

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

			// WARNING: some error test to erase when finished
			if(Stream.concat(bottomup.values().stream(), topdown.values().stream()).anyMatch(control -> control.getValue() > 1d))
				throw new RuntimeException("Some conditional probabilities exceed 1:"
						+ "\nBottomup: "+bottomup.entrySet().stream().map(e -> e.toString())
						.collect(Collectors.joining("\n"))
						+ "\nTopdown: "+topdown.entrySet().stream().map(e -> e.toString())
						.collect(Collectors.joining("\n")));

			// Add new concerned bottomup/topdown values
			concernedValues.addAll(Stream.concat(bottomup.keySet().stream().flatMap(set -> set.stream()),
					topdown.keySet().stream().flatMap(set -> set.stream())).collect(Collectors.toSet()));
			AControl<Double> newProbability = mat.getVal(concernedValues);

			// If there is any empty value associated with mapped attribute, then exit with empty value
			// It means attributes has divergent encoding values
			if(Stream.concat(bottomup.keySet().stream().flatMap(set -> set.stream()),
					topdown.keySet().stream().flatMap(set -> set.stream()))
					.anyMatch(value -> value.getAttribute().getEmptyValue().equals(value)))
				return this.getNulVal();

			AControl<Double> conditionalProbability = conditionalValues.isEmpty() ? 
					this.getIdentityProductVal() : mat.getVal(conditionalValues); 

			// Adjust conditional probability
			conditionalProbability.multiply(Stream.concat(bottomup.values().stream(), topdown.values().stream())
							.reduce(this.getIdentityProductVal(), (c1, c2) -> c1.multiply(c2)));
			
			// Useless ? only zero possible conditional probability is because mapped attribute
			// have divergent encoded values
			if(conditionalProbability.getValue().equals(this.getNulVal().getValue()))
				return this.getNulVal();
			
			// Adjust probability space definition: e.g. age from 15 to more than 65 & age from 0 to more than 100
			// WARNING: impossible in probability theory / make assumption on the transition (uniformity)ccv
			AControl<Double> adjustSpaceDefinition = this.getIdentityProductVal()
					.multiply(Stream.concat(bottomup.entrySet().stream(), topdown.entrySet().stream())
					.map(e -> e.getValue().multiply(1/mat.getVal(e.getKey()).getValue()))
							.reduce(this.getIdentityProductVal(), (c1, c2) -> c1.multiply(c2)));

			// COMPUTE BRUT PROBABILITY
			newProbability.multiply(adjustSpaceDefinition.getValue() / conditionalProbability.getValue());

			// Update conditional probability
			conditionalProba.multiply(newProbability);

			// Update visited dimension
			Set<APopulationAttribute> updateDimension = concernedValues
					.stream().filter(val -> aspects.contains(val))
							.map(a -> a.getAttribute()).collect(Collectors.toSet());
			assignedDimension.addAll(updateDimension);
			remainingDimension.removeAll(updateDimension);
		}
		return conditionalProba;
	}


	@Override
	public AControl<Double> getVal(Collection<APopulationValue> aspects, boolean defaultToNul) {
		return getVal(aspects);
	}


	@Override
	public AControl<Double> getVal(String... coordinates) {

		return getVal(this.getValues(coordinates));

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

		return this.addValue(this.getCoordinate(coordinates), value);
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
	public Set<APopulationValue> getEmptyReferentCorrelate(
			ACoordinate<APopulationAttribute, APopulationValue> coordinate){
		Set<APopulationValue> knownRef = new HashSet<>();
		for(AFullNDimensionalMatrix<Double> mat : jointDistributionSet)
			if(mat.getEmptyReferentCorrelate(coordinate).isEmpty())
				knownRef.addAll(coordinate.values().stream().filter(val -> 
					mat.getDimensions().stream().anyMatch(dim -> 
					val.getAttribute().getReferentAttribute().equals(dim)))
						.collect(Collectors.toSet()));
		Set<APopulationValue> emptyRef = jointDistributionSet.stream().flatMap(jd -> 
			jd.getEmptyReferentCorrelate(coordinate).stream()).collect(Collectors.toSet());
		emptyRef.removeAll(knownRef.stream().map(val -> 
			val.getAttribute().getReferentAttribute().getEmptyValue()).collect(Collectors.toSet()));
		return emptyRef;
	}
	
	@Override
	public AControl<Double> parseVal(GSDataParser parser, String val) {
		if(parser.getValueType(val).equals(GSEnumDataType.String) || parser.getValueType(val).equals(GSEnumDataType.Boolean))
			return getNulVal();
		return new ControlFrequency(Double.valueOf(val));
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
		return jointDistributionSet.stream().allMatch(jd -> jd.checkGlobalSum());
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
		if(refAttributeToBottomup.isEmpty())
			return Collections.emptyMap();
		// Transpose top down value set to control proportional referent
		Map<Set<APopulationValue>, AControl<Double>> res = computeControlReferences(refAttributeToBottomup, 
				aspects, assignedDimension);
		// Transpose back from top down to bottom up for conditional value to fit **mat** dimensions
		Set<APopulationAttribute> noneAllignedAttribute = res.keySet().stream().flatMap(set -> set.stream())
				.filter(a -> !mat.getDimensions().contains(a.getAttribute()))
				.map(a -> a.getAttribute()).collect(Collectors.toSet());
		if(noneAllignedAttribute.stream().anyMatch(att -> mat.getDimensions()
				.stream().noneMatch(matAtt -> matAtt.getReferentAttribute().equals(att))))
			throw new RuntimeException("Estimated bottom up reference targeted dimension out of the concerned matrix:"
					+ "\nConcerned matrix dimensions = "+Arrays.toString(mat.getDimensions().toArray())
					+ "\nTargeted dimensions = "+res.keySet().stream().flatMap(set -> set.stream().map(val -> val.getAttribute()))
					.collect(Collectors.toList()));
		return res;
	}

	private Map<Set<APopulationValue>, AControl<Double>> estimateTopDownReferences(
			AFullNDimensionalMatrix<Double> mat, Collection<APopulationValue> aspects,
			Set<APopulationAttribute> assignedDimension){
		// Setup conditional top down values: value for which this matrix has partial 
		// top down information, i.e. one attribute of this matrix is the referent of
		// one value attribute for which probability has already be defined
		Map<APopulationAttribute, APopulationAttribute> assignedAttributeToTopdown = assignedDimension.stream()
				.filter(att -> !att.getReferentAttribute().equals(att) && !att.isRecordAttribute()
						&& mat.getDimensions().contains(att.getReferentAttribute()))
				.collect(Collectors.toMap(Function.identity(), att -> att.getReferentAttribute()));
		if(assignedAttributeToTopdown.isEmpty())
			return Collections.emptyMap();
		// Transpose bottom up value set to control proportional referent
		Map<Set<APopulationValue>, AControl<Double>> res = computeControlReferences(assignedAttributeToTopdown, 
				aspects, assignedDimension);
		if(res.keySet().stream().flatMap(set -> set.stream()).anyMatch(a -> !mat.getDimensions().contains(a.getAttribute())))
			throw new RuntimeException("Estimated bottom up reference targeted dimension out of the concerned matrix:"
					+ "\nConcerned matrix dimensions = "+Arrays.toString(mat.getDimensions().toArray())
					+ "\nTargeted dimensions = "+res.keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList()));
		return res;
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
		for(APopulationAttribute assignedAtt : assignedAttToCurrentAtt.keySet()){
			// Distinguish bottomup & topdown attribute from assigned & current attribute
			APopulationAttribute bottomupAtt = assignedAtt.getReferentAttribute()
					.equals(assignedAttToCurrentAtt.get(assignedAtt)) ? assignedAtt : assignedAttToCurrentAtt.get(assignedAtt);
			// Map bottomup & topdown attribute to according current & assigned values
			Set<APopulationValue> currentMappedVals = CurrentAttToAssignedValues.get(assignedAttToCurrentAtt.get(assignedAtt))
					.stream().flatMap(val -> bottomupAtt.findMappedAttributeValues(val).stream())
					.collect(Collectors.toSet());
			Set<APopulationValue> assignedMappedVals = currentMappedVals.stream().flatMap(val -> 
			bottomupAtt.findMappedAttributeValues(val).stream())
					.collect(Collectors.toSet());
			// Find the proper matrix to compute conditional probability
			AFullNDimensionalMatrix<Double> matrix = this.jointDistributionSet.stream().filter(m -> 
			m.getDimensions().contains(assignedAtt)).findAny().get();
			// Use two information: the probability to have assigned values, knowing that current mapped values probability 
			// in this particular matrix (that is we need to know how current mapped values mapped to assigned values)
			Set<APopulationValue> assignedValues = CurrentAttToAssignedValues.get(assignedAttToCurrentAtt.get(assignedAtt));
			
			/* HINT: some syso to debug 
			System.out.println("Assigned: "+Arrays.toString(assignedValues.toArray()));
			System.out.println("Current: "+Arrays.toString(currentMappedVals.toArray()));
			System.out.println("Assigned mapped: "+Arrays.toString(assignedMappedVals.toArray()));
			 */
			
			if(assignedMappedVals.stream().anyMatch(val -> val.getAttribute().getEmptyValue().equals(val)))
				output.put(currentMappedVals, matrix.getNulVal());
			else
				output.put(currentMappedVals, matrix.getVal(assignedValues)
						.multiply(matrix.getVal(assignedValues)
						.multiply(1d / matrix.getVal(assignedMappedVals).getValue())));
			// Pretty slow to multiply twice
		}
		return output;
	}

}
