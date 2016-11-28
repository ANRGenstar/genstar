package gospl.distribution.matrix;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.util.data.GSDataParser;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.GSSurveyType;


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

// ----------------------- Main contract ----------------------- //
	
	/**
	 * Retrieve the matrix value according to the coordinate passed in method parameter. 
	 * 
	 * @param coordinate
	 * @return {@link AControl} associated to the given {@link ACoordinate}
	 */
	public AControl<T> getVal(ACoordinate<D, A> coordinate);
	
	/**
	 * Retrieve the matrix aggregated value according to one dimension's aspect
	 * 
	 * @param aspect
	 * @return
	 * @throws IllegalNDimensionalMatrixAccess 
	 */
	public AControl<T> getVal(A aspect) throws IllegalNDimensionalMatrixAccess;
	
	/**
	 * Retrieve the matrix aggregated value according to a set of aspect of one or several dimension
	 * 
	 * @param aspects
	 * @return
	 */
	public AControl<T> getVal(Collection<A> aspects);
	
	/**
	 * Add a new value associated with a new coordinate. The add can fails if the specified coordinate in parameter
	 * has already be binding with another value
	 * 
	 * @param coordinates
	 * @param value
	 * @return <code>true</code> if the value has been added, <code>false</code> otherwise
	 */
	public boolean addValue(ACoordinate<D, A> coordinates, AControl<? extends Number> value);
	
	/**
	 * Add or replace the value associate with the coordinate in parameter for the new value passed as method's argument  
	 * 
	 * @param coordinate
	 * @param value
	 * @return <code>true</code> if the value has been added, <code>false</code> otherwise
	 */
	public boolean setValue(ACoordinate<D, A> coordinate, AControl<? extends Number> value);
	
// ------------------------- Accessors ------------------------- //
	
	/**
	 * Return a view of matrix: each coordinate of the matrix is mapped with a numerical 
	 * value according to the underlying n Dimensional matrix
	 * 
	 */
	public Map<ACoordinate<D, A>, AControl<T>> getMatrix();
	
	/**
	 * Return the empty coordinate of this matrix
	 * 
	 * @return
	 */
	public ACoordinate<D, A> getEmptyCoordinate();
	
	/**
	 * The dimensions of the matrix
	 * 
	 * @return
	 */
	public Set<D> getDimensions();
	
	/**
	 * The dimensions associated with a spectific aspect
	 * 
	 * @param aspect
	 * @return
	 * @throws IllegalNDimensionalMatrixAccess
	 */
	public D getDimension(A aspect);
	
	/**
	 * Return all the values dimension contains 
	 * 
	 * @return
	 */
	public Set<A> getAspects();
	
	/**
	 * Return the values the spectified dimension [{@link IAttribute}] is made of
	 * 
	 * @param dimension
	 * @return
	 */
	public Set<A> getAspects(D dimension);
	
// ------------------------- descriptors ------------------------- //
	
	/**
	 * The concrete size of the matrix, i.e. the number of coordinate / value pairs
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Inform wether the matrix represents a "full distribution matrix" (i.e. each dimension related to all other dimensions)
	 * or a "segmented distribution matrix" (i.e. at least two dimension are unrelated)  
	 * 
	 * @return
	 */
	public boolean isSegmented();
	
	/**
	 * Gives the {@link GSSurveyType} that characterize "frame of referent" for this matrix. This in 
	 * turn inform about the specific target of the {@link AControl} associated to coordinate.
	 * 
	 * {@see GosplMetatDataType}
	 * 
	 * @return
	 */
	public GSSurveyType getMetaDataType();

// ------------------------- coordinate management ------------------------- //

	/**
	 * Check if this coordinate feet the matrix requirement
	 * 
	 * @param coordinate
	 * @return
	 */
	public boolean isCoordinateCompliant(ACoordinate<D, A> coordinate);

// ------------------------- utility methods ------------------------- //
	
	public String toString();
	
	public String toCsv(char csvSeparator);

	public AControl<T> getNulVal();

	public AControl<T> getIdentityProductVal();

	public AControl<T> parseVal(GSDataParser parser, String val);
	
}