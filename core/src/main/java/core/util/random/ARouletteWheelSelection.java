package core.util.random;

import java.util.List;

/**
 * Defines a selection of roulette wheel (as known in genetic algorithms) which can be used for 
 * random sampling. 
 * 
 * @author Samuel Thiriot	
 */
public abstract class ARouletteWheelSelection<T extends Number> {

	protected List<T> distribution = null;
	protected List<?> keys = null;

	T total = null;

	public ARouletteWheelSelection(List<T> distribution) {
	
		this.setDistribution(distribution);
	}
	

	public void setKeys(List<?> keys) {
		this.keys = keys;
	}
	
	/**
	 * computes the sum of the distribution (later used for normalization)
	 */
	protected abstract T computeDistributionSum(List<T> distribution);
	
	/**
	 * Define the distribution of the roulette wheel. Drives costly operations 
	 * such as computation as the sum that will be reused later to sample .
	 * @param distribution
	 */
	public void setDistribution(List<T> distribution) {
		
		this.distribution = distribution;
		
		this.total = this.computeDistributionSum(distribution);
		
	}
	
	/**
	 * returns an index of the distribution based on the content of the wheel. 
	 * For instance for a distribution [0.1,0.8,0.1], indexes have respectively 10%, 80% and 10% of chances to 
	 * be selected. Note that normalization is driven, so [1.,8.,1.] would lead to the very same result.
	 * @return
	 */
	public abstract int drawIndex() throws IllegalStateException;
	
	/**
	 * Returns one of the keys based on the distribution and keys passed as parameter using setters.
	 * @param keys
	 * @param distribution
	 * @return
	 * @throws IllegalStateException if the keys or the distribution were not defined first 
	 */
	@SuppressWarnings("unchecked")
	public <X> X drawObject() throws IllegalStateException {
		try {
			return (X) keys.get(drawIndex());
		} catch (NullPointerException e) {
			throw new IllegalStateException("please call setKeys() first to define the keys");
		}
	}


}
