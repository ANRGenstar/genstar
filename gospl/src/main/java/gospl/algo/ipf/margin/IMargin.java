package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Set;

import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;

/**
 * Higher order abstraction that describes marginal of a n dimensional matrix,
 * which can be a segmented matrix in the sens of {@link ASegmentedNDimensionalMatrix}.
 * <\p>
 * A marginal is constructed as follow: we have a referent dimension of type A, and a
 * set of other related dimension; for each other dimension we can have a description
 * in the form of a {@link AControl}.
 * <\p>
 * TODO: better description
 * 
 * @author kevinchapuis
 *
 * @param <A>
 * @param <V>
 * @param <T>
 */
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
	 * Retrieves the all seed marginal descriptors for this dimension
	 * 
	 * @param controlMargin
	 * @return
	 */
	public Collection<Set<V>> getSeedMarginalDescriptors();
	
	/**
	 * Gives the collection of controls 
	 * 
	 * @return
	 */
	public Collection<AControl<T>> getControls();
	
	/**
	 * Retrieves abstract control number associated to this seed margin descriptor
	 * 
	 * @param seedMargin
	 * @return
	 */
	public AControl<T> getControl(Set<V> seedMargin);
	
	/**
	 * Marginal size
	 * 
	 * @return
	 */
	public int size();
	
}
