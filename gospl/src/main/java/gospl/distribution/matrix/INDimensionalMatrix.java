package gospl.distribution.matrix;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.util.data.GSDataParser;
import gospl.distribution.exception.IllegalNDimensionalMatrixAccess;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;


/**
 * Main interface that forces n dimensional matrix to specify: 
 * <p>
 * <ul>
 *  <li> {@code <D>} the type of dimension to be used 
 *  <li> {@code <A>} the type of aspect dimensions contain 
 *  <li> {@code <T>} the type of value the matrix contains
 * </ul> 
 * <p>
 * There is also several methods to access and set the matrix. The collection
 * format is based on spares collection abstraction: there is no memory allocated
 * to null value and there is not any coordinate associated in the matrix
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
	 * <p>
	 * <b>WARNING: return the actual control associated to this matrix. 
	 * This method enables a direct access to the content of the matrix with no computation at all.</b>
	 * 
	 * @param coordinate
	 * @return {@link AControl} associated to the given {@link ACoordinate}
	 */
	public AControl<T> getVal(ACoordinate<D, A> coordinate);
	
	/**
	 * Compute the matrix aggregated value according to one dimension's aspect
	 * 
	 * @param aspect
	 * @return
	 * @throws IllegalNDimensionalMatrixAccess 
	 */
	public AControl<T> getVal(A aspect) throws IllegalNDimensionalMatrixAccess;
	
	/**
	 * Compute the matrix aggregated value according to a set of aspect of one or several dimension
	 * 
	 * @param aspects
	 * @return
	 */
	public AControl<T> getVal(Collection<A> aspects);
	
	/**
	 * Compute the matrix aggregated value according to a set of aspect of one or several dimension.
	 * if defaultToNul is true, then a missing value will return null.
	 * 
	 * @param aspects
	 * @param defaultToNul
	 * @return
	 */
	public AControl<T> getVal(Collection<A> aspects, boolean defaultToNul);

	/**
	 * Compute the matrix aggregated value according to a set of aspect of one or several dimension.
	 * Pass parameters as: "gender", "female", "age", "60 and more", ... 
	 * 
	 * @param coordinates
	 * @return
	 */
	public AControl<T> getVal(String ... coordinates);
	
	/**
	 * Compute the matrix aggregated value according to a set of aspect of one or several dimension
	 * 
	 * @param aspects
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AControl<T> getVal(A ... aspects);

	
	/**
	 * Compute the total sum of the entire matrix
	 * 
	 * @return
	 */
	public AControl<T> getVal();

	
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
	 * Add a new value associated with a new coordinate. The add can fails if the specified coordinate in parameter
	 * has already be binding with another value
	 * 
	 * @param coordinates
	 * @param value
	 * @return
	 */
	public boolean addValue(ACoordinate<D, A> coordinates, T value);
	
	/**
	 * Add a new value associated with a new coordinate. The add can fails if the specified coordinate in parameter
	 * has already be binding with another value.
	 * This convenience function is called like: addValue(0.1,"gender","male","age","12-25"...)
	 * 
	 * @param coordinates
	 * @param value
	 * @return
	 */
	public boolean addValue(T value, String ... coordinates);


	/**
	 * Add or replace the value associate with the coordinate in parameter for the new value passed as method's argument  
	 * 
	 * @param coordinate
	 * @param value
	 * @return <code>true</code> if the value has been added, <code>false</code> otherwise
	 */
	public boolean setValue(ACoordinate<D, A> coordinate, AControl<? extends Number> value);
	
	/**
	 * Add or replace the value associate with the coordinate in parameter for the new value passed as method's argument  
	 * 
	 * @param coordinate
	 * @param value
	 * @return <code>true</code> if the value has been added, <code>false</code> otherwise
	 */
	public boolean setValue(ACoordinate<D, A> coordinate, T value);

	
	/**
	 * Add or replace the value associate with the coordinate in parameter for the new value passed as method's argument  
	 * This convenience function is called like: addValue(0.1,"gender","male","age","12-25"...)
	 *
	 * @param coordinate
	 * @param value
	 * @return <code>true</code> if the value has been added, <code>false</code> otherwise
	 */
	public boolean setValue(T value, String ... coordinates);

	
