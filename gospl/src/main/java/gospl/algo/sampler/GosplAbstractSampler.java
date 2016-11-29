package gospl.algo.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public abstract class GosplAbstractSampler implements ISampler<ACoordinate<ASurveyAttribute, AValue>> {

	/* The random number generator used to sample from the distribution. */
	protected Random random = ThreadLocalRandom.current();

	public GosplAbstractSampler() {
		// TODO Auto-generated constructor stub
	}


	// -------------------- setup methods -------------------- //

	@Override
	public final void setRandom(Random random) {
		this.random = random;
	}
	

	@Override
	public final void setDistribution(AFullNDimensionalMatrix<Double> distribution) {
		this.setDistribution(new GosplBasicDistribution(distribution));
	}
	
	
	// -------------------- main contract -------------------- //


	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<ASurveyAttribute, AValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}


	
}
