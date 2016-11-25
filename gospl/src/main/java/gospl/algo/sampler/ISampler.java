package gospl.algo.sampler;

import java.util.List;
import java.util.Random;

import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.util.GosplBasicDistribution;

/**
 * The global contract for sampler --
 * 
 * Must be created from a {@link GosplSPGeneratorFactory}
 * 
 * TODO: explain more
 * 
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public interface ISampler<T> {
	
	// ---------------- setup methods ---------------- //

	/**
	 * Set the pseudo-random engine
	 * 
	 * @param rand
	 */
	public void setRandom(Random rand);
	
	/**
	 * Set the distribution to draw within
	 * 
	 * @param distribution
	 */
	public void setDistribution(GosplBasicDistribution distribution);
	
	/**
	 * Set the distribution to draw within in form of a n-dimensional matrix
	 * 
	 * @param distribution
	 */
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution);
	
	// ---------------- main contract ---------------- //
	
	/**
	 * Main method that return a random draw given a pseudo-random engine 
	 * and a distribution of probability
	 * 
	 * @return
	 */
	public T draw();
	
	/**
	 * Return {@code numberOfDraw} number of draw. Due to performance optimization,
	 * it could be based on another method than {@link #draw()}
	 * 
	 * @param numberOfDraw
	 * @return
	 */
	public List<T> draw(int numberOfDraw);
	
	/**
	 * Should give an overview of the underlying distribution
	 * 
	 * @param csvSeparator
	 * @return
	 */
	public String toCsv(String csvSeparator);
	
}
