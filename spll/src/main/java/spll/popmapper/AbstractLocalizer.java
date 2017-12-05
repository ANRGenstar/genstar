package spll.popmapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.io.IGSGeofile.GeoGSFileType;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandom;
import spll.SpllEntity;
import spll.SpllPopulation;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.variable.ISPLVariable;
import spll.entity.GeoEntityFactory;
import spll.entity.SpllFeature;
import spll.entity.SpllPixel;
import spll.io.SPLGeofileBuilder;
import spll.io.SPLGeofileBuilder.SPLGisFileExtension;
import spll.io.SPLRasterFile;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;
import spll.popmapper.constraint.ISpatialConstraint;
import spll.popmapper.constraint.SpatialConstraintLocalization;
import spll.popmapper.normalizer.SPLUniformNormalizer;
import spll.popmapper.pointInalgo.PointInLocalizer;
import spll.popmapper.pointInalgo.RandomPointInLocalizer;
import spll.util.SpllUtil;


public abstract class AbstractLocalizer implements ISPLocalizer {

	/*
	 * Performance purpose logger
	 */
	protected GSPerformanceUtil gspu;

	protected SpllPopulation population;

	//main referenced area for placing the agents (e.g. Iris)
	protected IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> match;
	//gives the number of entities per area (e.g. regression cells)
	protected IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> map; 
	
	protected List<ISpatialConstraint> constraints; //spatial constraints related to the placement of the entities in their nest 
	protected SpatialConstraintLocalization localizationConstraint; //the localization constraint;
	protected PointInLocalizer pointInLocalizer; //allows to return one or several points in a geometry

	protected String keyAttMap; //name of the attribute that contains the number of entities in the map file
	protected String keyAttPop; //name of the attribute that is used to store the id of the referenced area in the population
	protected String keyAttMatch; //name of the attribute that is used to store the id of the referenced area in the match file

	protected Random rand;

	/**
	 * Private constructor to setup random engine
	 */
	private AbstractLocalizer() {
		rand = GenstarRandom.getInstance();
		pointInLocalizer = new RandomPointInLocalizer(rand);
	}

	/**
	 * Build a localizer based on a geographically grounded population
	 *  
	 * @param population
	 */
	public AbstractLocalizer(SpllPopulation population) {
		this();
		this.population = population;
		localizationConstraint = new SpatialConstraintLocalization(null);
		localizationConstraint.setReferenceFile(population.getGeography());
		constraints = new ArrayList<>();
		constraints.add(localizationConstraint);
	}

	// ----------------------------------------------------- //
	// ---------------------- MATHCER ---------------------- //
	// ----------------------------------------------------- //

