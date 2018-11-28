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
import core.util.GSPerformanceUtil;
import core.util.GSUtilAttribute;
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
	
	/**
	 * Transpose an unknown matrix into a full matrix. If matrix passed in argument is a segmented matrix
	 * then, the algorithm will end up making unknown relationship between attribute independent
	 * 
	 * @param unknownDistribution
	 * @param gspu: in order to track the process from the outside
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> createDistribution(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> unknownDistribution,
			GSPerformanceUtil gspu){
		
		if(!unknownDistribution.isSegmented())
			return this.createDistribution(unknownDistribution.getMatrix());
		
		// Reject attribute with referent, to only account for referent attribute
		Set<Attribute<? extends IValue>> targetedDimensions = unknownDistribution.getDimensions()
				.stream().filter(att -> att.getReferentAttribute().equals(att))
				.collect(Collectors.toSet());

		// Setup the matrix to estimate 
		AFullNDimensionalMatrix<Double> freqMatrix = new GosplNDimensionalMatrixFactory()
				.createEmptyDistribution(targetedDimensions);

		gspu.sysoStempMessage("Creation of matrix with attributes: "+Arrays.toString(targetedDimensions.toArray()));

		// Extrapolate the whole set of coordinates
		Collection<Map<Attribute<? extends IValue>, IValue>> coordinates = GSUtilAttribute.getValuesCombination(targetedDimensions);

		gspu.sysoStempPerformance(1, this);
		gspu.sysoStempMessage("Start writting down collpased distribution of size "+coordinates.size());

		for(Map<Attribute<? extends IValue>, IValue> coordinate : coordinates){
			AControl<Double> nulVal = freqMatrix.getNulVal();
			ACoordinate<Attribute<? extends IValue>, IValue> coord = new GosplCoordinate(coordinate);
			AControl<Double> freq = unknownDistribution.getVal(coord);
			if(!nulVal.getValue().equals(freq.getValue()))
				freqMatrix.addValue(coord, freq);
			else {
				// HINT: MUST INTEGRATE COORDINATE WITH EMPTY VALUE, e.g. age under 5 & empty occupation
				gspu.sysoStempMessage("Goes into a referent empty correlate: "
						+Arrays.toString(coordinate.values().toArray()));
				ACoordinate<Attribute<? extends IValue>, IValue	> newCoord = new GosplCoordinate(
						coord.getDimensions().stream().collect(Collectors.toMap(Function.identity(), 
						att -> unknownDistribution.getEmptyReferentCorrelate(coord).stream()
									.anyMatch(val -> val.getValueSpace().getAttribute().equals(att)) ?
								att.getValueSpace().getEmptyValue() : coord.getMap().get(att))));
				if(newCoord.equals(coord))
					freqMatrix.addValue(coord, freq);
				else
					freqMatrix.addValue(newCoord, unknownDistribution.getVal(newCoord.values()
							.stream().filter(value -> !unknownDistribution.getDimension(value).getEmptyValue().equals(value))
							.collect(Collectors.toSet())));
			}
		}
		
		gspu.sysoStempMessage("Distribution has been created succefuly");
		
		return freqMatrix;
	}
	
	/**
	 * Clone the distribution so the value in it are not linked to one another (like it is the case in
	 * createDistribution method)
	 * 
	 * @param distribution
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> cloneDistribution(
			AFullNDimensionalMatrix<Double> distribution){
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(distribution.getDimensions(), 
				GSSurveyType.GlobalFrequencyTable);
		
		distribution.getMatrix().keySet().forEach(coordinate -> 
				matrix.setValue(coordinate, new ControlFrequency(distribution.getVal(coordinate).getValue())
						));
		
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
	 * Create a contingency matrix from entities of a population, but taking into account only the
	 * set of attributes given in parameter
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
	 * Create a full contingency table from an unknown type contingency matrix
	 *  
	 * @param unknownMatrix
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> createContingency(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> unknownMatrix){
		AFullNDimensionalMatrix<Integer> matrix = new GosplContingencyTable(unknownMatrix.getDimensions());
		unknownMatrix.getMatrix().keySet().forEach(coordinate -> 
				matrix.addValue(coordinate, 
						new ControlContingency(unknownMatrix.getVal(coordinate).getValue())
						));
		return matrix;
	}
	
	/**
	 * Clone a matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> cloneContingency(AFullNDimensionalMatrix<Integer> matrix){
		Map<ACoordinate<Attribute<? extends IValue>, IValue>, AControl<Integer>> m = matrix.getMatrix();
		return new GosplContingencyTable(m.keySet().stream().collect(
				Collectors.toMap(
						Function.identity(),
						coord -> new ControlContingency(m.get(coord).getValue())
				)));
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
