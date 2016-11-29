package gospl.generator;

import static org.junit.Assert.*;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.BeforeClass;
import org.junit.Test;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IPopulation;
import gospl.metamodel.GosplEntity;

public class UniformRandomGeneratorTest {

	static UniformRandomGenerator urg;
	
	static int maxAtt = 4, maxVal = 10;
	
	static int popSize = 10000;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		urg = new UniformRandomGenerator(ThreadLocalRandom.current(), maxAtt, maxVal);
	}

	@Test
	public void test() {
		IPopulation<GosplEntity, ASurveyAttribute, AValue> pop = urg.generate(popSize);
		assertEquals(popSize, pop.size());
	}

}
