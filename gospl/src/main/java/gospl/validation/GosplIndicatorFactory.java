package gospl.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import core.metamodel.IPopulation;
import core.metamodel.IQueryablePopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

/**
 * Provides some methods to evaluate the distance between input data and generated population.
 * <p>
 * Input data format as {@link INDimensionalMatrix} guarantees no zero cell value
 * 
 * @author kevinchapuis
 *
 */
public class GosplIndicatorFactory {

	private static Double EPSILON = Math.pow(10, -6);
	private static GosplIndicatorFactory gif = new GosplIndicatorFactory();
	private double criticalPValue = 0.05;

	private GSPerformanceUtil gspu;
	
	private GosplIndicatorFactory(){}

	public static GosplIndicatorFactory getFactory() {
		gif.gspu = new GSPerformanceUtil("GSUtil print for indicator factory", LogManager.getLogger(gif));
		return gif;
	}

	public void setChiSquareCritivalPValue(double criticalPValue){
		this.criticalPValue  = criticalPValue;
	}
	
	// ----------------------------- Matrix error ---------------------------- //
	
	/**
	 * Return the absolute error on each control marginals for the given population
	 * 
	 * @param layer
	 * @param errorMatrix
	 * @param objectives
	 * @return
	 */
	public INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> getAbsoluteErrors(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> pop,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> errorMatrix,
			Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {

		
		Set<Attribute<? extends IValue>> marginals = objectives.stream()
				.flatMap(mat -> mat.getDimensions().stream()).collect(Collectors.toSet());
		
		if (errorMatrix.getDimensions().stream().allMatch(att -> marginals.stream().anyMatch(m -> m.isLinked(att)))) {
			throw new IllegalArgumentException("Some attribute of the errorMatrix does not match with any marginals:\n"
					+ errorMatrix.getDimensions().stream().filter(att -> marginals.stream().noneMatch(m -> m.isLinked(att)))
						.map(Attribute::getAttributeName).collect(Collectors.joining("; ")));
		}
		
		Map<Attribute<? extends IValue>, Attribute<? extends IValue>> margeToAtt = marginals.stream()
				.filter(att -> pop.getPopulationAttributes().stream()
						.anyMatch(popAtt -> att.isLinked(popAtt)))
				.collect(Collectors.toMap(
						Function.identity(),
						marginal -> pop.getPopulationAttributes().stream()
							.filter(popAtt -> marginal.isLinked(popAtt))
							.findFirst().get()
						));
		
		AFullNDimensionalMatrix<Integer> popMat = GosplNDimensionalMatrixFactory.getFactory()
				.createContingency(new HashSet<>(margeToAtt.keySet()), pop);
		
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : errorMatrix.getMatrix().keySet()) {
			int input = objectives.stream()
					.filter(mat -> mat.getDimensions().containsAll(coord.getDimensions()))
					.findFirst().get().getVal(coord.values())
					.getValue();
			
			Collection<IValue> popCoord = popMat.getDimensions().stream()
					.flatMap(att -> margeToAtt.get(att).findMappedAttributeValues(coord.getMap().get(att)).stream())
					.collect(Collectors.toSet());
			int output = popMat.getVal(popCoord,true).getValue();
			
			errorMatrix.setValue(coord, input-output);
		}
		
		return errorMatrix;
	}
	
