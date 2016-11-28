package core.util.R;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.math.R.Rsession;

/**
 * Manages pools of R sessions. Useful if you intend to do an intensive usage of Rsessions, 
 * such as running several sessions in parallel or using them in loops. This will take care
 * of freeing ressources, opening as many sesssions as required, etc. 
 * 
 * @author Samuel Thiriot
 *
 */
public final class RPoolUtils {

	private static Object poolLocker = new Object();
	private static Set<Rsession> poolAvailable = new HashSet<Rsession>(100);
	private static Set<Rsession> poolRunning = new HashSet<Rsession>(100);
	
	private static Logger logger = LogManager.getLogger();

	protected static Thread shutdownThread = null;
	
	/**
	 * Closes all the R sessions. Should be called before leaving the program.
	 */
	public static void closeAllSessions() {
		
	}

	/**
	 * Returns a R session. Will be taken from the pool if any is available (and sane), 
	 * or a novel one will be provided. Please return it after usage with returnRSession(). 
	 * @return
	 */
	public static Rsession getRSession() {
		synchronized (poolLocker) {
			Rsession res = null;
			
			while (true) {
				// get or create an available session
				if (poolAvailable.isEmpty()) {
					logger.debug("all the "+poolRunning.size()+" R sessions are busy; creating another session in pool");
					res = RUtils.createNewLocalRSession();
					if (poolRunning.isEmpty()) {
						// that's the first thread created. Let's add a shutdown thread.
						addShutdownThread();
					}
				} else {
					res = poolAvailable.iterator().next();
					poolAvailable.remove(res);
					// is it still working ?
					if (!RUtils.isResponsive(res)) {
						logger.warn("this R session is not operational anymore. Will close it and create another one instead.");
						try {
							res.end();
						} catch (Exception e) {
						}
						res = null;
					}
				}
				
				if (res != null)
					break;
				
			} 
			poolRunning.add(res);
			return res;
		}
	}
	

	public static void returnRSession(Rsession session) {
		synchronized (poolLocker) {
			poolRunning.remove(session);
			poolAvailable.add(session);
			logger.debug("a session was returned to the pool; we now have "+poolAvailable.size()+" available vs. "+poolRunning.size()+" used");
		}
	}
	

	/**
	 * Adds a shutdown thread that will close every open R session when the program exits
	 */
	private static void addShutdownThread() {
		
		if (RPoolUtils.shutdownThread != null)
			return;
		
		RPoolUtils.shutdownThread = new Thread() {
			public void run() {
				RPoolUtils.closeAllSessions();
			};
		};
		
		Runtime.getRuntime().addShutdownHook(RPoolUtils.shutdownThread);
		
	}
	
	
}
