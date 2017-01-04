package spll.popmapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;
import core.metamodel.geo.io.GeoGSFileType;
import core.metamodel.geo.io.IGSGeofile;
import core.metamodel.pop.APopulationEntity;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandom;
import spll.SpllPopulation;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.variable.ISPLVariable;
import spll.entity.GSFeature;
import spll.entity.GSPixel;
import spll.entity.GeoEntityFactory;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;
import spll.io.SPLGeofileFactory;
import spll.io.SPLRasterFile;
import spll.io.SPLVectorFile;
import spll.popmapper.constraint.SpatialConstraint;
import spll.popmapper.normalizer.SPLUniformNormalizer;
import spll.popmapper.pointInalgo.PointInLocalizer;
import spll.popmapper.pointInalgo.RandomPointInLocalizer;
import spll.util.SpllUtil;


public class SPUniformLocalizer implements ISPLocalizer {

	private SpllPopulation population;

	private IGSGeofile<? extends AGeoEntity> match; //main referenced area for placing the agents (ex: Iris)
	private IGSGeofile<? extends AGeoEntity> map; //gives the number of entities per area (ex: regression cells)

	private List<SpatialConstraint> constraints; //spatial constraints related to the placement of the entities in their nest 
	private PointInLocalizer pointInLocalizer; //allows to return one or several points in a geometry

	private String keyAttMap; //name of the attribute that contains the number of entities in the entityNbAreas file
	private String keyAttPop; //name of the attribute that is used to store the id of the referenced area  in the population
	private String keyAttMatch; //name of the attribute that is used to store the id of the referenced area in the entityNbAreas file

	private Random rand;

	/**
	 * Private constructor to setup random engine
	 */
	private SPUniformLocalizer() {
		rand = GenstarRandom.getInstance();
		pointInLocalizer = new RandomPointInLocalizer(rand);
	}

	/**
	 * Build a localizer based on a geographically grounded population
	 *  
	 * @param population
	 */
	public SPUniformLocalizer(SpllPopulation population) {
		this();
		this.population = population;
	}

	// ----------------------------------------------------- //
	// ---------------------- SETTERS ---------------------- //
	// ----------------------------------------------------- //

	@Override
	public void setMatcher(IGSGeofile<? extends AGeoEntity> match, String keyAttPop, String keyAttMatch) {
		if(!match.isCoordinateCompliant(population.getGeography()))
			throw new IllegalArgumentException("The Coordinate Referent System of matcher does not fit population's geography:\n"
					+ "Match = "+match.getWKTCoordinateReferentSystem()+"\n"
					+ "Geography = "+population.getGeography().getWKTCoordinateReferentSystem());
		if(match.getGeoAttributes().stream().noneMatch(att -> att.getAttributeName().equals(keyAttMatch)))
			throw new IllegalArgumentException("The match file does not contain any attribute named "+keyAttMatch
					+ "while this name has been setup to be the key attribute match");
		if(population.getPopulationAttributes().stream().noneMatch(att -> att.getAttributeName().equals(keyAttPop)))
			throw new IllegalArgumentException("The population does not contains any attribute named "+keyAttPop
					+" while this name has been setup to be the key attribute population");
		this.match = match;
		this.keyAttPop = keyAttPop;
		this.keyAttMatch = keyAttMatch;
	}

	@Override
	public void setMapper(IGSGeofile<? extends AGeoEntity> map, String mapAttribute) {
		this.map = map;
		this.keyAttMap = mapAttribute;
	}

