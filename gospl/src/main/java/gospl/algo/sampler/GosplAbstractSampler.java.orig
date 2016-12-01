package gospl.algo.sampler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public abstract class GosplAbstractSampler implements ISampler<ACoordinate<ASurveyAttribute, ASurveyValue>> {

	protected Logger logger = LogManager.getLogger();
	
	public GosplAbstractSampler() {
	}


	// -------------------- setup methods -------------------- //

	

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution) {
		this.setDistribution(new GosplBasicDistribution(distribution));
	}
	
	
	// -------------------- main contract -------------------- //


	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<ASurveyAttribute, ASurveyValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}


	
}
