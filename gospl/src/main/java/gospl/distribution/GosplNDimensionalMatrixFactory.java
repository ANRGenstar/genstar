package gospl.distribution;

import java.util.Arrays;
import java.util.Collection;
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
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.ControlContingency;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * Factory to build various type of {@link INDimensionalMatrix} from many sources:
 * <p>
 * You can create distribution (or contingency table), empty or not, from a {@link IPopulation}
 * or a set of population or even just with a map of coordinate / control
 * <p>  
 * 
 * @author kevinchapuis
 * @author samuel Thiriot
 *
 */
public class GosplNDimensionalMatrixFactory {

	public static final GosplNDimensionalMatrixFactory getFactory() {
		return new GosplNDimensionalMatrixFactory();
	}
	
	//////////////////////////////////////////////
	//				EMPTY MATRIX				//
	//////////////////////////////////////////////
	
	/**
	 * Create an empty distribution
	 * 
	 * @param dimensions
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createEmptyDistribution(
			Set<APopulationAttribute> dimensions, GSSurveyType type){
		AFullNDimensionalMatrix<Double> matrix =  new GosplJointDistribution(dimensions.stream().collect(Collectors.toMap(dim -> dim, dim -> dim.getValues())), 
				type);
		matrix.addGenesis("created from scratch GosplNDimensionalMatrixFactory@createEmptyDistribution");
		return matrix;
	}
	
	public AFullNDimensionalMatrix<Double> createEmptyDistribution(
			Set<APopulationAttribute> dimensions){
		return createEmptyDistribution(dimensions, GSSurveyType.GlobalFrequencyTable);
	}

	public AFullNDimensionalMatrix<Double> createEmptyDistribution(
			APopulationAttribute ... dimensions){
		return createEmptyDistribution(new HashSet(Arrays.asList(dimensions)), GSSurveyType.GlobalFrequencyTable);
	}
	/**
	 * Create an empty segmented distribution
	 * 
	 * @param segmentedDimensions
	 * @return
	 * @throws IllegalDistributionCreation 
	 */
	public ASegmentedNDimensionalMatrix<Double> createEmptyDistribution(
			Collection<Set<APopulationAttribute>> segmentedDimensions) throws IllegalDistributionCreation{
		return new GosplConditionalDistribution(segmentedDimensions.stream()
				.map(dimSet -> this.createEmptyDistribution(dimSet)).collect(Collectors.toSet()));
	}
	
	//////////////////////////////////////////////////
	//				DISTRIBUTION MATRIX				//
	//////////////////////////////////////////////////
	
