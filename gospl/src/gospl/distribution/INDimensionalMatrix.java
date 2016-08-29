package gospl.distribution;

import java.util.Map;

import gospl.distribution.coordinate.ACoordinate;

/**
 * Main interface that forces n dimensional matrix to specify: 
 * <p>
 * <ul>
 *  <li> {@code <D>} the type of dimension to be used 
 *  <li> {@code <A>} the type of aspect dimensions contain 
 *  <li> {@code <T>} the type of value the matrix contains
 * </ul> 
 * <p>
 * There is also several methods to access and set the matrix
 * 
 * @author kevinchapuis
 *
 * @param <D> Type of random variables
 * @param <A> Type of variables' values
 * @param <T> Type of values the matrix is made of
 */
public interface INDimensionalMatrix<D, A, T extends Number> {
	
	/**
	 * Draw a particular {@link ACoordinate} using the mutli-dimensional distribution. This drawing
	 * should return a complete coordinate: each dimension of the distribution should be represented
	 * 
	 * @return {@link ACoordinate}
	 * @throws GenstarSampleException 
	 */
	public ACoordinate<D, A> draw();
	
	/**
	 * Return a view of matrix: each coordinate of the matrix is mapped with a numerical 
	 * value according to the underlying n Dimensional matrix
	 * 
	 */
	public Map<ACoordinate<D, A>, T> getMatrix();

	/**
	 * The concrete size of the matrix, i.e. the number of coordinate / value pairs
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Check if the matrix is empty
	 * 
	 * @return <code>true</code> if there is not any coordinate / value pairs
	 */
	public boolean isEmpty();
	
}
