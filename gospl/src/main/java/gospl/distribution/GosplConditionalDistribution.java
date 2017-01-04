package gospl.distribution;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

/**
 * A set of joint distributions with links of dependancy between them. 
 * 
 * @author Kevin Chapuis
 */
public class GosplConditionalDistribution extends ASegmentedNDimensionalMatrix<Double> {

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
			
			// Setup conditional values (known probability to compute conditional probability)
			Set<APopulationValue> conditionalValues = aspects.stream()
					.filter(val -> assignedDimension.stream().anyMatch(dim -> dim.getValues().contains(val)))
					.collect(Collectors.toSet());
			
			// Deal with referent attribute to also make use of know relationship between attributes
			Map<Set<APopulationValue>, Set<APopulationValue>> referentAspects = new HashMap<>(); 
					
			// Bottom-up references
			referentAspects.putAll(aspects.stream()
					.filter(as -> !as.getAttribute().getReferentAttribute().equals(as.getAttribute()) 
						&& !as.getAttribute().isRecordAttribute() 
						&& mat.getDimensions().contains(as.getAttribute().getReferentAttribute()))
					.collect(Collectors.groupingBy(as -> as.getAttribute().getReferentAttribute().findMappedAttributeValues(as),
								Collectors.mapping(Function.identity(), Collectors.toSet()))));
			
			// Top-down references
			referentAspects.putAll(mat.getDimensions().stream().filter(att -> aspects.stream()
					.anyMatch(as -> as.getAttribute().equals(att.getReferentAttribute())))
					.map(att -> att.getValues().stream()
							.filter(as -> att.findMappedAttributeValues(as).stream().anyMatch(mapAs -> aspects.contains(mapAs)))
							.collect(Collectors.toSet()))
					.collect(Collectors.toMap(Function.identity(), set -> set.stream().flatMap(as -> 
							as.getAttribute().findMappedAttributeValues(as).stream()).collect(Collectors.toSet()))));
			
			// Partial mapping record to referent attribute's values
			AControl<Double> conditionalReferent = this.getIdentityProductVal();
			for(Entry<Set<APopulationValue>, Set<APopulationValue>> entry : referentAspects.entrySet()){
				long theoreticalMap = entry.getValue().stream().findFirst().get().getAttribute()
						.getValues().stream().filter(as -> as.getAttribute().getReferentAttribute().findMappedAttributeValues(as)
								.equals(entry.getKey())).count();
				// WARNING: this is the independent hypothesis, another way to do can be to randomly pick
				// referent attribute value = i.e. randomly select 'x' value in entry.getKey()
				conditionalReferent.multiply(entry.getValue().size() * 1d / theoreticalMap);
			}
			
			// Add all referent values to conditional values
			conditionalValues.addAll(referentAspects.keySet().stream().flatMap(Set::stream).collect(Collectors.toSet()));
			
			// TODO: verify the process
			// Compute brut probability
			AControl<Double> brutUpdate = mat.getVal(concernedValues);
			AControl<Double> conditionalUpdate = mat.getVal(conditionalValues).multiply(conditionalReferent);
			// Update conditional probability
			conditionalProba.multiply(brutUpdate.multiply(conditionalUpdate.getValue()));
			
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
	public boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, AControl<? extends Number> value) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinates.getDimensions())).collect(Collectors.toSet());
		if(jds.size() != 1)
			return false;
		return jds.iterator().next().setValue(coordinates, value);
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


}
