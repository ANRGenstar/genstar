package gospl.algo.bn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gospl.algo.sr.bn.BayesianNetwork;
import gospl.algo.sr.bn.NodeCategorical;

public class TestBayesianNetwork {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	   
	@Test
	public void testDefineNodeCategorical() {
		
		NodeCategorical nGender = new NodeCategorical("gender");
		
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
	public void testStorageProbas() {
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getProbability("male").doubleValue(), 1e-5);
		assertEquals(0.45, nGender.getProbability("female").doubleValue(), 1e-5);
		
		assertEquals(0.55, nGender.getConditionalProbability("male").doubleValue(), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbability("female").doubleValue(), 1e-5);

		assertTrue("CPT should be valid", nGender.isValid());
		
		NodeCategorical nAge = new NodeCategorical("age");
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

		assertEquals(0.50, nAge.getProbability("<15","gender","female").doubleValue(), 1e-5);
		assertEquals(0.55, nAge.getProbability("<15","gender","male").doubleValue(), 1e-5);

		assertTrue("CPT should be valid", nAge.isValid());		
	}
	

	@Test
	public void testComputeProbas() {
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");

		assertEquals(0.55, nGender.getConditionalProbabilityPosterior("male").doubleValue(), 1e-5);
		assertEquals(0.45, nGender.getConditionalProbabilityPosterior("female").doubleValue(), 1e-5);

		
		NodeCategorical nAge = new NodeCategorical("age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

	
		assertEquals(0.5275, nAge.getConditionalProbabilityPosterior("<15").doubleValue(), 1e-5);
		assertEquals(0.4725, nAge.getConditionalProbabilityPosterior(">=15").doubleValue(), 1e-5);

	}
	
	@Test
	public void testOrderNodes1several() {
		for (int i=0; i<10; i++)
			testOrderNodes1();
	}
	
	protected void testOrderNodes1() {
		
		BayesianNetwork bn = new BayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical("age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		bn.add(nGender);
		bn.add(nAge);
		
		// nodes should be enumerated from the root to the leaf
		List<NodeCategorical> l = bn.enumerateNodes();
		assertEquals(nGender, l.get(0));
		assertEquals(nAge, l.get(1));
		
	}
	

	@Test
	public void testWrite() {
		
		BayesianNetwork bn = new BayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical("age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		bn.add(nGender);
		bn.add(nAge);
		
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
		
		BayesianNetwork bn2 = BayesianNetwork.loadFromXMLBIF(f);
		assertEquals(bn.getNodes().size(), bn2.getNodes().size());
		
		assertEquals(bn.getVariable("gender").getCardinality(), bn2.getVariable("gender").getCardinality());
		assertEquals(bn.getVariable("age").getCardinality(), bn2.getVariable("age").getCardinality());

		
	}
	
	
}