	@Override
	public void setMatcher(IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> match, 
			String keyAttPop, String keyAttMatch) {
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
	public IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> estimateMatcher(File destination) 
			throws MismatchedDimensionException, IllegalArgumentException, IOException, 
			TransformException, SchemaException { 
		if(this.match == null)
			throw new NullPointerException("To call for a matcher, you need first to set one while match variable is null");

		// Logger to track process
		gspu = new GSPerformanceUtil("Create a file to store entity-space match (called 'matcher')", LogManager.getLogger());
		Map<? extends AGeoEntity<? extends IValue>, Number> transfer = this.estimateMatches(this.match, this.keyAttMatch, this.keyAttPop);
		final GeographicAttribute<? extends IValue> transferAttribute = GeographicAttributeFactory.getFactory()
				.createIntegerAttribute("count");
		return this.match.transferTo(destination, transfer, transferAttribute);
	}

	// ----------------------------------------------------- //
	// ----------------------- MAPPER ---------------------- //
	// ----------------------------------------------------- //

	public IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> getMapperOutput(){
		return map;
	} 
	
	@Override
	public void setMapper(IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> map, String mapAttribute) {
		this.map = map;
		this.keyAttMap = mapAttribute;
	}

	@Override
	public void setMapper(List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> ancillaryFileList, 
			List<? extends IValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException, 
	TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException, SchemaException, 
	MismatchedDimensionException, IllegalArgumentException, InvalidGeoFormatException {
		String keyAttribute = "count" ;
		File tmp = File.createTempFile("match", "."+ (match.getGeoGSFileType().equals(GeoGSFileType.VECTOR) ? 
				SPLGisFileExtension.shp.toString() : SPLGisFileExtension.tif.toString()));
		tmp.deleteOnExit();
		
		this.setMapper(this.estimateMatcher(tmp), keyAttribute, ancillaryFileList, 
				varList, lmRegressionOLS, splUniformNormalizer);
		
	}

	@Override
	public void setMapper(IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> mainMapper, String mainAttribute,
			List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> ancillaryFileList, 
			List<? extends IValue> varList, LMRegressionOLS lmRegressionOLS, 
			SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException, 
	TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException, 
	SchemaException, MismatchedDimensionException, IllegalArgumentException, InvalidGeoFormatException {
		this.setMapper(new SPLAreaMapperBuilder(mainMapper, mainAttribute, 
				ancillaryFileList, varList, lmRegressionOLS, splUniformNormalizer));

	}

	/*
	 * Inner utility set mapper from regression
	 * 
	 */
	private void setMapper(ASPLMapperBuilder<? extends ISPLVariable, ? extends Number> splMapperBuilder) 
			throws IOException, TransformException, InterruptedException, ExecutionException, 
			IllegalRegressionException, IndexOutOfBoundsException, GSMapperException, SchemaException, 
			MismatchedDimensionException, IllegalArgumentException, InvalidGeoFormatException {
		splMapperBuilder.buildMapper();
		switch (splMapperBuilder.getAncillaryFiles().get(0).getGeoGSFileType()) {
		case RASTER:
			File tmpRaster = Files.createTempFile("regression_raster_output", ".tif").toFile();
			tmpRaster.deleteOnExit();
			this.setMapper(splMapperBuilder.buildOutput(tmpRaster, 
					(SPLRasterFile) splMapperBuilder.getAncillaryFiles().get(0), false, true, 
					(double) population.size()), GeoEntityFactory.ATTRIBUTE_PIXEL_BAND+0);
			break;
		case VECTOR:
			File tmpVector = Files.createTempFile("regression_vector_output", ".shp").toFile();
			tmpVector.deleteOnExit();
			this.setMapper(splMapperBuilder.buildOutput(tmpVector, 
					(SPLRasterFile) splMapperBuilder.getAncillaryFiles().get(0), false, true, 
					(double) population.size()), splMapperBuilder.getMainAttribute());
			break;
		default:
			throw new IllegalArgumentException("Ancillary could not be resolve to a proper geo file type ("+GeoGSFileType.values()+")");
		}
	}

	// ----------------------------------------------------- //
	// -------------------- CONSTRAINTS -------------------- //
	// ----------------------------------------------------- //

	@Override
	public void setConstraints(List<ISpatialConstraint> constraints) {
		this.constraints = constraints;
	}
	
	@Override
	public boolean addConstraint(ISpatialConstraint constraint){
		return this.constraints.add(constraint);
	}
	
	@Override
	public List<ISpatialConstraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	// ----------------------------------------------------- //
	// ------------------ POINT LOCALIZER ------------------ //
	// ----------------------------------------------------- //

	public void setPointInLocalizer(PointInLocalizer pointInLocalizer) {
		this.pointInLocalizer = pointInLocalizer;
	}

	public PointInLocalizer getPointInLocalizer() {
		return pointInLocalizer;
	}

	///////////////////////////////////////////////////////////
	// ------------------- MAIN CONTRACT ------------------- //
	///////////////////////////////////////////////////////////

	@Override
	public SpllPopulation localisePopulation() {
		constraints = constraints.stream().sorted((n1, n2) -> Integer.compare( n1.getPriority(), n2.getPriority())).collect(Collectors.toList());
		try {
			//case where the referenced file is not defined
			if (match == null) {
				List<SpllEntity> entities = new ArrayList<>(population);

				//case where there is no information about the number of entities in specific spatial areas
				if (keyAttMap == null || map == null) {
					localizationInNest(entities, null);
				}
				//case where we have information about the number of entities per specific areas (entityNbAreas)
				else {
					localizationInNestWithNumbers(entities, null);
				}
			}
			//case where the referenced file is defined
			else {
				for (AGeoEntity<? extends IValue> globalfeature : match.getGeoEntity()) {
					String valKeyAtt = globalfeature.getValueForAttribute(keyAttMatch).getStringValue();
					
					
					List<SpllEntity> entities = population.stream()
							.filter(s -> s.getValueForAttribute(keyAttPop).getStringValue().equals(valKeyAtt))
							.collect(Collectors.toList());
					if (keyAttMap == null || map == null) {
						localizationInNest(entities, globalfeature.getProxyGeometry());
					}
					else {
						localizationInNestWithNumbers(entities, globalfeature.getProxyGeometry());
					}
				}
			}
			population.removeIf(a -> a.getLocation() == null); 
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
	protected void localizationInNest(Collection<SpllEntity> entities, Geometry spatialBounds) throws IOException, TransformException {
		List<ISpatialConstraint> otherConstraints = new ArrayList<>(constraints);
		otherConstraints.remove(localizationConstraint);
		Collection<SpllEntity> remainingEntities = entities;
		localizationConstraint.setBounds(spatialBounds);
		for (ISpatialConstraint cr : constraints) {
			while (!cr.isConstraintLimitReach()) {
				List<AGeoEntity<? extends IValue>> possibleNests = 
						new ArrayList<>(population.getGeography().getGeoEntity());
				List<AGeoEntity<? extends IValue>> possibleNestsInit = 
						localizationConstraint.getSortedCandidates(null);
				possibleNests = new ArrayList<>(possibleNests);
				for (ISpatialConstraint constraint : otherConstraints) {
					possibleNests = constraint.getSortedCandidates(possibleNests);
				}
				
				remainingEntities = localizationInNestOp(remainingEntities, possibleNests, null);
				if (remainingEntities != null && !remainingEntities.isEmpty()) 
					 cr.relaxConstraint(possibleNestsInit);
				else return;
					
			}
		}
	}
	
	protected abstract List<SpllEntity> localizationInNestOp(Collection<SpllEntity> entities, 
			List<AGeoEntity<? extends IValue>> possibleNests, Long val);
		
	// For each area concerned of the entityNbAreas shapefile  (all if not bounds is defined, only the one in the bounds if the one is not null),
	//define the number of entities from the entities list to locate inside, then try to set a nest to this randomly chosen number of entities.
	// NOTE: if no nest is located inside the area, not entities will be located inside.
	@SuppressWarnings("unchecked")
	private void localizationInNestWithNumbers(List<SpllEntity> entities, Geometry spatialBounds) 
			throws IOException, TransformException {
		List<ISpatialConstraint> otherConstraints = new ArrayList<>(constraints);
		otherConstraints.remove(localizationConstraint);
		
		Collection<? extends AGeoEntity<? extends IValue>> areas = spatialBounds == null ? 
		map.getGeoEntity() : map.getGeoEntityWithin(spatialBounds);
		Map<String,Double> vals = map.getGeoEntity().stream()
				.collect(Collectors.toMap(AGeoEntity::getGenstarName, e -> e.getNumericValueForAttribute(keyAttMap).doubleValue()));
				
		if (map.getGeoGSFileType().equals(GeoGSFileType.RASTER)) {
			double unknowVal = ((SPLRasterFile) map).getNoDataValue();
			List<String> es = new ArrayList<>(vals.keySet());
			for (String e : es) {
				if (vals.get(e).doubleValue() == unknowVal) {
					vals.remove(e);
				}
			}
		}
		Double tot = vals.values().stream().mapToDouble(s -> s).sum();
		if (tot == 0) return;
		Collection<SpllEntity> remainingEntities = entities;
		for (AGeoEntity<? extends IValue> feature: areas) {
			if (map.getGeoGSFileType().equals(GeoGSFileType.RASTER))  {
				if (!vals.containsKey(feature.getGenstarName())) continue;
			}
			localizationConstraint.setBounds(feature.getProxyGeometry());
			long val = Math.round(population.size() *vals.get(feature.getGenstarName()) / tot);
			if (entities.isEmpty()) break;
			for (ISpatialConstraint cr : constraints) {
				while (!remainingEntities.isEmpty() && !cr.isConstraintLimitReach()) {
					List<AGeoEntity<? extends IValue>> possibleNestsInit = localizationConstraint.getSortedCandidates(null);
					List<AGeoEntity<? extends IValue>>  possibleNests = new ArrayList<>(possibleNestsInit);
					for (ISpatialConstraint constraint : otherConstraints) {
						possibleNests = constraint.getSortedCandidates(possibleNests);
					}
					remainingEntities = localizationInNestOp(remainingEntities, possibleNests, val);
					if (!remainingEntities.isEmpty()) {
						cr.relaxConstraint((Collection<AGeoEntity<? extends IValue>>) population.getGeography().getGeoEntity());
					}
				}
				if (remainingEntities == null || remainingEntities.isEmpty()) break;
			}
		}
	}
	
	// ----------------------------- MOVE PART OF THESE METHOD INTO FACTORY / BUILDER
	
	/*
	 * Estimate the number of match between population and space through the key attribute link
	 */
	protected Map<? extends AGeoEntity<? extends IValue>, Number> estimateMatches(
			IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> matchFile, 
			String keyAttributeSpace, String keyAttributePopulation) 
					throws IOException {
		// Collection of entity to match
		Collection<? extends AGeoEntity<? extends IValue>> entities = matchFile.getGeoEntity();

		// Setup key attribute of entity mapped to the number of match
		Map<String, Integer> attMatches = entities.stream()
				.collect(Collectors.toMap(e -> e.getValueForAttribute(keyAttributeSpace).getStringValue(), e -> 0));

		// Test if each entity has it's own key attribute, and if not through an exception
		if(attMatches.size() != entities.size())
			throw new IllegalArgumentException("Define matcher does not fit key attribute contract: some entity has the same key value");

		// DOES THE MATCH
		population.stream().map(e -> e.getValueForAttribute(keyAttributePopulation)).filter(e -> attMatches.containsKey(e)).forEach(value -> attMatches.put(value.getStringValue(), attMatches.get(value.getStringValue())+1));
		
		this.gspu.sysoStempPerformance("Matches ("+ attMatches.size() +") have been counted (Total = "
				+attMatches.values().stream().reduce(0, (i1, i2) -> i1 + i2).intValue()+") !", this);

		// Bind each key attribute with its entity to fasten further processes
		return entities.stream().collect(Collectors.toMap(e -> e, 
				e -> attMatches.get(e.getValueForAttribute(keyAttributeSpace).getStringValue())));
	}

	/*
	 * Create a raster match file from a number of matches (eMatches) and a key attribute: parameter file for areal interpolation
	 */
	protected SPLRasterFile createMatchFile(File output, SPLRasterFile template, 
			Map<AGeoEntity<? extends IValue>, Number> eMatches) 
					throws MismatchedDimensionException, IllegalArgumentException, 
					IOException, TransformException, SchemaException, InvalidGeoFormatException {
		float[][] pixels = new float[template.getColumnNumber()][template.getRowNumber()];
		eMatches.entrySet().stream().forEach(e -> 
			pixels[((SpllPixel) e.getKey()).getGridX()][((SpllPixel) e.getKey()).getGridY()] = e.getValue().floatValue());

		this.gspu.sysoStempPerformance("Matches have been stored in a raster file ("
				+ pixels[0].length * pixels.length +" pixels) !", this);

		return new SPLGeofileBuilder().setFile(output).setRasterBands(pixels).setNoData(template.getNoDataValue())
				.setReferenceEnvelope(new ReferencedEnvelope(template.getEnvelope(), SpllUtil.getCRSfromWKT(template.getWKTCoordinateReferentSystem())))
				.buildRasterfile();
	}

	/*
	 * Create a vector file from a number of matches and a key attribute: parameter file for areal interpolation
	 */
	protected SPLVectorFile createMatchFile(File output, SPLVectorFile matchFile,
			Map<AGeoEntity<? extends IValue>, Number> eMatches, String keyAttMatch) 
					throws IOException, SchemaException, InvalidGeoFormatException {
		Optional<GeographicAttribute<? extends IValue>> keyAtt = matchFile.getGeoAttributes().stream()
				.filter(att -> att.getAttributeName().equals(keyAttMatch)).findFirst();
		if(!keyAtt.isPresent())
			throw new IllegalArgumentException("key attribute matcher "
					+keyAttMatch+ " does not exist in proposed matched file");
		if(!eMatches.keySet().stream().allMatch(entity -> entity.getPropertiesAttribute().contains(keyAttMatch))
				|| !eMatches.keySet().stream().allMatch(entity -> entity.getValueForAttribute(keyAtt.get().getAttributeName()) != null))
			throw new IllegalArgumentException("Matches entity must contain attribute "+keyAttMatch);

		GeographicAttribute<? extends IValue> key = keyAtt.get();
		GeographicAttribute<IntegerValue> contAtt = GeographicAttributeFactory.getFactory()
				.createIntegerAttribute(GeoEntityFactory.ATTRIBUTE_FEATURE_POP);

		// Transpose entity-contingency map into a collection of feature
		Collection<SpllFeature> features = constructFeatureCollection(eMatches, contAtt, key, 
				matchFile.getStore().getSchema(matchFile.getStore().getTypeNames()[0]));

		this.gspu.sysoStempPerformance("Matches have been stored in a vector file ("
				+features.size()+ " features) !", this);
		Set<SpllFeature> categoricalValue = features.stream()
				.filter(feat -> !feat.getValueForAttribute(GeoEntityFactory.ATTRIBUTE_FEATURE_POP).getType().isNumericValue())
				.collect(Collectors.toSet());
		if(!categoricalValue.isEmpty())
			throw new RuntimeException(categoricalValue.size()+" created feature are not numerical: "
					+ categoricalValue.stream().map(gsf -> gsf.getValueForAttribute(key).getStringValue())
					.collect(Collectors.joining("; ")));
		this.gspu.sysoStempPerformance("Total population count is "+features.stream().mapToDouble(feat -> 
		feat.getNumericValueForAttribute(GeoEntityFactory.ATTRIBUTE_FEATURE_POP).intValue()).sum(), this);

		return new SPLGeofileBuilder().setFile(output).setFeatures(features).buildShapeFile();
	}

	/*
	 * Create a set of GSFeature
	 */
	protected Collection<SpllFeature> constructFeatureCollection(Map<AGeoEntity<? extends IValue>, Number> eMatches, 
			GeographicAttribute<IntegerValue> contAtt, GeographicAttribute<? extends IValue> keyAtt, SimpleFeatureType featType){
		GeoEntityFactory ef = new GeoEntityFactory(Stream.of(contAtt, keyAtt).collect(Collectors.toSet()), 
				featType);
		Collection<SpllFeature> features = new HashSet<>();
		for(AGeoEntity<? extends IValue> entity : eMatches.keySet()) {
			Map<GeographicAttribute<? extends IValue>, IValue> theMap = new HashMap<>();
			theMap.put(contAtt, contAtt.getValueSpace().addValue(eMatches.get(entity).toString()));
			theMap.put(keyAtt, entity.getValueForAttribute(keyAtt.getAttributeName()));
			features.add(ef.createGeoEntity(entity.getProxyGeometry(), theMap));
		}
		return features;
	}

	
	public SpatialConstraintLocalization getLocalizationConstraint() {
		return localizationConstraint;
	}

	public void clearMapCache(){
		if (map != null && map instanceof SPLRasterFile) 
			((SPLRasterFile) map).clearCache();
	}
	
	
}