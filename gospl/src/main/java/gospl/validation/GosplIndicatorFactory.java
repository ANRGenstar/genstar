package gospl.validation;

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
			AFullNDimensionalMatrix<Integer> contingencyTable = GosplNDimensionalMatrixFactory
				.getFactory().createContingency(population); 
			return inputMatrix.getMatrix().entrySet()
					.stream().mapToInt(e -> Math.abs(contingencyTable.getVal(e.getKey().values())
							.getValue() - e.getValue().getValue().intValue()))
					.sum();
		case GlobalFrequencyTable:
			AFullNDimensionalMatrix<Double> frequencyTable = GosplNDimensionalMatrixFactory
				.getFactory().createDistribution(population);
			return inputMatrix.getMatrix().entrySet()
					.stream().mapToDouble(e -> Math.abs(frequencyTable.getVal(e.getKey().values())
							.getValue() - e.getValue().getValue().doubleValue()))
					.sum();
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}

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
		int nbCells = inputMatrix.size();
		double expectedValue = 0d;
		double actualValue = 0d;
		double s = 0d;
		double rmse = 0d;
		AFullNDimensionalMatrix<Integer> contingencyTable = GosplNDimensionalMatrixFactory
				.getFactory().createContingency(population);
		switch (inputMatrix.getMetaDataType()) {
		case ContingencyTable:
			for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue();
				actualValue = contingencyTable.getVal(coord.values()).getValue();
				rmse += Math.pow(expectedValue - actualValue, 2) / nbCells;
				s += Math.pow(actualValue, 2) / nbCells;
			}
			return Math.sqrt(rmse) / inputMatrix.getVal().getValue().intValue();
		case GlobalFrequencyTable:
			for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * population.size();
				actualValue = contingencyTable.getVal(coord.values()).getValue();
				rmse += Math.pow(expectedValue - actualValue, 2) / nbCells;
				s += Math.pow(actualValue, 2) / nbCells;
			}
			return Math.sqrt(rmse) / s;
		case LocalFrequencyTable:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "of type "+ inputMatrix.getMetaDataType());
		default:
			throw new IllegalArgumentException("Input contingency argument cannot be "
					+ "a segmented matrix with multiple matrix meta data type");
		}
	}

	/**
	 * RSSZ is an overall estimation of goodness of fit based on several indicator.
	 * It is first based on Z-score that focus on cell based indicator of error. SSZ is
	 * the sum of square Z-score and RSSZ is a proposed relative indicator, that is the SSZ
	 * divided by the chi square 5% critical value.
	 * 
	 * @see Williamson, Pau, 2012. “An Evaluation of Two Synthetic Small-Area Microdata Simulation 
	 * Methodologies: Synthetic Reconstruction and Combinatorial Optimisation.” 
	 * In Spatial Microsimulation: A Reference Guide for Users
	 * @see Huand, Z., Williamson, P., 2001. "A Comparison of Synthetic Reconstruction and Combinatorial
	 * Optimisation Approaches to the Creation of Small-area Microdata" Working paper online
	 * @return
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
				actualValue = contingencyTable.getVal(coord.values()).getValue();
				ssz += Math.pow(actualValue - expectedValue, 2) / (expectedValue * (1 - expectedValue / population.size()));
			}
			return ssz / chiFiveCritical;
		case GlobalFrequencyTable:
			for(ACoordinate<APopulationAttribute, APopulationValue> coord : inputMatrix.getMatrix().keySet()){			 
				expectedValue = inputMatrix.getVal(coord).getValue().doubleValue() * population.size();
				actualValue = contingencyTable.getVal(coord.values()).getValue();
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

}
