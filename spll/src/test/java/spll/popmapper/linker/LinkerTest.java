package spll.popmapper.linker;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.attribute.AttributeFactory;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import spll.SpllEntity;
import spll.SpllPopulation;
import spll.SpllSetupTest;
import spll.localizer.SPLocalizer;
import spll.localizer.distribution.SpatialDistributionFactory;
import spll.localizer.linker.ISPLinker;
import spll.localizer.linker.SPLinker;

public class LinkerTest {

	public static SpllSetupTest sst;
	
	public static String ATT_NAME = "driving place";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sst = new SpllSetupTest();
	}
	
	@Test
	public void testDefaultLinker() throws GSIllegalRangedData {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		localizer.linkPopulation(localizedPop,
				new SPLinker<SpllEntity>(
						SpatialDistributionFactory.getInstance()
						.getUniformDistribution()),
				sst.sfRoads.getGeoEntity(), 
				AttributeFactory.getFactory().createAttribute(ATT_NAME, GSEnumDataType.Nominal));
		
		assert localizedPop.stream().allMatch(a -> a.getLinkedPlaces().containsKey(ATT_NAME));
	}
	
	@Test
	public void testComplexLinker() throws GSIllegalRangedData {
		SPLocalizer localizer = new SPLocalizer(sst.pop, sst.sfBuildings);
		SpllPopulation localizedPop = localizer.localisePopulation();
				
		ISPLinker<SpllEntity> linker = new SPLinker<>(SpatialDistributionFactory.getInstance()
				.getGravityModelDistribution(sst.sfRoads.getGeoEntity(), 1.0,
						localizedPop.toArray(new SpllEntity[localizedPop.size()])));
		
		Collection<? extends AGeoEntity<? extends IValue>> candidates = sst.sfRoads.getGeoEntity();
		
		localizer.linkPopulation(localizedPop, linker, candidates, 
				AttributeFactory.getFactory().createAttribute("Driving", GSEnumDataType.Nominal));
		
		assert localizedPop.stream().allMatch(a -> a.getLinkedPlaces().containsKey(ATT_NAME));
	}
	
}