	/**
	 * Create a distribution from a map: key are mapped to matrix coordinate
	 * and value to matrix control value
	 * 
	 * @param sampleDistribution
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(Set<APopulationAttribute> dimensions,
			Map<Set<APopulationValue>, Double> sampleDistribution){
		if(sampleDistribution.isEmpty())
			throw new IllegalArgumentException("Sample distribution cannot be empty");
		AFullNDimensionalMatrix<Double> distribution = this.createEmptyDistribution(dimensions);
		sampleDistribution.entrySet().stream().forEach(entry -> distribution.addValue(
				new GosplCoordinate(entry.getKey()), new ControlFrequency(entry.getValue())));
		return distribution;
	}
	
	
	/**
	 * Changes a contingency table to a global frequency table
	 * @param contigency
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(
			AFullNDimensionalMatrix<Integer> contigency){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				contigency.getDimensionsAsAttributesAndValues(), 
				GSSurveyType.GlobalFrequencyTable
				); 
		matrix.addGenesis("created from distribution GosplNDimensionalMatrixFactory@createDistribution");

		int total = contigency.getVal().getValue();
		
		// Normalize increments to global frequency
		contigency.getMatrix().keySet().stream().forEach(coord -> matrix.setValue(
											coord, 
											new ControlFrequency(contigency.getVal(coord).getValue().doubleValue()/total)
											));
		
		return matrix;
	}
	
	/**
	 * Create a frequency matrix from entities' population characteristics.  
	 * 
	 * @param population
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				population.getPopulationAttributes().stream().collect(Collectors.toMap(att -> att, att -> att.getValues())), 
				GSSurveyType.GlobalFrequencyTable);
		matrix.addGenesis("created from population GosplNDimensionalMatrixFactory@createDistribution");

		double unitFreq = 1d/population.size();
		
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(APopulationEntity entity : population){
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					new HashSet<>(entity.getValues()));
			if(!matrix.addValue(entityCoord, new ControlFrequency(unitFreq)))
				matrix.getVal(entityCoord).add(unitFreq);
		}
		
		return matrix;
	}
	
	/**
	 * Create a frequency matrix from entities' population subset of characteristics
	 * given as a parameter.  
	 * 
	 * @param attributesToMeasure
	 * @param population
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(
			Set<APopulationAttribute> attributesToMeasure,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				attributesToMeasure.stream().collect(Collectors.toMap(att -> att, att -> att.getValues())),
				GSSurveyType.GlobalFrequencyTable);
		matrix.addGenesis("created from population GosplNDimensionalMatrixFactory@createDistribution");

		double unitFreq = 1d/population.size();
		
		// iterate the whole population
		for (APopulationEntity entity : population) {
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					entity.getValues().stream().filter(pv -> attributesToMeasure.contains(pv.getAttribute())).collect(Collectors.toSet())
					);
			if(!matrix.addValue(entityCoord, new ControlFrequency(unitFreq)))
				matrix.getVal(entityCoord).add(unitFreq);
		}

		return matrix;
	}
	
	//////////////////////////////////////////////////
	//				SEGMENTED MATRIX				//
	//////////////////////////////////////////////////
	
	/**
	 * Create a segmented matrix from multiple population, each beeing a full dimensional matrix
	 * 
	 * @param populations
	 * @return
	 * @throws IllegalDistributionCreation 
	 */
	public ASegmentedNDimensionalMatrix<Double> createDistributionFromPopulations(
			Set<IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>> populations) 
					throws IllegalDistributionCreation {
		return new GosplConditionalDistribution(populations
				.stream().map(pop -> this.createDistribution(pop))
				.collect(Collectors.toSet()));
	}
	
	/**
	 * Create a segmented matrix from multiple full matrix
	 * 
	 * @param innerDistributions
	 * @return
	 * @throws IllegalDistributionCreation
	 */
	public ASegmentedNDimensionalMatrix<Double> createDistributionFromDistributions(
			Set<AFullNDimensionalMatrix<Double>> innerDistributions) throws IllegalDistributionCreation{
		return new GosplConditionalDistribution(innerDistributions);
	}
	
	/**
	 * Create a segmented matrix from multiple full matrix
	 * 
	 * @param innerDistributions
	 * @return
	 * @throws IllegalDistributionCreation
	 */
	@SuppressWarnings("unchecked")
	public ASegmentedNDimensionalMatrix<Double> createDistributionFromDistributions(
			AFullNDimensionalMatrix<Double>... innerDistributions) throws IllegalDistributionCreation{
		return createDistributionFromDistributions(new HashSet<>(Arrays.asList(innerDistributions)));
	}
	
	//////////////////////////////////////////////////
	//				CONTINGENCY MATRIX				//
	//////////////////////////////////////////////////

	/**
	 * Create a contingency matrix from entities' population characteristics
	 * 
	 * @param seed
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createContingency(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(population.getPopulationAttributes().stream()
				.collect(Collectors.toMap(att -> att, att -> att.getValues())));
		matrix.addGenesis("created from a population GosplNDimensionalMatrixFactory@createContigency");

		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(APopulationEntity entity : population){
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					new HashSet<>(entity.getValues()));
			if(!matrix.addValue(entityCoord, new ControlContingency(1)))
				matrix.getVal(entityCoord).add(1);
		}
		
		return matrix;
	}
	
	/**
	 * 
	 * 
	 * @param attributesToMeasure
	 * @param population
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createContigency(
			Set<APopulationAttribute> attributesToMeasure,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(
				attributesToMeasure.stream().collect(Collectors.toMap(att -> att, att -> att.getValues())));
		
		matrix.addGenesis("created from a population GosplNDimensionalMatrixFactory@createContigency");

		// iterate the whole population
		for (APopulationEntity entity : population) {
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					entity.getValues().stream().filter(pv -> attributesToMeasure.contains(pv.getAttribute())).collect(Collectors.toSet())
					);
			if(!matrix.addValue(entityCoord, new ControlContingency(1)))
				matrix.getVal(entityCoord).add(1);
		}

		return matrix;
	}
	
}