	// ---------------------- Total Absolute Cell Error ---------------------- //

	
	/**
	 * Home made indicator that follow down the path of RSSZ* indicator but with
	 * a very simple expression of "cell based error": it count the number of cells
	 * that does not fit input matrix. 
	 * 
	 * The indicator test relative difference for contingency matrix, and absolute difference
	 * for frequency matrix. Cells fit when the difference is less than critical chi value.
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.getDegree())
				.inverseCumulativeProbability(criticalPValue);
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTACE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population), chiFiveCritical);
		case GlobalFrequencyTable:
			return getDoubleTACE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createDistribution(population), chiFiveCritical);
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type : it should have been collapse"
					+ " [see GosplInputDataManager#collapseDataTablesIntoDistribution]");
		}
	}
	
	/**
	 * Same as {@link #getTACE(INDimensionalMatrix, IPopulation)} but with queryable population to fasten computation
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population){
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.getDegree())
				.inverseCumulativeProbability(criticalPValue);
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTACE(inputMatrix, population, chiFiveCritical);
		case GlobalFrequencyTable:
			return getDoubleTACE(inputMatrix, population, chiFiveCritical);
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type : it should have been collapse"
					+ " [see GosplInputDataManager#collapseDataTablesIntoDistribution]");
		}
	}

	/**
	 * Home made indicator that follow down the path of RSSZ* indicator but with
	 * a very simple expression of "cell based error": it count the number of cells
	 * that does not fit input matrix. This one is based on a delta relative difference.
	 * 
	 * The indicator test relative difference for contingency matrix, and absolute difference
	 * for frequency matrix. Cells fit when the difference is less than delta parameter.
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, double delta){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTACE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population), delta);
		case GlobalFrequencyTable:
			return getDoubleTACE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createDistribution(population), delta);
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type : it should have been collapse"
					+ " [see GosplInputDataManager#collapseDataTablesIntoDistribution]");
		}
	}
	
	/**
	 * Total Absolute Cell error with population transposed and input data as contingency tables
	 * 
	 * @see #getDoubleTACE(INDimensionalMatrix, AFullNDimensionalMatrix, double)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @param delta
	 * @return
	 */
	public int getIntegerTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix, double delta){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().intValue()) / 
						e.getValue().getValue().doubleValue() > delta ? 1 : 0)
				.sum();
	}
	
	/**
	 * Total Absolute Cell error with population transposed and input data as frequency tables
	 * 
	 * @see #getDoubleTACE(INDimensionalMatrix, AFullNDimensionalMatrix, double)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @param delta
	 * @return
	 */
	public int getDoubleTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix, double delta){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().doubleValue()) / 
						e.getValue().getValue().doubleValue() > delta ? 1 : 0)
				.sum();
	}
	
	/**
	 * Total Absolute Cell error with a queryable population to fasten computation
	 * 
	 * @param inputMatrix
	 * @param queryablePopulation
	 * @param delta
	 * @return
	 */
	public int getIntegerTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> queryablePopulation, double delta) {
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(queryablePopulation.getCountHavingValues(e.getKey().values().stream()
							.collect(Collectors.groupingBy(v -> inputMatrix.getDimension(v),
									Collectors.toCollection(ArrayList::new)))) - 
						e.getValue().getValue().intValue()) / 
						e.getValue().getValue().doubleValue() > delta ? 1 : 0)
				.sum();
	}
	
	/**
	 * Total Absolute Cell error with a queryable population to fasten computation
	 * 
	 * @param inputMatrix
	 * @param queryablePopulation
	 * @param delta
	 * @return
	 */
	public int getDoubleTACE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> queryablePopulation, double delta){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(queryablePopulation.getCountHavingValues(e.getKey().values().stream()
						.collect(Collectors.groupingBy(v -> inputMatrix.getDimension(v),
								Collectors.toCollection(ArrayList::new)))) / queryablePopulation.size() 
						- e.getValue().getValue().doubleValue()) > delta ? 1 : 0)
				.sum();
	}

	// ---------------------- Total Absolute Error ---------------------- //

	/**
	 * Return total absolute error (TAE) for this {@code population}. The indicator
	 * just compute the number of misclassified individual from the population
	 * compared to record of the {@code inputMatrix}.
	 * <p>
	 * If provided input matrix is a distribution of probability, hence indicator is an estimation
	 * of the number of misclassified individual (sum of frequency difference normalize to population size)
	 * 
	 * @see P. Williamson, M. Birkin, Phil H. Rees, 1998. The estimation of population microdata 
	 * by using data from small area statistics and samples of anonymised records; 
	 * Environment and PLanning A
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getTAE(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTAE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population));
		case GlobalFrequencyTable:
			return Math.round(Math.round(getDoubleTAE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createDistribution(population)) * population.size()));
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * Same as {@link #getTAE(INDimensionalMatrix, IPopulation)} but with queryable population to fasten computation
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getTAE(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTAE(inputMatrix, population);
		case GlobalFrequencyTable:
			return Math.round(Math.round(getDoubleTAE(inputMatrix, population) * population.size()));
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * Total absolute error with population transposed and input data as contingency tables
	 * 
	 * @see {@link #getTAE(INDimensionalMatrix, IPopulation)}
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public int getIntegerTAE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix){
		
		if(gspu.getLogger().getLevel()==Level.TRACE) {
			gspu.sysoStempMessage("Compute TAE for matrix "+inputMatrix.getGenesisAsString()+" and "+populationMatrix.getGenesisAsString());
			for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()) {
				gspu.sysoStempMessage("Matrix coordinate :"+coord.values().stream().map(IValue::getStringValue).collect(Collectors.joining("; ")));
				gspu.sysoStempMessage("\tValue = "+inputMatrix.getVal(coord));
				Collection<IValue> vals = null;
				try {
					vals = populationMatrix.getCoordinates(coord.values().stream().collect(Collectors.toSet())).stream()
							.flatMap(c -> c.values().stream()).collect(Collectors.toSet());
				} catch (NullPointerException e) {
					gspu.sysoStempMessage(populationMatrix.getAspects().stream().map(IValue::getStringValue)
							.collect(Collectors.joining("; ")));
					throw e;
				}
				
				gspu.sysoStempMessage("Population coordinate :"+vals.stream().map(IValue::getStringValue).collect(Collectors.joining("; ")));
				gspu.sysoStempMessage("\tValue = "+populationMatrix.getVal(vals.stream().collect(Collectors.toSet())));
			}
		}
		
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(populationMatrix.getVal(e.getKey().values().stream()
							.filter(v -> populationMatrix.getAspects().contains(v)).collect(Collectors.toSet()), true)
						.getValue() - e.getValue().getValue().intValue()))
				.sum();
	}
	
	/**
	 * Total absolute error with queryable population to fasten process
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public int getIntegerTAE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(population.getCountHavingValues(e.getKey().values().stream()
						.collect(Collectors.groupingBy(v -> inputMatrix.getDimension(v),
								Collectors.toCollection(ArrayList::new)))) 
						- e.getValue().getValue().intValue()))
				.sum();
	}
	
	/**
	 * Total absolute error with population transposed and input data as frequency tables
	 * 
	 * @see {@link #getTAE(INDimensionalMatrix, IPopulation)}
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public double getDoubleTAE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToDouble(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().doubleValue()))
				.sum();
	}
	
	/**
	 * Total absolute error with queryable population to fasten process
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getDoubleTAE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToDouble(e -> Math.abs(population.getCountHavingValues(e.getKey().values().stream()
						.collect(Collectors.groupingBy(v -> inputMatrix.getDimension(v),
								Collectors.toCollection(ArrayList::new)))) / (1d * population.size())
						- e.getValue().getValue().doubleValue()))
				.sum();
	}
	
	
	// ---------------------- Average Absolute Pourcentage Error ---------------------- //

	
	/**
	 * Return the average absolute percentage difference (AAPD) for this {@code population}. This indicator
	 * aggregates relative difference between known multi-way marginal total from input data and those of
	 * the generated synthetic population. 
	 * <p>
	 * TODO: little background for the method and advise to read output indicator
	 * 
	 * @see J.Y. Guo and C R. Bhat, 2007. Population synthesis for microsimulating travel behavior; 
	 * Transportation Research Record: Journal of the Transportation Research Board
	 * 
	 * @param inputFrequency
	 * @param population
	 * @return
	 */
	public double getAAPD(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		return this.getTAE(inputMatrix, population) / inputMatrix.size();
	}
	
	/**
	 * @see #getAAPD(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getAAPD(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		return this.getTAE(inputMatrix, population) / inputMatrix.size();
	}
	
	/**
	 * @see #getAAPD(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public double getIntegerAAPD(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix) {
		return this.getIntegerTAE(inputMatrix, populationMatrix) / inputMatrix.size();
	}
	
	/**
	 * @see #getAAPD(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public double getDoubleAAPD(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix) {
		return this.getDoubleTAE(inputMatrix, populationMatrix) / inputMatrix.size();
	}

	
	// ---------------------- Standardize Root Mean Square Error ---------------------- //
	
	
	/**
	 * Return the standardized root mean square error (SRMSE also known as NRMSE) for this {@code population}. 
	 * This indicator aggregates error between known control total from input data and those of the generated
	 * synthetic population. The standardization (normalization) criterion is the most important discrepancy
	 * among generated data, meaning: maxarg(generatedMatrix) - minarg(generatedMatrix).
	 * <p>
	 * TODO: little background for the method and advise to read output indicator
	 * 
	 * @see Müller, K., Axhausen, K.W., 2011. Population synthesis for microsimulation: state of the art; 
	 * Transportation Research Board 90th Annual Meeting.Washington, D.C.
	 * @see Sun, L. and Erath A., 2015. A Bayesian network approach for population synthesis; Transportation
	 * Research Part C
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			return getIntegerSRMSE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population));
		case GlobalFrequencyTable:
			return getDoubleSRMSE(inputMatrix, GosplNDimensionalMatrixFactory
				.getFactory().createDistribution(population));
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * 
	 * @see #getSRMSE(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			return getIntegerSRMSE(inputMatrix, population);
		case GlobalFrequencyTable:
			return getDoubleSRMSE(inputMatrix, population);
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * Standardized Root Mean Square Error with population transposed and input data as a contingency table
	 * <p>
	 * @see #getSRMSE(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public double getIntegerSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix){
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, mse = 0d;
		int minVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).min().getAsInt();
		int maxVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).max().getAsInt();
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = populationMatrix.getVal(coord.values(), true).getValue();
			mse += Math.pow(expectedValue - actualValue, 2) / nbCells;
		}
		return Math.sqrt(mse) / (maxVal - minVal);
	}
	
	/**
	 * Uses faster access population interface {@link IQueryablePopulation}
	 * 
	 * @see #getIntegerSRMSE(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getIntegerSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, mse = 0d;
		int minVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).min().getAsInt();
		int maxVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).max().getAsInt();
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = population.getCountHavingCoordinate(coord.getMap());
			mse += Math.pow(expectedValue - actualValue, 2) / nbCells;
		}
		return Math.sqrt(mse) / (maxVal - minVal);
	}

	/**
	 * Standardized Root Mean Square Error that compare a synthetic distribution and
	 * input data as a frequency table matrix
	 * <p>
	 * @see #getSRMSE(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @param popSize
	 * @return
	 */
	public double getDoubleSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix){
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, mse = 0d;
		double minVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).min().getAsInt();
		double maxVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).max().getAsInt();
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = populationMatrix.getVal(coord.values(), true).getValue();
			mse += Math.pow(expectedValue - actualValue, 2) / nbCells;
		}
		return Math.sqrt(mse) / (maxVal - minVal);
	}
	
	/**
	 * Uses faster access population interface {@link IQueryablePopulation}
	 * 
	 * @see #getDoubleSRMSE(INDimensionalMatrix, AFullNDimensionalMatrix, int)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @param popSize
	 * @return
	 */
	public double getDoubleSRMSE(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population){
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, mse = 0d;
		double minVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).min().getAsInt();
		double maxVal = inputMatrix.getMatrix().values().stream()
				.mapToInt(v -> v.getValue().intValue()).max().getAsInt();
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * population.size();
			actualValue = population.getCountHavingCoordinate(coord.getMap());
			mse += Math.pow(expectedValue - actualValue, 2) / nbCells;
		}
		return Math.sqrt(mse) / (maxVal - minVal);
	}
	
	// ---------------------- Relative Sum of Square Modified Z-Score ---------------------- //
	
	
	/**
	 * RSSZ is an overall estimation of goodness of fit based on several indicator.
	 * It is first based on Z-score that focus on cell based indicator of error. SSZ is
	 * the sum of square Z-score and RSSZ is a proposed relative indicator, that is the SSZ
	 * divided by the chi square 5% critical value.
	 * <p>
	 * FIXME: do not use because of inconsistent result
	 * 
	 * @see Williamson, Pau, 2012. “An Evaluation of Two Synthetic Small-Area Microdata Simulation 
	 * Methodologies: Synthetic Reconstruction and Combinatorial Optimisation.” 
	 * In Spatial Microsimulation: A Reference Guide for Users
	 * @see Huand, Z., Williamson, P., 2001. "A Comparison of Synthetic Reconstruction and Combinatorial
	 * Optimisation Approaches to the Creation of Small-area Microdata" Working paper online
	 * @return RSSZstaar indicator as a double
	 */
	public double getRSSZstar(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			return this.getIntegerRSSZstar(inputMatrix, GosplNDimensionalMatrixFactory.getFactory().createContingency(population));
		case GlobalFrequencyTable:
			return this.getDoubleRSSZstar(inputMatrix, GosplNDimensionalMatrixFactory.getFactory().createDistribution(population));
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * Uses faster access population interface {@link IQueryablePopulation}
	 * 
	 * @see #getRSSZstar(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getRSSZstar(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			IQueryablePopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		double expectedValue = 0d;
		double actualValue = 0d;
		double ssz = 0d;
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.getDegree())
				.inverseCumulativeProbability(criticalPValue);
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
				actualValue = population.getCountHavingCoordinate(coord.getMap());
				ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue / population.size()));
			}
			return ssz / chiFiveCritical;
		case GlobalFrequencyTable:
			for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * population.size();
				actualValue = population.getCountHavingCoordinate(coord.getMap());
				ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue / population.size()));
			}
			return ssz / chiFiveCritical;
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}
	
	/**
	 * 
	 * @param inputMatrix
	 * @param contingencyTable
	 * @return
	 */
	public double getIntegerRSSZstar(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> contingencyTable) {
		double expectedValue = 0d;
		double actualValue = 0d;
		double ssz = 0d;
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.getDegree())
				.inverseCumulativeProbability(criticalPValue);
		int popSize = contingencyTable.getVal().getValue().intValue();
		
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = contingencyTable.getVal(coord.values(), true).getValue();
			ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue / popSize));
		}
		
		return ssz / chiFiveCritical;
		
	}
	
	/**
	 * WARNING: not sure of implementation for distribution comparison, because literature usage have been made
	 * using contingency tables
	 * 
	 * @param inputMatrix
	 * @param distribution
	 * @return
	 */
	public double getDoubleRSSZstar(INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> distribution) {
		double expectedValue = 0d;
		double actualValue = 0d;
		double ssz = 0d;
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.getDegree())
				.inverseCumulativeProbability(criticalPValue);
		
		for(ACoordinate<Attribute<? extends IValue>, IValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = distribution.getVal(coord.values(), true).getValue();
			ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue));
		}
		
		return ssz / chiFiveCritical;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	// ---------------------- MAIN REPORT UTILITY METHODS ---------------------- //
	///////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Give a statistical summary
	 * 
	 * @param file
	 * @param distribution
	 * @param population
	 * @throws IOException 
	 */
	public Map<GosplIndicator, Number> getReport(Collection<GosplIndicator> indicators,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population) throws IOException {		
		return indicators.stream().collect(Collectors.toMap(Function.identity(), 
				indicator -> this.getStats(indicator, distribution, population)));
	}

	/**
	 * Save report to file
	 * 
	 * @param outputFile
	 * @param report
	 * @throws IOException 
	 */
	public void saveReport(File outputFile, Map<GosplIndicator, Number> report,
			String algo, int popSize) 
			throws IOException {
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		String separator = ";";
		BufferedWriter bw;

		bw = Files.newBufferedWriter(outputFile.toPath());
		bw.write("Algo"+separator+"Pop size");
		for(GosplIndicator indicator : report.keySet())
			bw.write(separator+indicator.toString());
		bw.newLine();
		bw.write(algo+separator+popSize);
		for(GosplIndicator indicator : report.keySet())
			bw.write(separator+decimalFormat.format(report.get(indicator).doubleValue()).toString());
		bw.flush();
	}
	
	/**
	 * Get the desired indicator that asses error between input and created distribution
	 * 
	 * @param indicator
	 * @param inputMatrix
	 * @param distribution
	 * @return
	 */
	public double getIndicator(GosplIndicator indicator,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> inputMatrix,
			AFullNDimensionalMatrix<Double> distribution) {
		switch(indicator) {
		case TAE:
			return this.getDoubleTAE(inputMatrix, distribution);
		case TACE:
			return this.getDoubleTACE(inputMatrix, distribution, EPSILON);
		case AAPD:
			return this.getDoubleAAPD(inputMatrix, distribution);
		case SRMSE:
			return this.getDoubleSRMSE(inputMatrix, distribution);
		case RSSZstar:
			return this.getDoubleRSSZstar(inputMatrix, distribution);
		default:
			throw new IllegalArgumentException(indicator+" is an unknown indicator");
		}
	}
	

	// -------------------- Private inner methods -------------------- //
	
	private Number getStats(GosplIndicator indicator,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population){
		switch (indicator) {
		case TAE:
			return this.getTAE(distribution, population);
		case TACE:
			return this.getTACE(distribution, population);
		case AAPD:
			return this.getAAPD(distribution, population);
		case SRMSE:
			return this.getSRMSE(distribution, population);
		case RSSZstar:
			return this.getRSSZstar(distribution, population);
		default:
			return this.getTAE(distribution, population);
		}
	}

}
