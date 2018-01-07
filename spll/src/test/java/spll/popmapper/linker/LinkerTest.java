package spll.popmapper.linker;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import spll.SpllEntity;
import spll.SpllPopulation;
import spll.SpllSetupTest;
import spll.popmapper.SPLocalizer;
import spll.popmapper.distribution.SpatialDistributionFactory;

public class LinkerTest {

	public static SpllSetupTest sst;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sst = new SpllSetupTest();
	}
	
	@Test
	public void testDefaultLinker() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		localizer.linkPopulation(localizedPop,
				new SPLinker<SpllEntity>(
						SpatialDistributionFactory.getInstance()
						.getUniformDistribution()),
				sst.sfRoads.getGeoEntity(), 
				GeographicAttributeFactory.getFactory().createAttribute("Driving", GSEnumDataType.Nominal));
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
	@Test
	public void testComplexLinker() {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpllPopulation localizedPop = localizer.localisePopulation();
				
		ISPLinker<SpllEntity> linker = new SPLinker<>(SpatialDistributionFactory.getInstance()
				.getGravityModelDistribution(sst.sfRoads.getGeoEntity(), 
						localizedPop.toArray(new SpllEntity[localizedPop.size()])));
		
		Collection<? extends AGeoEntity<? extends IValue>> candidates = sst.sfRoads.getGeoEntity();
		
		localizer.linkPopulation(localizedPop, linker, candidates, 
				GeographicAttributeFactory.getFactory().createAttribute("Driving", GSEnumDataType.Nominal));
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
}
