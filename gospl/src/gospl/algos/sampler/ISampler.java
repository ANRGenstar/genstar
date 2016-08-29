package gospl.algos.sampler;

import java.util.List;

import gospl.algos.exception.GosplSampleException;

/**
 * The global contract for sampler --
 * 
 * TODO: explain more
 * TODO: implement a factory
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public interface ISampler<T> {

	public T draw() throws GosplSampleException;
	
	public List<T> draw(int numberOfDraw) throws GosplSampleException;
	
	public String toCsv(String csvSeparator);
	
}
