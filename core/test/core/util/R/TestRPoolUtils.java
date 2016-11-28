package core.util.R;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.math.R.Rsession;
import org.rosuda.REngine.REXP;

import core.util.R.RPoolUtils;

public class TestRPoolUtils {

	@Test
	/**
	 * gets 10 different sessions. Ensures they are not contaminated by each other.
	 */
	public void testGet10Sessions() {
		
		List<Rsession> sessions = new LinkedList<>();
		for (int i=0; i<10; i++) {
			Rsession novelSession = RPoolUtils.getRSession();
			assertNotNull(novelSession);
			sessions.add(novelSession);
		}
		for (int i=0; i<10; i++) {
			sessions.get(i).eval("a <- "+i);
		}
		for (int i=0; i<10; i++) {
			Integer res = RUtils.evalAsInteger(sessions.get(i), "a");
			assertEquals((Integer)i, res);
		}
		for (Rsession session: sessions) {
			RPoolUtils.returnRSession(session);
		}
		assertNotNull(
				"a shutdown thread should have been created", 
				RPoolUtils.shutdownThread
				);
	}
	

	@Test
	/**
	 * gets 5 sessions. Free them. Reuse them. Ensure we did not created more than 5 sessions.
	 */
	public void testReuseOfSessions() {
		
		List<Rsession> sessions = new LinkedList<>();
		Set<Rsession> uniqueSessions = new java.util.HashSet<>();
		for (int i=0; i<5; i++) {
			Rsession novelSession = RPoolUtils.getRSession();
			assertNotNull(novelSession);
			sessions.add(novelSession);
			uniqueSessions.add(novelSession);
		}
		// return all of them
		for (Rsession session: sessions) {
			RPoolUtils.returnRSession(session);
		}
		// recreate 5 !
		for (int i=0; i<5; i++) {
			Rsession novelSession = RPoolUtils.getRSession();
			assertNotNull(novelSession);
			sessions.add(novelSession);
			uniqueSessions.add(novelSession);
		}
		// return all of them
		for (Rsession session: sessions) {
			RPoolUtils.returnRSession(session);
		}
		// we asked total 10 sessions
		assertEquals(10, sessions.size());
		// but only 5 where created 
		assertEquals(5, uniqueSessions.size());
		
	}
	

	@Test
	/**
	 * gets 5 sessions. Free them. Reuse them. Ensure we did not created more than 5 sessions.
	 */
	public void testGetReuseSessionsWithErrors() {
		
		List<Rsession> sessions = new LinkedList<>();
		Set<Rsession> uniqueSessions = new java.util.HashSet<>();
		for (int i=0; i<5; i++) {
			Rsession novelSession = RPoolUtils.getRSession();
			assertNotNull(novelSession);
			sessions.add(novelSession);
			uniqueSessions.add(novelSession);
		}
		// make one of them fail
		try{
			sessions.get(1).eval("here is an error !");
		} catch(RuntimeException e) {
		}
		// return all of them
		for (Rsession session: sessions) {
			RPoolUtils.returnRSession(session);
		}
		// recreate 5 !
		for (int i=0; i<5; i++) {
			Rsession novelSession = RPoolUtils.getRSession();
			assertNotNull(novelSession);
			sessions.add(novelSession);
			uniqueSessions.add(novelSession);
		}
		// return all of them
		for (Rsession session: sessions) {
			RPoolUtils.returnRSession(session);
		}
		// we asked total 10 sessions
		assertEquals(10, sessions.size());
		// but 6 where created : the 5 original + 1 not working anymore that was replaced.
		assertEquals(6, uniqueSessions.size());
		
	}

	@Test
	/**
	 * Ensure the session is cleaned after being returned to the pool. 
	 */
	public void testSessionsReset() {
		
		// take a session, assign a value, return it. 
		Rsession session = RPoolUtils.getRSession();
		session.eval("a <- 42");		
		RPoolUtils.returnRSession(session);
		
		// take a session again (it should be the same)
		Rsession novelSession = RPoolUtils.getRSession();
		assertEquals(session, novelSession);
		REXP res = novelSession.eval("a");		
		assertNull(
				"the variable should have been deleted", 
				res 
				);
	}
	

}
