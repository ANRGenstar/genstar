package spll.popmapper.linker;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.util.data.GSEnumDataType;
import spll.SpllPopulation;
import spll.SpllSetupTest;
import spll.popmapper.SPLocalizer;

public class LinkerTest {

	public static SpllSetupTest sst;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sst = new SpllSetupTest();
	}
	
	@Test
	public void testDefaultLinker() {
		SPLocalizer localizer = new SPLocalizer(new SpllPopulation(sst.pop, sst.sfBuildings));
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		localizer.linkPopulation(sst.sfRoads, GeographicAttributeFactory.getFactory().createAttribute("Driving", GSEnumDataType.Nominal));
		
		assert localizedPop.stream().filter(a -> a.getLocation() != null).count() == 50;
	}
	
}
