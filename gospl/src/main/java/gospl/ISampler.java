package gospl;

import java.util.List;
import java.util.Random;

import gospl.algos.exception.GosplSamplerException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.util.BasicDistribution;

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

	public void setRandom(Random rand);
	
	public void setDistribution(BasicDistribution distribution) throws GosplSamplerException;
	
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution) throws GosplSamplerException;
	
	// ---------------- main contract ---------------- //
	
	public T draw() throws GosplSamplerException;
	
	public List<T> draw(int numberOfDraw) throws GosplSamplerException;
	
	public String toCsv(String csvSeparator);
	
}