	@Override
	public void setMapper(List<IGSGeofile<? extends AGeoEntity>> endogeneousVarFile, 
			List<? extends AGeoValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException, 
	TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException, SchemaException {
		File tmp = File.createTempFile("match", "geo");
		tmp.deleteOnExit();
		this.setMapper(this.getMatcher(tmp), this.keyAttMap, endogeneousVarFile, 
				varList, lmRegressionOLS, splUniformNormalizer);
	}

	@Override
	public void setMapper(IGSGeofile<? extends AGeoEntity> mainMapper, String mainAttribute,
			List<IGSGeofile<? extends AGeoEntity>> ancillaryFileList, 
			List<? extends AGeoValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException, 
	TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException, SchemaException {
		this.setMapper(new SPLAreaMapperBuilder(mainMapper, mainAttribute, 
				ancillaryFileList, varList, lmRegressionOLS, splUniformNormalizer));

	}

	/*
	 * Inner utility set mapper from regression
	 * 
	 */
	private void setMapper(ASPLMapperBuilder<? extends ISPLVariable, ? extends Number> splMapperBuilder) 
			throws IOException, TransformException, InterruptedException, ExecutionException, 
			IllegalRegressionException, IndexOutOfBoundsException, GSMapperException, SchemaException {
		splMapperBuilder.buildMapper();
		File tmpFile = Files.createTempFile("regression_raster_output", "tif").toFile();
		tmpFile.deleteOnExit();
		switch (splMapperBuilder.getAncillaryFiles().get(0).getGeoGSFileType()) {
		case RASTER:
			this.setMapper(splMapperBuilder.buildOutput(tmpFile, 
					(SPLRasterFile) splMapperBuilder.getAncillaryFiles().get(0), false, true, 
					(double) population.size()), GeoEntityFactory.ATTRIBUTE_PIXEL_BAND+0);
			break;
		case VECTOR:
			this.setMapper(splMapperBuilder.buildOutput(tmpFile, 
				(SPLVectorFile) splMapperBuilder.getAncillaryFiles().get(0), false, true, 
				(double) population.size()), GeoEntityFactory.ATTRIBUTE_PIXEL_BAND+0);
			break;
		default:
			throw new IllegalArgumentException("Ancillary could not be resolve to a proper geo file type ("+GeoGSFileType.values()+")");
		}
	}
	
	public void setConstraints(List<SpatialConstraint> constraints) {
		this.constraints = constraints;
	}
	
	public void setPointInLocalizer(PointInLocalizer pointInLocalizer) {
		this.pointInLocalizer = pointInLocalizer;
	}
	
	// ----------------------------------------------------- //
	// ---------------------- GETTERS ---------------------- //
	// ----------------------------------------------------- //


	public List<SpatialConstraint> getConstraints() {
		return constraints;
	}

	public PointInLocalizer getPointInLocalizer() {
		return pointInLocalizer;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * make use of parallelism through {@link Stream#parallel()}
	 * @throws TransformException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws MismatchedDimensionException 
	 * @throws SchemaException 
	 * 
	 */
	@Override
	public IGSGeofile<? extends AGeoEntity> getMatcher(File match) 
			throws MismatchedDimensionException, IllegalArgumentException, IOException, 
			TransformException, SchemaException {
		if(this.match == null)
			throw new NullPointerException("To call for a matcher, you need first to set one while match variable is null");
		
		// Logger to track process
		GSPerformanceUtil gspu = new GSPerformanceUtil("Create a file to store entity-space match (called 'matcher')", LogManager.getLogger());
		gspu.sysoStempPerformance(1, this);
		
		// Collection of entity to match
		Collection<? extends AGeoEntity> entities = this.match.getGeoEntity();
		// Setup key attribute of entity mapped to the number of match
		Map<String, Integer> attMatches = entities.parallelStream()
				.collect(Collectors.toMap(e -> e.getValueForAttribute(keyAttMatch).getInputStringValue(), e -> 0));
		// Test if each entity has it's own key attribute, and if not through an exception
		if(attMatches.size() != entities.size())
				throw new IllegalArgumentException("Define matcher does not fit key attribute contract: some entity has the same key value");
			
		// DOES THE MATCH
		population.stream().map(e -> e.getValueForAttribute(keyAttPop).getInputStringValue())
			.forEach(value -> attMatches.put(value, attMatches.get(value)+1));
		
		gspu.sysoStempMessage("matches count ("+attMatches.size()+") done");
		gspu.sysoStempPerformance(2, this);
		
		// Bind each key attribute with its entity to fasten further processes
		Map<AGeoEntity, Integer> entityMatches = entities.stream()
				.collect(Collectors.toMap(e -> e, e -> attMatches.get(e.getValueForAttribute(keyAttMatch).getInputStringValue())));
		
		// Setup factories
		SPLGeofileFactory gf = new SPLGeofileFactory();
		
		// DOES EXPORT THE MATCH TO THE PROPER FORMAT, EITHER RASTER OR VECTOR
		switch (this.match.getGeoGSFileType()) {
		case RASTER:
			SPLRasterFile rasterMatch = (SPLRasterFile) this.match;
			float[][] pixels = new float[rasterMatch.getColumnNumber()][rasterMatch.getRowNumber()];
			entityMatches.entrySet().parallelStream()
				.forEach(e -> pixels[((GSPixel) e.getKey()).getGridX()][((GSPixel) e.getKey()).getGridY()] = e.getValue());
			return gf.createRasterfile(match, pixels, (float) rasterMatch.getNoDataValue(), 
					new ReferencedEnvelope(rasterMatch.getEnvelope(), SpllUtil.getCRSfromWKT(rasterMatch.getWKTCoordinateReferentSystem())));
		case VECTOR:
			gspu.sysoStempMessage("Start exporting to file");
			SPLVectorFile vectorMatch = (SPLVectorFile) this.match;
			RawGeoAttribute contAtt = new RawGeoAttribute(keyAttMatch);
			// Build a factory able to create proper feature 
			GeoEntityFactory ef = new GeoEntityFactory(Stream.of(contAtt).collect(Collectors.toSet()),
					vectorMatch.getStore().getSchema(vectorMatch.getStore().getTypeNames()[0]));
			gspu.getLogger().debug("Start processing data and create feature with attribute {}", 
					contAtt.getAttributeName());
			// Transpose entity-contingency map into a collection of feature
//			Collection<GSFeature> features = entityMatches.entrySet().parallelStream()
//				.map(e -> ef.createGeoEntity(ef.createContingencyFeature(e.getKey().getGeometry(), 
//						Stream.of(new RawGeoData(contAtt, e.getValue())).collect(Collectors.toSet())), 
//						null))
//				.collect(Collectors.toSet());
			Collection<GSFeature> features = new HashSet<>();
			for(Entry<AGeoEntity, Integer> entry : entityMatches.entrySet()){
				AGeoValue val = new RawGeoData(contAtt, entry.getValue());
				Feature feat = ef.createContingencyFeature(entry.getKey().getGeometry(), 
						Stream.of(val).collect(Collectors.toSet()));
				gspu.getLogger().debug("Create feature with value {} and inner feature {}", 
						val.getInputStringValue(), feat.getIdentifier().getID());
				GSFeature spllFeat = ef.createGeoEntity(feat, null);
				features.add(spllFeat);
			}
			return gf.createShapeFile(match, features);
		default:
			throw new IllegalArgumentException("Geographic file "+match.getClass().getCanonicalName()
					+" does not match any known geographic file type");
		}
	}
	
	///////////////////////////////////////////////////////////
	// ------------------- MAIN CONTRACT ------------------- //
	///////////////////////////////////////////////////////////

	@Override
	public SpllPopulation localisePopulation() {
		try {
			//case where the referenced file is not defined
			if (match == null) {
				List<APopulationEntity> entities = new ArrayList<>(population);

				//case where there is no information about the number of entities in specific spatial areas
				if (keyAttMap == null || map == null) {
					randomLocalizationInNest(entities, null);
				}
				//case where we have information about the number of entities per specific areas (entityNbAreas)
				else {
					randomLocalizationInNestWithNumbers(entities, null);
				}
			}
			//case where the referenced file is defined
			else {
				for (AGeoEntity globalfeature : match.getGeoEntity()) {
					String valKeyAtt = globalfeature.getValueForAttribute(keyAttMatch).getStringValue();
					List<APopulationEntity> entities = population.stream()
							.filter(s -> s.getValueForAttribute(keyAttPop).getStringValue().equals(valKeyAtt))
							.collect(Collectors.toList());
					if (keyAttMap == null || map == null) {
						randomLocalizationInNest(entities, globalfeature.getGeometry());
					}
					else {
						randomLocalizationInNestWithNumbers(entities, globalfeature.getGeometry());
					}
				}
			} 

		} catch (IOException | TransformException e) {
			e.printStackTrace();
		} 
		return population;
	}

	
	/////////////////////////////////////////////////////
	// --------------- INNER UTILITIES --------------- //
	/////////////////////////////////////////////////////
	
	
	//set to all the entities given as argument, a given nest chosen randomly in the possible geoEntities 
	//of the localisation shapefile (all if not bounds is defined, only the one in the bounds if the one is not null)
	private void randomLocalizationInNest(Collection<APopulationEntity> entities, Geometry spatialBounds) throws IOException, TransformException {
		Object[] locTab = spatialBounds == null ? population.getGeography().getGeoEntity().toArray() : population.getGeography().getGeoEntityWithin(spatialBounds).toArray();
		int nb = locTab.length;
		for (APopulationEntity entity : entities) {
			AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
			entity.setNest(nest);
			entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
		}
	}

	// For each area concerned of the entityNbAreas shapefile  (all if not bounds is defined, only the one in the bounds if the one is not null),
	//define the number of entities from the entities list to locate inside, then try to set a nest to this randomly chosen number of entities.
	// NOTE: if no nest is located inside the area, not entities will be located inside.
	private void randomLocalizationInNestWithNumbers(List<APopulationEntity> entities, Geometry spatialBounds) 
			throws IOException, TransformException {
		Collection<? extends AGeoEntity> areas = spatialBounds == null ? 
				map.getGeoEntity() : map.getGeoEntityWithin(spatialBounds);
				for (AGeoEntity feature: areas) {
					Object[] locTab = null;
					if (population.getGeography() == map) {
						locTab = new Object[1];
						locTab[0] = feature;
					} else {
						locTab = population.getGeography().getGeoEntityWithin(feature.getGeometry()).toArray();
					}
					int nb = locTab.length;
					if (nb == 0) continue;
					double val = feature.getValueForAttribute(keyAttMap).getNumericalValue().doubleValue();
					for (int i = 0; i < val; i++) {
						if (entities.isEmpty()) break;
						int index = rand.nextInt(entities.size());
						APopulationEntity entity = entities.remove(index);

						AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
						entity.setNest(nest);
						entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
					}
				}
	}

}
