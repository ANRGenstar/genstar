package gospl.distribution;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.ISegmentedNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
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
	
	public static double EPSILON = Math.pow(10, -3);

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
			Set<Attribute<? extends IValue>> dimensions, GSSurveyType type){
		AFullNDimensionalMatrix<Double> matrix =  new GosplJointDistribution(dimensions, type);
		matrix.addGenesis("created from scratch GosplNDimensionalMatrixFactory@createEmptyDistribution");
		return matrix;
	}
	
	public AFullNDimensionalMatrix<Double> createEmptyDistribution(
			Set<Attribute<? extends IValue>> dimensions){
		return createEmptyDistribution(dimensions, GSSurveyType.GlobalFrequencyTable);
	}

	@SuppressWarnings("unchecked")
	public AFullNDimensionalMatrix<Double> createEmptyDistribution(
			Attribute<? extends IValue> ... dimensions){
		return createEmptyDistribution(new HashSet<Attribute<? extends IValue>>(Arrays.asList(dimensions)), GSSurveyType.GlobalFrequencyTable);
	}
	/**
	 * Create an empty segmented distribution
	 * 
	 * @param segmentedDimensions
	 * @return
	 * @throws IllegalDistributionCreation 
	 */
	public ISegmentedNDimensionalMatrix<Double> createEmptyDistribution(
			Collection<Set<Attribute<? extends IValue>>> segmentedDimensions) throws IllegalDistributionCreation{
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
	public AFullNDimensionalMatrix<Double> createDistribution(Set<Attribute<? extends IValue>> dimensions,
			Map<Set<IValue>, Double> sampleDistribution){
		if(sampleDistribution.isEmpty())
			throw new IllegalArgumentException("Sample distribution cannot be empty");
		AFullNDimensionalMatrix<Double> distribution = this.createEmptyDistribution(dimensions);
		sampleDistribution.entrySet().stream().forEach(entry -> distribution.addValue(
				new GosplCoordinate(dimensions.stream().collect(Collectors
						.toMap(Function.identity(), att -> entry.getKey()
								.stream().filter(val -> att.getValueSpace().contains(val)).findFirst().get()))), 
				new ControlFrequency(entry.getValue())));
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
				contigency.getDimensions(), 
				GSSurveyType.GlobalFrequencyTable
				); 
		matrix.addGenesis("created from distribution GosplNDimensionalMatrixFactory@createDistribution");

		int total = Math.round(Math.round(contigency.getVal().getValue().doubleValue()));
		
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
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(population.getPopulationAttributes(), 
				GSSurveyType.GlobalFrequencyTable);
		matrix.addGenesis("created from population GosplNDimensionalMatrixFactory@createDistribution");

		double unitFreq = 1d/population.size();
		
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(ADemoEntity entity : population){
			ACoordinate<Attribute<? extends IValue>, IValue> entityCoord = new GosplCoordinate(entity.getAttributeMap());
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
			Set<Attribute<? extends IValue>> attributesToMeasure,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(attributesToMeasure, GSSurveyType.GlobalFrequencyTable);
		matrix.addGenesis("created from population GosplNDimensionalMatrixFactory@createDistribution");

		double unitFreq = 1d/population.size();
		
		// iterate the whole population
		for (ADemoEntity entity : population) {
			ACoordinate<Attribute<? extends IValue>, IValue> entityCoord = new GosplCoordinate(
					entity.getAttributeMap().entrySet().stream().filter(entry -> attributesToMeasure.contains(entry.getKey()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			if(!matrix.addValue(entityCoord, new ControlFrequency(unitFreq)))
				matrix.getVal(entityCoord).add(unitFreq);
		}

		return matrix;
	}
	
	/**
	 * Create a frequency matrix from inner map collection
	 * <p>
	 * WARNING: there is not any guarantee on inner map collection consistency
	 * 
	 * @param matrix
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(
			Map<ACoordinate<Attribute<? extends IValue>, IValue>, AControl<Double>> matrix){
		return new GosplJointDistribution(matrix);
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
	public ISegmentedNDimensionalMatrix<Double> createDistributionFromPopulations(
			Set<IPopulation<ADemoEntity, Attribute<? extends IValue>>> populations) 
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
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(population.getPopulationAttributes());
		matrix.addGenesis("Created from a population GosplNDimensionalMatrixFactory@createContigency");

		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(ADemoEntity entity : population){
			ACoordinate<Attribute<? extends IValue>, IValue> entityCoord = 
					new GosplCoordinate(entity.getAttributeMap());
			if(!matrix.addValue(entityCoord, new ControlContingency(1)))
				matrix.getVal(entityCoord).add(1);
		}
		return matrix;
	}
	
	/**
	 * TODO: javadoc
	 * 
	 * @param attributesToMeasure
	 * @param population
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createContingency(
			Set<Attribute<? extends IValue>> attributesToMeasure,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(attributesToMeasure);
		
		matrix.addGenesis("created from a population GosplNDimensionalMatrixFactory@createContigency");

		// iterate the whole population
		for (ADemoEntity entity : population) {
			ACoordinate<Attribute<? extends IValue>, IValue> entityCoord = new GosplCoordinate(entity.getAttributeMap());
			if(!matrix.addValue(entityCoord, new ControlContingency(1)))
				matrix.getVal(entityCoord).add(1);
		}

		return matrix;
	}
	
	/**
	 * Clone a matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createContingency(AFullNDimensionalMatrix<Integer> matrix){
		return new GosplContingencyTable(matrix.getMatrix());
	}
	
//////////////////////////////////////////////////
//				    SAMPLE MATRIX				//
//////////////////////////////////////////////////

	/**
	 * 
	 * 
	 * @param attributesToMeasure
	 * @param population
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createSample(
			Set<Attribute<? extends IValue>> attributesToMeasure,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		
		// Init the output matrix
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(attributesToMeasure);
		
		matrix.addGenesis("created from a population GosplNDimensionalMatrixFactory@createContigency");

		// iterate the whole population
		for (ADemoEntity entity : population) {
			ACoordinate<Attribute<? extends IValue>, IValue> entityCoord = new GosplCoordinate(entity.getAttributeMap());
			if(!matrix.addValue(entityCoord, new ControlContingency(1)))
				matrix.getVal(entityCoord).add(1);
		}

		return matrix;
	}
}
