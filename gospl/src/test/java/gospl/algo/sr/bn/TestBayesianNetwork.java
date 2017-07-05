package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestBayesianNetwork {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	   
	@Test
	public void testDefineNodeCategorical() {
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		
		assertEquals(0, nGender.getDomainSize());
		assertEquals(0, nGender.getCardinality());
		
		nGender.addDomain("male");
		
		assertEquals(1, nGender.getDomainSize());
		assertEquals(1, nGender.getCardinality());
		
		nGender.addDomain("female");
		
		assertEquals(2, nGender.getDomainSize());
		assertEquals(2, nGender.getCardinality());
		
		//fail("Not yet implemented");
	}
	
	/*
	@Test
	public void testIndices() {
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		
		NodeCategorical nAge = new NodeCategorical("age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		
		assertEquals(4, nAge.getCardinality());
		
		assertEquals(0, nGender._getIndex("male"));
		assertEquals(1, nGender._getIndex("female"));

		try {
			nAge._getIndex("<15");
			nAge._getIndex(">+15");
			fail("invalid argument exception expected");
		} catch (IllegalArgumentException e) {
		}
		
		assertEquals(0, nAge._getIndex("<15", "gender", "male"));
		assertEquals(2, nAge._getIndex("<15", "gender", "female"));
		assertEquals(1, nAge._getIndex(">=15", "gender", "male"));
		assertEquals(3, nAge._getIndex(">=15", "gender", "female"));

		
	}
	*/


	@Test
	public void testStorageProbasNoParent() {
		
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getProbability("female"), 1e-5);
		
		assertEquals(0.55, nGender.getConditionalProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbability("female"), 1e-5);

		assertTrue("CPT should be valid", nGender.isValid());
		
	}
	


	@Test
	public void testStorageProbasOneParent() {
		
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getProbability("female"), 1e-5);
		
		assertEquals(0.55, nGender.getConditionalProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbability("female"), 1e-5);

		assertTrue("CPT should be valid", nGender.isValid());
		
		NodeCategorical nAge = new NodeCategorical(null, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		
		try {
			nAge.setProbabilities(0.55, "<15");
			fail("invalid argument exception expected");
		} catch (IllegalArgumentException e) {
		}

		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		assertEquals(0.50, nAge.getProbability("<15","gender","female"), 1e-5);
		assertEquals(0.55, nAge.getProbability("<15","gender","male"), 1e-5);

		assertTrue("CPT should be valid", nAge.isValid());		
	}
	

	@Test
	public void testStorageProbasTwoParents() {
		
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getProbability("female"), 1e-5);
		
		assertEquals(0.55, nGender.getConditionalProbability("male"), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbability("female"), 1e-5);

		assertTrue("CPT should be valid", nGender.isValid());
		
		NodeCategorical nAge = new NodeCategorical(null, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
	
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		assertEquals(0.50, nAge.getProbability("<15","gender","female"), 1e-5);
		assertEquals(0.55, nAge.getProbability("<15","gender","male"), 1e-5);

		assertTrue("CPT should be valid", nAge.isValid());		
		

		NodeCategorical nActif = new NodeCategorical(null, "actif");
		nActif.addParent(nAge);
		nActif.addParent(nGender);
		nActif.addDomain("oui", "non");
	
		nActif.setProbabilities(0.0, "oui", "gender", "male", "age", "<15");
		nActif.setProbabilities(1.0, "non", "gender", "male", "age", "<15");
		nActif.setProbabilities(0.05, "oui", "gender", "female", "age", "<15");
		nActif.setProbabilities(0.95, "non", "gender", "female", "age", "<15");
		
		nActif.setProbabilities(0.8, "oui", "gender", "male", "age", ">=15");
		nActif.setProbabilities(0.2, "non", "gender", "male", "age", ">=15");
		nActif.setProbabilities(0.9, "oui", "gender", "female", "age", ">=15");
		nActif.setProbabilities(0.1, "non", "gender", "female", "age", ">=15");
		
		assertEquals(0.0, nActif.getProbability("oui","gender","male", "age", "<15"), 1e-5);
		assertEquals(1.0, nActif.getProbability("non","gender","male", "age", "<15"), 1e-5);

		assertEquals(0.05, nActif.getProbability("oui","gender","female", "age", "<15"), 1e-5);
		assertEquals(0.95, nActif.getProbability("non","gender","female", "age", "<15"), 1e-5);

		assertEquals(0.8, nActif.getProbability("oui","gender","male", "age", ">=15"), 1e-5);
		assertEquals(0.2, nActif.getProbability("non","gender","male", "age", ">=15"), 1e-5);
		
		assertEquals(0.9, nActif.getProbability("oui","gender","female", "age", ">=15"), 1e-5);
		assertEquals(0.1, nActif.getProbability("non","gender","female", "age", ">=15"), 1e-5);

		assertTrue("CPT should be valid", nActif.isValid());		
	}
	

	@Test
	public void testCanComputePosteriors() {
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		assertTrue(nGender.isValid());
		
		NodeCategorical nAge = new NodeCategorical(null, "age");
		nAge.addParent(nGender);
		nAge.addDomain("A", "B", "C");
		nAge.setProbabilities(1./3, "A", "gender", "male");
		nAge.setProbabilities(1./3, "B", "gender", "male");
		nAge.setProbabilities(1./3, "C", "gender", "male");
		nAge.setProbabilities(1./3, "A", "gender", "female");
		nAge.setProbabilities(1./3, "B", "gender", "female");
		nAge.setProbabilities(1./3, "C", "gender", "female");

		assertTrue("the test node is not valid", nAge.isValid());
		
		nGender.getConditionalProbabilityPosterior("male");
		nGender.getConditionalProbabilityPosterior("female");
		nAge.getConditionalProbabilityPosterior("A");
		nAge.getConditionalProbabilityPosterior("B");
		nAge.getConditionalProbabilityPosterior("C");
		
	}
	

	@Test
	public void testProbas() {
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		NodeCategorical nAge = new NodeCategorical(null, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		assertEquals(0.55, nGender.getConditionalProbabilityPosterior("male"), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbabilityPosterior("female"), 1e-5);

		
		assertEquals(0.5275, nAge.getConditionalProbabilityPosterior("<15"), 1e-5);
		assertEquals(0.4725, nAge.getConditionalProbabilityPosterior(">=15"), 1e-5);

	}

	@Test
	public void testParentDimensionality() {
		
		NodeCategorical nGender = new NodeCategorical(null, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getConditionalProbabilityPosterior("male"), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbabilityPosterior("female"), 1e-5);

		
		NodeCategorical nAge = new NodeCategorical(null, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");


		assertEquals(1, nGender.getParentsDimensionality());
		assertEquals(2, nAge.getParentsDimensionality());
		
	}
	
	@Test
	public void testOrderNodes1several() {
		for (int i=0; i<10; i++)
			testOrderNodes1();
	}
	
	
	protected void testOrderNodes1() {
		
		CategoricalBayesianNetwork bn = new CategoricalBayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical(bn, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical(bn, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		
		// nodes should be enumerated from the root to the leaf
		List<NodeCategorical> l = bn.enumerateNodes();
		assertEquals(nGender, l.get(0));
		assertEquals(nAge, l.get(1));
		
	}
	

	@Test
	public void testWrite() {
		
		CategoricalBayesianNetwork bn = new CategoricalBayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical(bn, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical(bn, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		File f = null;
		try {
			f = tempFolder.newFile("test1.xmlbif");
			bn.saveAsXMLBIF(f);
			
			// bn.saveAsXMLBIF( "/tmp/test1.xmlbif"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// TODO now try to read the network and check it work
		
		CategoricalBayesianNetwork bn2 = CategoricalBayesianNetwork.loadFromXMLBIF(f);
		assertEquals(bn.getNodes().size(), bn2.getNodes().size());
		
		assertEquals(bn.getVariable("gender").getCardinality(), bn2.getVariable("gender").getCardinality());
		assertEquals(bn.getVariable("age").getCardinality(), bn2.getVariable("age").getCardinality());

		
	}

	@Test
	public void testProbabilityDistribution() {
		
		CategoricalBayesianNetwork bn = new CategoricalBayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical(bn, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical(bn, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		Map<NodeCategorical, String> node2value = new HashMap<>();
		node2value.put(nGender, "male");
		node2value.put(nAge, "<15");
		assertEquals(
				0.55*0.55,
				bn.jointProbability(node2value, Collections.emptyMap()),
				1e-8
				);
		assertEquals(
				bn.jointProbability(node2value, Collections.emptyMap()),
				bn.jointProbabilityFromFactors(node2value),
				1e-8
				);
		node2value.put(nGender, "male");
		node2value.put(nAge, ">=15");
		assertEquals(
				0.55*0.45,
				bn.jointProbability(node2value, Collections.emptyMap()),
				1e-8
				);
		
		assertEquals(
				bn.jointProbability(node2value, Collections.emptyMap()),
				bn.jointProbabilityFromFactors(node2value),
				1e-8
				);
		
		
	}
	
	
}
