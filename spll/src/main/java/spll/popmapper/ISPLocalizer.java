package spll.popmapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.geotools.feature.SchemaException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import core.metamodel.IPopulation;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;
import core.metamodel.geo.io.IGSGeofile;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spll.SpllPopulation;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.popmapper.normalizer.SPLUniformNormalizer;

/**
 * This is the main object to localize population. It is the main ressource in Spll process.
 * It contains a <i>must have</i> module and two optional ones:
 * <p>
 * <ul>
 * <li>1) <b>MANDATORY</b>: A geographically referenced population, i.e. {@link SpllPopulation}
 * <li>2) <b>OPTIONAL</b>: A geographical match between a population's entity attribute and geographical entities (denote as <i>match</i>)
 * <li>3) <b>OPTIONAL</b>: A density map (without any match with population) OR a spatial regression setup to estimate one
 * </ul>
 * </p>
 * These three options outline what Spll localization process cover: <br> 
 * (1) localize entity into nest {@link APopulationEntity#getNest()} <br>
 * (2) match entity with the geography {@link APopulationEntity#getLocation()} (if no match, it is equal to the nest) <br> 
 * (3) ancillary information on density (even estimated one using regression techniques) <br>
 * <p>
 * 
 * @author kevinchapuis
 * @author taillandier patrick
 *
 */
public interface ISPLocalizer {

	/**
	 * Provide the higher order method that take a population and 
	 * return the population with localisation indication 
	 * 
	 * @param population
	 * @return
	 */
	public IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> localisePopulation();
	
	/**
	 * Setup a "matched geography" between population's entities attribute and a geographical entitites
	 * 
	 * @param match
	 * @param keyAttPop
	 * @param keyAttMatch
	 */
	public void setMatcher(IGSGeofile<? extends AGeoEntity> match, String keyAttPop, String keyAttMatch);
	
	/**
	 * This method must setup matcher variable (i.e. the number of entity) in
	 * the proper output format geofile
	 * 
	 * @return
	 * @throws TransformException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws MismatchedDimensionException 
	 * @throws SchemaException 
	 */
	public IGSGeofile<? extends AGeoEntity> getMatcher(File match) 
			throws MismatchedDimensionException, IllegalArgumentException, IOException, TransformException, SchemaException;
	
	/**
	 * Setup a density map - through external files that define spatial contingency without any 
	 * match with population entities
	 * 
	 * @param entityNbAreas
	 * @param numberProperty
	 */
	public void setMapper(IGSGeofile<? extends AGeoEntity> map, String numberProperty);
	
	/**
	 * Setup a density map - from the result of spatial interpolation: this interpolation
	 * is based on the previous match !
	 * 
	 * WARNING: will throw a Exception if no match have been set before
	 * 
	 * @param endogeneousVarFile
	 * @param varList
	 * @param lmRegressionOLS
	 * @param splUniformNormalizer
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IllegalRegressionException
	 * @throws IndexOutOfBoundsException
	 * @throws GSMapperException
	 * @throws SchemaException 
	 */
	public void setMapper(List<IGSGeofile<? extends AGeoEntity>> endogeneousVarFile, 
			List<? extends AGeoValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IOException, TransformException, 
	InterruptedException, ExecutionException, IllegalRegressionException, IndexOutOfBoundsException, GSMapperException, SchemaException;
	
	/**
	 * Setup a density map - from the result of spatial interpolation: this interpolation
	 * is based on a given map file 
	 * 
	 * @param mainMapper
	 * @param mainAttribute
	 * @param endogeneousVarFile
	 * @param varList
	 * @param lmRegressionOLS
	 * @param splUniformNormalizer
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IllegalRegressionException
	 * @throws IndexOutOfBoundsException
	 * @throws GSMapperException
	 * @throws SchemaException 
	 */
	public void setMapper(IGSGeofile<? extends AGeoEntity> mainMapper , String mainAttribute, 
			List<IGSGeofile<? extends AGeoEntity>> endogeneousVarFile, 
			List<? extends AGeoValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IOException, TransformException, 
	InterruptedException, ExecutionException, IllegalRegressionException, IndexOutOfBoundsException, GSMapperException, SchemaException;
	
}
