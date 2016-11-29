package spll.datamapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import core.io.geo.IGSGeofile;
import core.io.geo.RasterFile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.GSFeature;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.matcher.ISPLMatcherFactory;
import spll.datamapper.variable.ISPLVariable;
import spll.popmapper.normalizer.ASPLNormalizer;

/**
 * The mapper is the main concept of SPLL algorithm. It matches main geographical features
 * contain in a shape file to various geographical variables (e.g. other features, satellite image).
 * It also setup regression algorithm to compute the relationship between an attribute of main features
 * (dependent variable) and ancillary geographical variable (explanatory variables)
 * 
 * @author kevinchapuis
 *
 * @param <V>
 * @param <T>
 */
public abstract class ASPLMapperBuilder<V extends ISPLVariable, T> {
	
	protected final ShapeFile mainFile;
	protected final String propertyName;
	
	protected List<IGSGeofile> ancillaryFiles;
	protected ISPLMatcherFactory<V, T> matcherFactory;
	
	protected ISPLRegressionAlgo<V, T> regressionAlgorithm;
	
	protected ASPLNormalizer normalizer;
	
	public ASPLMapperBuilder(ShapeFile mainFile, String propertyName,
			List<IGSGeofile> ancillaryFiles) {
		this.mainFile = mainFile;
		this.propertyName = propertyName;
		this.ancillaryFiles = ancillaryFiles;
	}
	
	/**
	 * Setup the regression algorithm
	 * 
	 * @param regressionAlgorithm
	 */
	public void setRegressionAlgorithm(ISPLRegressionAlgo<V, T> regressionAlgorithm){
		this.regressionAlgorithm = regressionAlgorithm;
	}
	
	/**
	 * Setup the matcher factory, i.e. the object whose responsible for variable matching
	 * 
	 * @param matcherFactory
	 */
	public void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}
	
	/**
	 * Setup the object that will ensure output value format to fit built-in normalizer requirements
	 * 
	 * @param normalizer
	 */
	public void setNormalizer(ASPLNormalizer normalizer){
		this.normalizer = normalizer;
	}
	
	/**
	 * This method match all ancillary files with the main shape file. More precisely,
	 * all geographic variables ancillary files contain will be bind to corresponding feature
	 * of the main file. Each {@link ASPLMapperBuilder} has its own definition of how
	 * feature and geographical variable should match to one another (e.g. within, intersect)
	 * 
	 * @return
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public abstract SPLMapper<V, T> buildMapper() throws IOException, TransformException, InterruptedException, ExecutionException;
	
	/**
	 * build the output of spll regression based localization as pixel based format output.
	 * Format file argument {@code formatFile} must be an ancillaryFiles, see {@link #getAncillaryFiles()}
	 * 
	 * @param outputFile
	 * @param formatFile
	 * @return
	 * @throws IllegalRegressionException
	 * @throws TransformException
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 * @throws GSMapperException 
	 */
	public abstract float[][] buildOutput(RasterFile formatFile, boolean intersect, boolean integer) 
			throws IllegalRegressionException, TransformException, 
			IndexOutOfBoundsException, IOException, GSMapperException;
	
	/**
	 * build the output of Spll regression based localization as vector based format output.
	 * Format file argument {@code formatFile} must be an ancillaryFiles, see {@link #getAncillaryFiles()}
	 * 
	 * @param outputFile
	 * @param formatFile
	 * @return
	 */
	public abstract Map<GSFeature, Double> buildOutput(File outputFile, ShapeFile formatFile);
	
	// ------------------------ ACCESSORS ------------------------ //
	
	public List<IGSGeofile> getAncillaryFiles(){
		return Collections.unmodifiableList(ancillaryFiles);
	}
	
	public ShapeFile getMainFile(){
		return mainFile;
	}
	
	public String getMainPropertyName(){
		return propertyName;
	}
	
}