// ------------------------- Accessors ------------------------- //
	
	/**
	 * Return a view of the inner matrix: each coordinate is mapped with 
	 * a numerical value 
	 * 
	 */
	public Map<ACoordinate<D, A>, AControl<T>> getMatrix();
	
	/**
	 * Return an ordered view of the inner matrix: each coordinate is mapped
	 * with a numerical value sorted by increasing number 
	 * @return
	 */
	public LinkedHashMap<ACoordinate<D, A>, AControl<T>> getOrderedMatrix();
	
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
	 * Searches for the dimension having this name. 
	 * @param name
	 * @return
	 * @throws IllegalArgumentException
	 */
	public D getDimension(String name) throws IllegalArgumentException;
	
	/**
	 * A complete view of the dimensions of the matrix
	 * @return
	 */
	public Map<APopulationAttribute, Set<APopulationValue>> getDimensionsAsAttributesAndValues();

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
	 * Check if this coordinate fits the matrix requirement
	 * 
	 * @param coordinate
	 * @return
	 */
	public boolean isCoordinateCompliant(ACoordinate<D, A> coordinate);
	
	/**
	 * Retrieve all coordinate that describe this set of value.
	 * Simply translated, this will return all coordinates which
	 * contains the {@code values} (one per dimension) passed as
	 * argument
	 * 
	 * @param values
	 * @return
	 */
	public Collection<ACoordinate<D, A>> getCoordinates(Set<A> values);
	

	/**
	 * Retrieve all coordinate that describe this set of value.
	 * Simply translated, this will return all coordinates which
	 * contains the {@code values} (one per dimension) passed as
	 * argument
	 * 
	 * pass the parameters as: "gender", "male", "age", "12-25"... 
	 * 
	 * @param keyAndVal
	 * @return
	 */
	public Collection<ACoordinate<D, A>> getCoordinates(String ... keyAndVal) throws IllegalArgumentException;

	/**
	 * Retrieve all coordinate that describe this set of value.
	 * Simply translated, this will return all coordinates which
	 * contains the {@code values} (one per dimension) passed as
	 * argument
	 * 
	 * pass the parameters as: "gender", "male", "age", "12-25"... 
	 * 
	 * @param keyAndVal
	 * @return
	 */
	public Set<A> getValues(String ... keyAndVal) throws IllegalArgumentException;

	/**
	 * Retrieve the only coordinate that describe this set of value.
	 * 
	 * pass the parameters as: "gender", "male", "age", "12-25"... 
	 * 
	 * @param keyAndVal
	 * @return
	 */
	public ACoordinate<D, A> getCoordinate(String ... keyAndVal) throws IllegalArgumentException;

// ------------------------- utility methods ------------------------- //
	
	
	/**
	 * Checks if all of the coordinates of the matrix have defined values.
	 * Note that not all matrices should have this contract. Sparse matricies are often relevant.
	 * 
	 * @param checkGlobalSum
	 * @param checkAllCoordinatesHaveValue
	 * @return true if the 
	 */
	public boolean checkAllCoordinatesHaveValues();
	
	/**
	 * Ensures the global contract of the matrix is ok depending to its type: 
	 * a global frequency has to sump up to 1, for instance. 
	 * If the type enables no check, true is always returned.
	 * @return
	 */
	public boolean checkGlobalSum();
	
	public String toString();
	
	public String toCsv(char csvSeparator);

	/**
	 * Get relative {@code T} null value
	 * 
	 * @return
	 */
	public AControl<T> getNulVal();

	/**
	 * Get the value that guarantee that any {@code T} value
	 * multiply by {@link #getIdentityProductVal()} stay the same
	 * 
	 * @return
	 */
	public AControl<T> getIdentityProductVal();

	/**
	 * Parses a value from a string and encapsulates it in a {@link AControl} 
	 * 
	 * @param parser
	 * @param val
	 * @return
	 */
	public AControl<T> parseVal(GSDataParser parser, String val);


	/**
	 * if it is relevant, normalizes the values
	 */
	public void normalize() throws IllegalArgumentException;

	/**
	 * Returns a human readable label, or null if undefined.
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Returns the genesis of the matrix, that is the successive steps that brought it to its 
	 * current state. Useful to expose meaningful error messages to the user.
	 * @return
	 */
	public String getGenesisAsString();
	
	/**
	 * imports into this matrix the genesis of another one. 
	 * Should be called after creating a matrix to keep a memory of where it comes from.
	 * @param o
	 */
	public void inheritGenesis(AFullNDimensionalMatrix<?> o);
	
	/**
	 * add one line to the genesis (history) of this matrix. 
	 * This line should better be kept quiet short for readibility.
	 * @param step
	 */
	public void addGenesis(String step);
	

	
}
