package spll.popmapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.geotools.feature.SchemaException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.operation.TransformException;

import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.generator.util.GSUtilGenerator;
import spll.SpllPopulation;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.io.SPLGeofileBuilder;
import spll.io.SPLRasterFile;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;
import spll.popmapper.constraint.SpatialConstraintMaxNumber;
import spll.popmapper.normalizer.SPLUniformNormalizer;

public class LocalizerTest {

	public static IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> pop;
	public static SPLVectorFile sfAdmin;
	public static SPLVectorFile sfBuildings;
	public static SPLVectorFile sfRoads;
	public static List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> endogeneousVarFile;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setupRandom();
	}

	
	@Test
	public void testSimpleLocalisation() {
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfBuildings));
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	@Test
	public void testMaxNumber() {
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfBuildings));
		SpatialConstraintMaxNumber numberConstr = new SpatialConstraintMaxNumber(sfBuildings.getGeoEntity(), 1.0);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(2);
		numberConstr.setMaxIncrease(60);
		localizer.addConstraint(numberConstr);
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	
	
	@Test
	public void testCloseRoadsMaxDistance() {
		((SPLVectorFile) sfRoads).minMaxDistance(0.0,5.0, false);
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfRoads));
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	@Test
	public void testCloseRoadsBetween() {
		((SPLVectorFile) sfRoads).minMaxDistance(2.0, 5.0, false);
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfRoads));
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	@Test
	public void testCloseRoadsBetweenNonOverlapping() {
		((SPLVectorFile) sfRoads).minMaxDistance(1.0, 5.0, true);
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfRoads));
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	
	@Test
	public void testMatcher() {
		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfBuildings));
		
		localizer.setMatcher(sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(10.0);
		localizer.getLocalizationConstraint().setMaxIncrease(10.0); 
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	@Test
	public void testMatcherMapperRegression() {

		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfBuildings));
		
		localizer.setMatcher(sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(100.0);
		localizer.getLocalizationConstraint().setMaxIncrease(100.0); 
		
		try {
			localizer.setMapper(endogeneousVarFile, new ArrayList<>(), 
					new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
		} catch (IndexOutOfBoundsException | IOException | TransformException | InterruptedException
				| ExecutionException | IllegalRegressionException | GSMapperException | SchemaException 
				| IllegalArgumentException | InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	@Test
	public void testMatcherMapperRegressionNumber() {

		SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(pop, sfBuildings));
		
		localizer.setMatcher(sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(100.0);
		localizer.getLocalizationConstraint().setMaxIncrease(100.0); 
		
		SpatialConstraintMaxNumber numberConstr = new SpatialConstraintMaxNumber(sfBuildings.getGeoEntity(), 1.0);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(2);
		numberConstr.setMaxIncrease(60);
		localizer.addConstraint(numberConstr);
		
		try {
			localizer.setMapper(endogeneousVarFile, new ArrayList<>(), 
					new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
		} catch (IndexOutOfBoundsException | IOException | TransformException | InterruptedException
				| ExecutionException | IllegalRegressionException | GSMapperException | SchemaException 
				| IllegalArgumentException | InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() > 0;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private static void setupRandom(){
		DemographicDictionary<DemographicAttribute<? extends IValue>> atts = new DemographicDictionary<>();
		try {
			atts.addAttributes(DemographicAttributeFactory.getFactory()
					.createAttribute("iris", GSEnumDataType.Nominal, Arrays.asList("765400102", "765400101")));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
		}
		
		GSUtilGenerator ug = new GSUtilGenerator(atts);
				
		pop = ug.generate(50);
		
		try {
			sfBuildings = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/buildings.shp"), Arrays.asList("name", "type"), null);
			sfAdmin = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/irisR.shp"), null);
			sfRoads = SPLGeofileBuilder.getShapeFile(new File("src/test/resources/roads.shp"), null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		Collection<String> stringPathToAncilaryGeofiles = new ArrayList<>();
		stringPathToAncilaryGeofiles.add("src/test/resources/CLC12_D076_RGF_S.tif");
		endogeneousVarFile = new ArrayList<>();
		for(String path : stringPathToAncilaryGeofiles){
			try {
				endogeneousVarFile.add(new SPLGeofileBuilder().setFile(new File(path)).buildGeofile());
			} catch (IllegalArgumentException | TransformException | IOException | InvalidGeoFormatException e2) {
				e2.printStackTrace();
			}
		}
	}
	
}
