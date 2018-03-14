package gospl.sampler;

import java.util.Collection;

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
	public Collection<T> draw(int numberOfDraw);
	
	/**
	 * Should give an overview of the underlying distribution
	 * 
	 * @param csvSeparator
	 * @return
	 */
	public String toCsv(String csvSeparator);

}
