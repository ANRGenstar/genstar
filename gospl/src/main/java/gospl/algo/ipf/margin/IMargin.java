package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Set;

import gospl.distribution.matrix.control.AControl;

public interface IMargin<A, V, T extends Number> {

	/**
	 * Gives the dimension that refereed to control aspect of IPF 
	 * 
	 * @return
	 */
	public A getControlDimension();
	
	/**
	 * Gives the dimension that refereed to seed aspect of IPF
	 * 
	 * @return
	 */
	public A getSeedDimension();
	
	/**
	 * Retrieves abstract control number associated to this seed margin descriptor
	 * 
	 * @param seedMargin
	 * @return
	 */
	public AControl<T> getControl(Set<V> seedMargin);
	
	/**
	 * Retrieves the all seed marginal descriptors for this dimension
	 * 
	 * @param controlMargin
	 * @return
	 */
	public Collection<Set<V>> getSeedMarginalDescriptors();
	
	/**
	 * Marginal size
	 * 
	 * @return
	 */
	public int size();
	
}
