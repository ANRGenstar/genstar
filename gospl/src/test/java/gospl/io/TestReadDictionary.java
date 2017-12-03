package gospl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.algo.sr.bn.CategoricalBayesianNetwork;

public class TestReadDictionary {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadDictionaryFromBayesianNetwork() {

		CategoricalBayesianNetwork bn = CategoricalBayesianNetwork.loadFromXMLBIF(new File("./src/test/resources/bayesiannetworks/gerland.xbif"));

		Collection<DemographicAttribute<? extends IValue>> attributes = ReadDictionaryUtils.readBayesianNetworkAsDictionary(bn);
		
		System.err.println(attributes);
		
		assertNotNull(attributes);
		
		assertEquals(7, attributes.size());
		
		
	}

}
