package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDTree {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	protected CategoricalBayesianNetwork loadSprinklerNetwork() {

		File f = new File("./src/test/resources/bayesiannetworks/sprinkler_rain_grasswet.xmlbif");
		
		return CategoricalBayesianNetwork.loadFromXMLBIF(f);
		
	}
	

	protected CategoricalBayesianNetwork loadGerlandNetwork() {

		File f = new File("./src/test/resources/bayesiannetworks/gerland.xbif");
		
		return CategoricalBayesianNetwork.loadFromXMLBIF(f);
		
	}
	
	@Test
	public void testProbabilityEvidenceManualDNodes() {
		 
		CategoricalBayesianNetwork bn = loadSprinklerNetwork();
		
		DNode nodeRoot 		= new DNode(bn);
		
		DNode nodeRain		= new DNode(bn.getVariable("rain"));
		nodeRain.becomeLeftChild(nodeRoot);
		
		DNode nodeTwo 		= new DNode();
		nodeTwo.becomeRightChild(nodeRoot);
				
		DNode nodeGrassWet 	= new DNode(bn.getVariable("grass_wet"));
		nodeGrassWet.becomeLeftChild(nodeTwo);
		
		DNode nodeSprinkler = new DNode(bn.getVariable("sprinkler"));
		nodeSprinkler.becomeRightChild(nodeTwo);
		
		// TODO check network when constructing inf engine
		
		// check consistency 
		nodeRoot.checkConsistency();
		
		System.out.println(nodeRoot);

		// check vars 
		/*
		assertEquals(1, nodeRoot.vars().size());
		assertTrue(nodeRoot.vars().contains(bn.getVariable("rain")));

		assertEquals(1, nodeTwo.vars().size());
		assertTrue(nodeTwo.vars().contains(bn.getVariable("rain")));
	*/
		
		// check 
		System.out.println(nodeRoot.cutset());
		System.out.println(nodeTwo.cutset());
		
		assertEquals(
				0.002, 
				nodeRoot.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"rain",			"true",
						"sprinkler",	"true"
						//"grass_wet",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.00198, 
				nodeRoot.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"rain",			"true",
						"sprinkler",	"true",
						"grass_wet",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.322, 
				nodeRoot.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"sprinkler",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.44838, 
				nodeRoot.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"grass_wet",	"true"
						)),
				Math.pow(1, -8)
				);
		
		InferencePerformanceUtils.singleton.display();

		
	}
	

	@Test
	public void testEliminationOrder2DTreeSprinkler() {
		 
		
		CategoricalBayesianNetwork bn = loadSprinklerNetwork();

		List<NodeCategorical> variables = new ArrayList<>(bn.getNodes());
		Collections.shuffle(variables);
		
		DNode dtree = DNode.eliminationOrder2DTree(bn, variables);
		System.out.println(dtree);
		
		assertEquals(
				0.00198, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"rain",			"true",
						"sprinkler",	"true",
						"grass_wet",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.002, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"rain",			"true",
						"sprinkler",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.322, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"sprinkler",	"true"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.44838, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"grass_wet",	"true"
						)),
				Math.pow(1, -8)
				);
		InferencePerformanceUtils.singleton.display();
	}
	

	@Test
	public void testEliminationOrder2DTreeGerland() {
		 
		
		CategoricalBayesianNetwork bn = loadGerlandNetwork();

		List<NodeCategorical> variables = new ArrayList<>(bn.getNodes());
		Collections.shuffle(variables);
		
		DNode dtree = DNode.eliminationOrder2DTree(bn, variables);
		System.out.println(dtree);
		
		assertEquals(
				0.44231461, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"actif",	"oui"
						)),
				Math.pow(1, -8)
				);
		
		assertEquals(
				0.0487540568, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"salarie",	"nonsalarie"
						)),
				Math.pow(1, -8)
				);
		
		assertEquals(
				0.5, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"gender",	"male"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.0349228141, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"gender",	"male",
						"salarie",	"nonsalarie"
						)),
				Math.pow(1, -8)
				);
		
		InferencePerformanceUtils.singleton.display();
	}
	

	@Test
	public void testEliminationDepthFirstGerland() {
		 
		
		CategoricalBayesianNetwork bn = loadGerlandNetwork();

		List<NodeCategorical> eliminationOrder = EliminationOrderDeepFirstSearch.computeEliminationOrder(bn);
			
		DNode dtree = DNode.eliminationOrder2DTree(bn, eliminationOrder);
		System.out.println(dtree);
		
		assertEquals(
				0.44231461, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"actif",	"oui"
						)),
				Math.pow(1, -8)
				);
		
		assertEquals(
				0.0487540568, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"salarie",	"nonsalarie"
						)),
				Math.pow(1, -8)
				);
		
		assertEquals(
				0.5, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"gender",	"male"
						)),
				Math.pow(1, -8)
				);
		assertEquals(
				0.0349228141, 
				dtree.recursiveConditionning(bn.toNodeAndValue((Collection<NodeCategorical>)null,
						"gender",	"male",
						"salarie",	"nonsalarie"
						)),
				Math.pow(1, -8)
				);
		
		InferencePerformanceUtils.singleton.display();
	}
}
