package gospl.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import core.util.random.GenstarRandomUtils;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.util.GSUtilPopulation;

public class GosplIndicatorTest {

	private static final double DELTA = 0.05;
	private static final int REPLICATION = 100;
	private static final double EPSILON = Math.pow(10, -8);
	
	private IPopulation<ADemoEntity,Attribute<? extends IValue>> population;
	
	int cDiff;
	int dcConstant = -1;
	private INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> cReference;
	private AFullNDimensionalMatrix<Integer> cComparison;
	
	double sDisruption = 0.05;
	double sdQuantity = 1/100;
	double ddConstant = 0.1;
	List<Integer> dFactors = Arrays.asList(1,2,5,10);
	private AFullNDimensionalMatrix<Double> dReference;
	private AFullNDimensionalMatrix<Double> dComparison;
	
	@Before
	public void setUp() {
		// SETUP REFERENCE MATRICES
		try {
			population = new GSUtilPopulation().buildPopulation(1000);
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cReference = GosplNDimensionalMatrixFactory.getFactory().createContingency(population);
		dReference = GosplNDimensionalMatrixFactory.getFactory().createDistribution(population);
		System.out.println(cReference.size());
		
		// SETUP DISTURBED CONTINGENCY
		cComparison = GosplNDimensionalMatrixFactory.getFactory().createContingency(cReference);
		cComparison.getMatrix().values().stream().forEach(v -> v.add(dcConstant));
		// SETUP DISTURBED DISTRIBUTION
		dComparison = GosplNDimensionalMatrixFactory.getFactory().cloneDistribution(dReference);
		dComparison.getMatrix().keySet().stream()
			.limit(Math.round(dComparison.size()*sdQuantity)+1)
			.forEach(c -> dComparison.getVal(c)
					.multiply(GenstarRandomUtils.flip(0.5) ? 1-sDisruption : 1+sDisruption));
		
	}
	
	/**
	 * {@link GosplIndicator#TAE}
	 */
	@Test
	public void testTAE() {
		
		// Two same distribution get TAE = 0.0
		assertEquals(GosplIndicatorFactory.getFactory().getIntegerTAE(cReference, 
				GosplNDimensionalMatrixFactory.getFactory().createContingency(cReference)), 0.0, EPSILON);
		
		int iTAE = GosplIndicatorFactory.getFactory().getIntegerTAE(cReference, cComparison);
		int iTACE = GosplIndicatorFactory.getFactory().getIntegerTACE(cReference, cComparison, EPSILON);
		
		// Each cell have been diminish by #dcConstant, hence TAE = number of cells (also each cell is different, so TACE = number of cells too)
		assertEquals(cReference.size(), cReference.getVal().getValue() - cComparison.getVal().getValue());
		assertEquals(iTAE, cReference.size());
		assertEquals(iTACE, cReference.size());
		
		this.testIndicator(GosplIndicator.TAE, dReference);
		
		
	}
	
	/**
	 * {@link GosplIndicator#AAPD}
	 */
	@Test
	public void testAAPD() {
		
		// Tow identical distribution should get AAPD = 0.0
		assertEquals(GosplIndicatorFactory.getFactory().getIntegerAAPD(cReference, 
				GosplNDimensionalMatrixFactory.getFactory().createContingency(cReference)), 0.0, EPSILON);
		
		// Each cell have been diminish by #dcConstant, hence AAPD = |#dcConstant| 
		assertEquals(Math.abs(dcConstant), 
				GosplIndicatorFactory.getFactory().getIntegerAAPD(cReference, cComparison), EPSILON);
		
		this.testIndicator(GosplIndicator.AAPD, dReference);
		
	}
	
	/**
	 * {@link GosplIndicator#SRMSE}
	 */
	@Test
	public void testSRMSE() {
		
		// Tow identical distribution should get AAPD = 0.0
		assertEquals(GosplIndicatorFactory.getFactory().getIntegerSRMSE(cReference, 
				GosplNDimensionalMatrixFactory.getFactory().createContingency(cReference)), 0.0, EPSILON);
		
		// Each cell have been diminish by #dcConstant, hence SRMSE = |#dcConstant| / S factor
		double sFactor = cComparison.getMatrix().values().stream().mapToInt(v -> v.getValue().intValue()).max().getAsInt() - 
				cComparison.getMatrix().values().stream().mapToInt(v -> v.getValue().intValue()).min().getAsInt();
		assertEquals(Math.abs(dcConstant) / sFactor, 
				GosplIndicatorFactory.getFactory().getIntegerSRMSE(cReference, cComparison), EPSILON);
		
		this.testIndicator(GosplIndicator.SRMSE, dReference);
		
	}
	
	/**
	 * {@link GosplIndicator#RSSZstar}
	 */
	@Test
	public void testRSSZstar() {
		
		// Tow identical distribution should get AAPD = 0.0
		assertEquals(GosplIndicatorFactory.getFactory().getIntegerRSSZstar(cReference, 
				GosplNDimensionalMatrixFactory.getFactory().createContingency(cReference)), 0.0, EPSILON);
				
		this.testIndicator(GosplIndicator.RSSZstar, dReference);
		
	}
	
	/*
	 * Test the replication of indicator with two principle:
	 * 
	 * 1) The more you introduce disruption in matrix, the greater will be error indicator (whatever the indicator is)
	 * 2) When replicate x times same indicator with same disruption span will give very close result on average
	 * 
	 */
	private void testIndicator(GosplIndicator indicator, 
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number> inputMatrix) {
		// Increasing disturbance factor should give worse indicator
		Map<Integer, Double> mapFacor = dFactors.stream().collect(Collectors.toMap(Function.identity(), 
				factor -> IntStream.range(0, REPLICATION)
				.mapToDouble(i -> GosplIndicatorFactory.getFactory().getIndicator(indicator, dReference, 
						this.disturbeDistribution(GosplNDimensionalMatrixFactory.getFactory()
								.cloneDistribution(dReference), factor)))
				.average().getAsDouble()
				));

		for(int i = 0; i < dFactors.size()-1; i++) {
			assertThat(mapFacor.get(dFactors.get(i)), lessThan(mapFacor.get(dFactors.get(i+1))));
		}


		// Distribution with similar disturbance factor should have close indicators: average TAE over
		// #REPLICATION number of replications compared to one another
		for(int factor : dFactors) {
			double formerIndicator = mapFacor.get(factor);
			double newIndicator = IntStream.range(0, REPLICATION)
					.mapToDouble(i -> GosplIndicatorFactory.getFactory().getIndicator(indicator, dReference, 
							this.disturbeDistribution(GosplNDimensionalMatrixFactory.getFactory()
									.cloneDistribution(dReference), factor)))
					.average().getAsDouble(); 
			assertThat(Math.abs(formerIndicator - newIndicator) / formerIndicator, lessThan(DELTA));
		}
	}
	
	private AFullNDimensionalMatrix<Double> disturbeDistribution(AFullNDimensionalMatrix<Double> distribution, 
			int dFactor) {
		int skipFactor = dFactors.stream().mapToInt(i->i).max().getAsInt() - dFactor + 1;
		int limit = distribution.size() / skipFactor + Math.min(distribution.size() % skipFactor, 1);
		for(ACoordinate<Attribute<? extends IValue>, IValue> coordinate : distribution.getMatrix().keySet()
				.stream().limit(limit).collect(Collectors.toList())) {
			double rnd = GenstarRandom.getInstance().nextDouble() * ddConstant * dFactor;
			rnd = rnd >= 1 ? 0.99 : rnd;
			double rndFactor = GenstarRandomUtils.flip(0.5) ? 1 - rnd : 1 + rnd;
			distribution.getVal(coordinate).multiply(rndFactor);
		}
		distribution.normalize();
		return distribution;
	}

}
