package gospl.generator;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.generator.util.GSUtilGenerator;

public class UniformRandomGeneratorTest {

	static GSUtilGenerator urg;
	
	static int maxAtt = 4, maxVal = 10;
	
	static int popSize = 10000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		urg = new GSUtilGenerator(maxAtt, maxVal);
	}

	@Test
	public void test() {
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> pop = urg.generate(popSize);
		assertEquals(popSize, pop.size());
	}

}
