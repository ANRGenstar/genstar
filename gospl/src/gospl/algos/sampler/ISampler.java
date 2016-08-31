package gospl.algos.sampler;

import java.util.List;

import gospl.algos.exception.GosplSamplerException;
import gospl.generator.GosplSPGeneratorFactory;

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

	public T draw() throws GosplSamplerException;
	
	public List<T> draw(int numberOfDraw) throws GosplSamplerException;
	
	public String toCsv(String csvSeparator);
	
}
