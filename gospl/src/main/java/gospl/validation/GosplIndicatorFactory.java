package gospl.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
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

	private static GosplIndicatorFactory gif = new GosplIndicatorFactory();
	private double criticalPValue = 0.05;

	private GosplIndicatorFactory(){}

	public static GosplIndicatorFactory getFactory() {
		return gif;
	}

	public void setChiSquareCritivalPValue(double criticalPValue){
		this.criticalPValue  = criticalPValue;
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
	public int getTACE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		double chiFiveCritical = new ChiSquaredDistribution(1)
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
					+ "a segmented matrix with multiple matrix meta data type");
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
	public int getTACE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, double delta){
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
					+ "a segmented matrix with multiple matrix meta data type");
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
	public int getIntegerTACE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
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
	public int getDoubleTACE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix, double delta){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().doubleValue()) > delta ? 1 : 0)
				.sum();
	}

	// ---------------------- Total Absolute Error ---------------------- //

	/**
	 * Return total absolute error (TAE) for this {@code population}. The indicator
	 * just compute the number of misclassified individual from the population
	 * compared to record of the {@code inputMatrix}.
	 * <p>
	 * TODO: little background for the method and advise to read output indicator
	 * 
	 * @see P. Williamson, M. Birkin, Phil H. Rees, 1998. The estimation of population microdata 
	 * by using data from small area statistics and samples of anonymised records; 
	 * Environment and PLanning A
	 * 
	 * @param inputMatrix
	 * @param population
	 * @return
	 */
	public double getTAE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable: 
			return getIntegerTAE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population));
		case GlobalFrequencyTable:
			return getDoubleTAE(inputMatrix, GosplNDimensionalMatrixFactory
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
	 * Total absolute error with population transposed and input data as contingency tables
	 * 
	 * @see {@link #getTAE(INDimensionalMatrix, IPopulation)}
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @return
	 */
	public double getIntegerTAE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToInt(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().intValue()))
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
	public double getDoubleTAE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Double> populationMatrix){
		return inputMatrix.getMatrix().entrySet().stream()
				.mapToDouble(e -> Math.abs(populationMatrix.getVal(e.getKey().values(), true)
						.getValue() - e.getValue().getValue().doubleValue()))
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
	public double getAAPD(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		return this.getTAE(inputMatrix, population) / inputMatrix.size();
	}

	
	// ---------------------- Standardize Root Mean Square Error ---------------------- //
	
	
	/**
	 * Return the square root mean square error (SRMSE) for this {@code population}. This indicator
	 * aggregates error between known control total from input data and those of the generated
	 * synthetic population.
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
	public double getSRMSE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			return getIntegerSRMSE(inputMatrix, GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population));
		case GlobalFrequencyTable:
			return getDoubleSRMSE(inputMatrix, GosplNDimensionalMatrixFactory
				.getFactory().createContingency(population), population.size());
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
	public double getIntegerSRMSE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix){
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, rmse = 0d;
		for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
			actualValue = populationMatrix.getVal(coord.values(), true).getValue();
			rmse += Math.pow(expectedValue - actualValue, 2) / nbCells;
		}
		return Math.sqrt(rmse) / inputMatrix.getVal().getValue().intValue();
	}

	/**
	 * Standardized Root Mean Square Error with population transposed as a contingency table and
	 * input data as a frequency table matrix
	 * <p>
	 * @see #getSRMSE(INDimensionalMatrix, IPopulation)
	 * 
	 * @param inputMatrix
	 * @param populationMatrix
	 * @param popSize
	 * @return
	 */
	public double getDoubleSRMSE(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			AFullNDimensionalMatrix<Integer> populationMatrix, int popSize){
		int nbCells = inputMatrix.size();
		double expectedValue, actualValue, s = 0d, rmse = 0d;
		for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
			expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * popSize;
			actualValue = populationMatrix.getVal(coord.values(), true).getValue();
			rmse += Math.pow(expectedValue - actualValue, 2) / nbCells;
			s += Math.pow(actualValue, 2) / nbCells;
		}
		return Math.sqrt(rmse) / s;
	}
	
	// ---------------------- Relative Sum of Square Modified Z-Score ---------------------- //
	
	
	/**
	 * RSSZ is an overall estimation of goodness of fit based on several indicator.
	 * It is first based on Z-score that focus on cell based indicator of error. SSZ is
	 * the sum of square Z-score and RSSZ is a proposed relative indicator, that is the SSZ
	 * divided by the chi square 5% critical value.
	 * <p>
	 * WARNING: do not use because of inconsistent result
	 * 
	 * @see Williamson, Pau, 2012. “An Evaluation of Two Synthetic Small-Area Microdata Simulation 
	 * Methodologies: Synthetic Reconstruction and Combinatorial Optimisation.” 
	 * In Spatial Microsimulation: A Reference Guide for Users
	 * @see Huand, Z., Williamson, P., 2001. "A Comparison of Synthetic Reconstruction and Combinatorial
	 * Optimisation Approaches to the Creation of Small-area Microdata" Working paper online
	 * @return RSSZstaar indicator as a double
	 */
	public double getRSSZstar(INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number> inputMatrix,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		double expectedValue = 0d;
		double actualValue = 0d;
		double ssz = 0d;
		double chiFiveCritical = new ChiSquaredDistribution(inputMatrix.size())
				.inverseCumulativeProbability(criticalPValue);
		AFullNDimensionalMatrix<Integer> contingencyTable = GosplNDimensionalMatrixFactory
				.getFactory().createContingency(population);
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
				actualValue = contingencyTable.getVal(coord.values(), true).getValue();
				ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue / population.size()));
			}
			return ssz / chiFiveCritical;
		case GlobalFrequencyTable:
			for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * population.size();
				actualValue = contingencyTable.getVal(coord.values(), true).getValue();
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
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> distribution,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) throws IOException {		
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
	public void saveReport(File outputFile, Map<GosplIndicator, Number> report) 
			throws IOException {
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		String separator = ";";
		BufferedWriter bw;

		bw = Files.newBufferedWriter(outputFile.toPath());
		bw.write("Algo"+separator+"Pop size");
		for(GosplIndicator indicator : report.keySet())
			bw.write(separator+indicator.toString());
		bw.newLine();
		for(GosplIndicator indicator : report.keySet())
			bw.write(separator+decimalFormat.format(report.get(indicator).doubleValue()).toString());
		bw.flush();
	}

	// -------------------- Private inner methods -------------------- //
	
	private Number getStats(GosplIndicator indicator,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> distribution,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
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
