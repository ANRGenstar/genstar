package spll.popmapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.geotools.feature.SchemaException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.operation.TransformException;

import spll.SpllPopulation;
import spll.SpllSetupTest;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.normalizer.SPLUniformNormalizer;
import spll.io.SPLRasterFile;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;
import spll.localizer.SPLocalizer;
import spll.localizer.constraint.SpatialConstraintMaxDensity;
import spll.localizer.constraint.SpatialConstraintMaxNumber;

public class LocalizerTest {
	
	public static SpllSetupTest sst;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sst = new SpllSetupTest();
	}

	
	@Test
	public void testSimpleLocalisation() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testMaxNumber() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpatialConstraintMaxNumber numberConstr = new SpatialConstraintMaxNumber(sst.sfBuildings.getGeoEntity(), 3.0);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(2);
		numberConstr.setMaxIncrease(10);
		localizer.addConstraint(numberConstr);
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testMaxDensity() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpatialConstraintMaxDensity numberConstr = new SpatialConstraintMaxDensity(sst.sfBuildings.getGeoEntity(), 0.5);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(0.1);
		numberConstr.setMaxIncrease(1.0);
		localizer.addConstraint(numberConstr);
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	
	@Test
	public void testCloseRoadsMaxDistance() {
		((SPLVectorFile) sst.sfRoads).minMaxDistance(0.0,5.0, false);
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfRoads);
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testCloseRoadsBetween() {
		((SPLVectorFile) sst.sfRoads).minMaxDistance(2.0, 5.0, false);
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfRoads);
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testCloseRoadsBetweenNonOverlapping() {
		((SPLVectorFile) sst.sfRoads).minMaxDistance(1.0, 5.0, true);
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfRoads);
		SpllPopulation localizedPop = localizer.localisePopulation();
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	
	@Test
	public void testMatcher() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		
		localizer.setMatcher(sst.sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(10.0);
		localizer.getLocalizationConstraint().setMaxIncrease(10.0); 
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testMatcherMapperRegression() {

		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		
		localizer.setMatcher(sst.sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(100.0);
		localizer.getLocalizationConstraint().setMaxIncrease(100.0); 
		
		try {
			localizer.setMapper(sst.endogeneousVarFile, new ArrayList<>(), 
					new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
		} catch (IndexOutOfBoundsException | IOException | TransformException | InterruptedException
				| ExecutionException | IllegalRegressionException | GSMapperException | SchemaException 
				| IllegalArgumentException | InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testMatcherMapperRegressionNumber() {

		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		
		localizer.setMatcher(sst.sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(100.0);
		localizer.getLocalizationConstraint().setMaxIncrease(100.0); 
		
		SpatialConstraintMaxNumber numberConstr = new SpatialConstraintMaxNumber(sst.sfBuildings.getGeoEntity(), 1.0);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(2);
		numberConstr.setMaxIncrease(60);
		localizer.addConstraint(numberConstr);
		
		try {
			localizer.setMapper(sst.endogeneousVarFile, new ArrayList<>(), 
					new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
		} catch (IndexOutOfBoundsException | IOException | TransformException | InterruptedException
				| ExecutionException | IllegalRegressionException | GSMapperException | SchemaException 
				| IllegalArgumentException | InvalidGeoFormatException e) {
			e.printStackTrace();
		}
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testConstraintRelax() {
		SPLocalizer localizer = new SPLocalizer(sst.popBig, sst.sfBuildings);
		localizer.setMatcher(sst.sfAdmin, "iris", "CODE_IRIS");
		localizer.getLocalizationConstraint().setIncreaseStep(10.0);
		localizer.getLocalizationConstraint().setMaxIncrease(30.0); 
		
		SpatialConstraintMaxNumber numberConstr = new SpatialConstraintMaxNumber(sst.sfBuildings.getGeoEntity(), 1.0);
		numberConstr.setPriority(10);
		numberConstr.setIncreaseStep(1);
		numberConstr.setMaxIncrease(3);
		localizer.addConstraint(numberConstr);
		
		SpllPopulation localizedPop = localizer.localisePopulation();
		long nbLocalized = localizedPop.stream().filter(a -> a.getLocation() != null).count() ;
		assert ((nbLocalized > 0) && (nbLocalized < 5000));
	}
	
}
