package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFactors {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSum() {
		
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
		
		Factor f = nAge.asFactor();
		
		// is the factor having the right size? 
		assertEquals(2*2, f.values.size());
		
		assertEquals(0.55, f.get("age","<15","gender","male"), Math.pow(1, -4));
		
		// test sum
		Factor summed = f.sumOut("gender");
		assertEquals(1.05, summed.get("age","<15"), Math.pow(1, -4));
		assertEquals(0.95, summed.get("age",">=15"), Math.pow(1, -4));
		
		try {
			summed.get("age");
			fail("should have raised an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all's right.
		}
		try {
			summed.get("age", "15");
			fail("should have raised an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all's right.
		}
		try {
			summed.get("gender", "<15");
			fail("should have raised an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// all's right.
		}
		summed = f.sumOut("age");
		assertEquals(1., summed.get("gender","male"), Math.pow(1, -4));
		assertEquals(1., summed.get("gender","female"), Math.pow(1, -4));

	}
	
	@Test
	public void testMultiply() {
		
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
		
		NodeCategorical nCSP = new NodeCategorical(bn, "CSP");
		nCSP.addParent(nGender);
		nCSP.addDomain("+", "++");
		nCSP.setProbabilities(0.1, "+", "gender", "male");
		nCSP.setProbabilities(0.9, "++", "gender", "male");
		nCSP.setProbabilities(0.2, "+", "gender", "female");
		nCSP.setProbabilities(0.8, "++", "gender", "female");
		

		Factor f1 = nAge.asFactor();
		Factor f2 = nGender.asFactor();
		
		Factor m = f1.multiply(f2);
		
		// is the factor having the right size? 
		assertEquals(2*2, m.values.size());
				
		// test mult
		assertEquals(0.45*0.55, m.get("age",">=15","gender","male"), Math.pow(1, -4));
		assertEquals(0.5*0.45, m.get("age","<15","gender","female"), Math.pow(1, -4));
		
		// another test
		f1 = nAge.asFactor();
		f2 = nCSP.asFactor();

		m = f1.multiply(f2);
		
		// is the factor having the right size? 
		assertEquals(2*2*2, m.values.size());
		
		// test mult
		assertEquals(0.45*0.1, m.get("age",">=15","gender","male","CSP","+"), Math.pow(1, -4));
		assertEquals(0.55*0.1, m.get("age","<15","gender","male","CSP","+"), Math.pow(1, -4));
		
	}

}
