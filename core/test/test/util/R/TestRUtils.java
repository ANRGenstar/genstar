package test.util.R;

import static org.junit.Assert.*;

import org.junit.Test;
import org.math.R.Rsession;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

import core.util.R.RUtils;

/**
 * Nota bene: this assume R is installed and configured in the current environment.
 * If it is not the case, many tests will fail with exceptions. 
 * 
 * @author Samuel Thiriot
 *
 */
public class TestRUtils {

	@Test
	/**
	 * Ensures R availability can be tested without exception. 
	 */
	public void testIsRAvailable() {
		try {
			boolean available = RUtils.isRAvailable();
			System.err.println("R available: "+available);
		} catch (RuntimeException e) {
			fail("Exception while testing R.");
		}
	}

	@Test
	/**
	 * If R is available, the version should not be null, but it should if R isnt. 
	 */
	public void testGetRVersion() {
		String version = RUtils.getRVersion();
		if (RUtils.isRAvailable()) {
			assertNotNull(version);
		} else {
			assertNull(version);
		}
	}
	
	@Test
	public void testCreateNewLocalRSession() {

		Rsession rsession = RUtils.createNewLocalRSession();
		if (RUtils.isRAvailable()) {
			assertNotNull(rsession);	
		}  else {
			throw new RuntimeException("cannot be tested without R being available in the environment");
		}
		
		System.err.println("status: "+rsession.getStatus());
		REXP o = rsession.eval("R.version.string");
		assertNotNull(o);
		try {
			System.err.println("using R version "+o.asString());
		} catch (REXPMismatchException e1) {
			fail("unable to get R version:"+e1.getMessage());
		}
		
		// ensure we can read an Integer
		{
			REXP expRes = rsession.eval("1");
			try {
				assertEquals(1, expRes.asInteger());
			} catch (REXPMismatchException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		{
			REXP expRes = rsession.eval("-1");
			try {
				assertEquals(-1, expRes.asInteger());
			} catch (REXPMismatchException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		{
			REXP expRes = rsession.eval("2+2");
			try {
				assertEquals(4, expRes.asInteger());
			} catch (REXPMismatchException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		
	}


	@Test
	public void testIsLibraryInstalled() {

		Rsession session = RUtils.createNewLocalRSession();
		
		try {
			boolean installedIGraph = RUtils.isPackageInstalled(session, "base");
			assertTrue(installedIGraph);
			installedIGraph = RUtils.isPackageInstalled(session, "thisLibraryShouldNotExist!");
			assertFalse(installedIGraph);
			//session.isPackageInstalled(
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	

	@Test
	/**
	 * Will only pass if igraph is available in the test environment (sorry)
	 */
	public void testLoadPackage() {
		
		Rsession session = RUtils.createNewLocalRSession();
		
		if (! RUtils.isPackageInstalled(session, "igraph")) {
			throw new RuntimeException("the igraph package is not installed; the test cannot be done.");
		}
		
		try {
			assertFalse(
					"the package should not be loaded by default (at least for this test)", 
					RUtils.isPackageLoaded(session, "igraph")
					);
			RUtils.loadPackage(session, "igraph");
			assertTrue(
					"the package was expected to be loaded after a call to loadPackage", 
					RUtils.isPackageLoaded(session, "igraph")
					);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		
	}


	@Test
	public void testEvalInteger() {

		Rsession session = RUtils.createNewLocalRSession();
		
		try {
			Integer res = RUtils.evalAsInteger(session, "1+41");
			assertNotNull(res);
			assertEquals((Integer)42, res);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	

	@Test
	public void testEvalDouble() {

		Rsession session = RUtils.createNewLocalRSession();
		
		try {
			Double res = RUtils.evalAsDouble(session, "1+41");
			assertNotNull(res);
			assertEquals((Double)42., res);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	

	@Test
	public void testEvalString() {

		Rsession session = RUtils.createNewLocalRSession();
		
		try {
			String res = RUtils.evalAsString(session, "\"truc\"");
			assertNotNull(res);
			assertEquals("truc", res);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

}
