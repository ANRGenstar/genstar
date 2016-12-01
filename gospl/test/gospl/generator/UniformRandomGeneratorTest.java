package gospl.generator;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;

public class UniformRandomGeneratorTest {

	static UniformRandomGenerator urg;
	
	static int maxAtt = 4, maxVal = 10;
	
	static int popSize = 10000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		urg = new UniformRandomGenerator(maxAtt, maxVal);
	}

	@Test
	public void test() {
		IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> pop = urg.generate(popSize);
		assertEquals(popSize, pop.size());
	}

}
