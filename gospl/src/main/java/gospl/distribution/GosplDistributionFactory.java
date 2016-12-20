package gospl.distribution;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlContingency;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

public class GosplDistributionFactory {

	
	/**
	 * Create an empty distribution
	 * 
	 * @param dimensions
	 * @return
	 */
	public static AFullNDimensionalMatrix<Double> createDistribution(
			Set<APopulationAttribute> dimensions){
		return new GosplJointDistribution(dimensions.stream().collect(Collectors.toMap(dim -> dim, dim -> dim.getValues())), 
				GSSurveyType.GlobalFrequencyTable);
	}
	
	/**
	 * Create a distribution from a map: key are mapped to matrix coordinate
	 * and value to matrix control value
	 * <p>
	 * WARNING: make use of parallelism through {@link Stream#parallel()}
	 * 
	 * @param sampleDistribution
	 * @return
	 */
	public static AFullNDimensionalMatrix<Double> createDistribution(Set<APopulationAttribute> dimensions,
			Map<Set<APopulationValue>, Double> sampleDistribution){
		if(sampleDistribution.isEmpty())
			throw new IllegalArgumentException("Sample distribution cannot be empty");
		AFullNDimensionalMatrix<Double> distribution = createDistribution(dimensions);
		sampleDistribution.entrySet().parallelStream().forEach(entry -> distribution.addValue(
				new GosplCoordinate(entry.getKey()), new ControlFrequency(entry.getValue())));
		return distribution;
	}
	
	/**
	 * Create a frequency matrix from entities' population characteristics.  
	 * <p>
	 * WARNING: make use of parallelism through {@link Stream#parallel()}
	 * 
	 * @param population
	 * @return
	 */
	public static AFullNDimensionalMatrix<Double> createDistribution(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				population.getPopulationAttributes().stream().collect(Collectors.toMap(att -> att, att -> att.getValues())), 
				GSSurveyType.GlobalFrequencyTable);
		
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(APopulationEntity entity : population){
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					new HashSet<>(entity.getValues()));
			AControl<Double> unitFreq = new ControlFrequency(1d);
			if(!matrix.addValue(entityCoord, unitFreq))
				matrix.getVal(entityCoord).add(unitFreq);
		}
		
		// Normalize increments to global frequency
		matrix.getMatrix().keySet().parallelStream().forEach(coord -> matrix.getVal(coord).multiply(1d/population.size()));
		
		return matrix;
	}
	
	/**
	 * Changes a contingency table to a global frequency table
	 * @param contigency
	 * @return
	 */
	public static AFullNDimensionalMatrix<Double> createGlobalFrequencyTableFromContingency(
			AFullNDimensionalMatrix<Integer> contigency){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				contigency.getDimensionsAsAttributesAndValues(), 
				GSSurveyType.GlobalFrequencyTable
				);
		
		
		int total = contigency.getVal().getValue();
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		
		// Normalize increments to global frequency
		contigency.getMatrix().keySet().stream().forEach(coord -> matrix.setValue(
											coord, 
											new ControlFrequency(contigency.getVal(coord).getValue().doubleValue()/total)
											));
		
		return matrix;
	}


	/**
	 * Create a contingency matrix from entities' population characteristics
	 * 
	 * @param seed
	 * @return
	 */
	public static AFullNDimensionalMatrix<Integer> createContingency(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(population.getPopulationAttributes().stream()
				.collect(Collectors.toMap(att -> att, att -> att.getValues())));
		
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(APopulationEntity entity : population){
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					new HashSet<>(entity.getValues()));
			AControl<Integer> unitFreq = new ControlContingency(1);
			if(!matrix.addValue(entityCoord, unitFreq))
				matrix.getVal(entityCoord).add(unitFreq);
		}
		
		return matrix;
	}
	
	public static AFullNDimensionalMatrix<Integer> createContigencyFromPopulation(
			Set<APopulationAttribute> attributesToMeasure,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population 
			) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(
				attributesToMeasure.stream().collect(Collectors.toMap(att -> att, att -> att.getValues()))
				);
		
		// iterate the whole population
		for (APopulationEntity entity : population) {
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					entity.getValues().stream().filter(pv -> attributesToMeasure.contains(pv.getAttribute())).collect(Collectors.toSet())
					);
			AControl<Integer> unitFreq = new ControlContingency(1);
			if(!matrix.addValue(entityCoord, unitFreq))
				matrix.getVal(entityCoord).add(unitFreq);
		}

		return matrix;
	}
	
}
