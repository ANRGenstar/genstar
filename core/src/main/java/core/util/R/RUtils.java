package core.util.R;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.math.R.Rsession;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * Provides access to R features (when available).
 * 
 * Basic features include:
 * <ul>
 * <li>Test if R is available: TODO </li>
 * <li>Obtain an Rsession object to run R commands directly: TODO</li>
 * </ul>
 * 
 * If you intend to do an intensive usage of R, have a look to RPollUtils TODO.
 * 
 * @author Samuel Thiriot
 */
public final class RUtils {

	/**
	 * True if R was identified available, False if R was identified not available, null if undefined yet. 
	 */
	private static Boolean isRAvailable = null;
	
	/**
	 * The logger used for Utils.
	 */
	private static Logger logger = LogManager.getLogger("RUtils");
	
	/**
	 * Defines after how long - in milliseconds - an R session will be considered as not reactive anymore/
	 */
	private static final int TIMEOUT_SESSION = 500;
	
	/**
	 * version of R detected at runtime. Null if not tested or R not available, or a string that 
	 * directly contains the content of the version as transmitted by R itself. 
	 */
	private static String rVersion = null;
	
	/**
	 * returns true if R seems available. 
	 * Tries to open a connection to R, and waits a bit (RUtils.TIMEOUT_SESSION). 
	 * @return
	 */
	public static boolean isRAvailable() {
		return isRAvailable(false);
	}
	
	/**
	 * Returns the R version as a String (refer to R for conventions), or null if 
	 * R cannot be accessed. 
	 * 
	 * @return
	 */
	public static String getRVersion() {
		if (! isRAvailable()) { // ensure isRAvailable was ran, as it is the one loading the version - anyway we cannot get the version if R is not connected
			return null;
		}
		return rVersion;
	}
	
	/**
	 * returns true if R seems available. 
	 * Tries to open a connection to R, and waits a bit (RUtils.TIMEOUT_SESSION). 
	 * Does the test only once, except if your pass True as a parameter.
	 * @return
	 */
	public static boolean isRAvailable(boolean force) {
		// only try to connect if not already tested (or force to retest)
		if (isRAvailable == null || force) {
			try {
				Rsession session = createNewLocalRSession();
				// wait a bit for it to be connected
				long startStamp = System.currentTimeMillis();
				while (!session.connected) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
					if (System.currentTimeMillis() - startStamp > RUtils.TIMEOUT_SESSION) {
						logger.warn("R session timeout: no answer from R after "+RUtils.TIMEOUT_SESSION+"ms");
						break;
					}
				}
				// get version of R
				RUtils.detectRVersion(session);
				// store result
				isRAvailable = session.connected;
				// clean this connection and free resources
				session.end();
			} catch (RuntimeException e) {
				isRAvailable = false;
			}
		}
		return isRAvailable;
	}
		
	private static void detectRVersion(Rsession session) {
		REXP o = session.eval("R.version.string");
		try {
			RUtils.rVersion = o.asString();
			logger.debug("using R version "+RUtils.rVersion);
		} catch (REXPMismatchException e1) {
			throw new RRuntimeException("Error while trying to fetch the version of R", e1);
		}
	}
	
	/**
	 * Returns a novel session to be used by a user. 
	 * It will be available to you only. 
	 * It is your responsability to finish it at the end of usage.
	 * @return
	 */
	public static Rsession createNewLocalRSession() {
		return Rsession.newInstanceTry( 
				System.out,//new NullPrintStream(),//System.out, 
				null
				);
	}
	

	/**
	 * Loads the package in the current version if not already loaded
	 * @param name
	 * @return
	 */
	public static void loadPackage(Rsession session, String name) {
		session.eval("library("+name+")");
		session.loadPackage(name);
		RUtils.analyzeLastError(session);
		if (!session.isPackageLoaded(name)) {
			throw new RRuntimeException("the package "+name+" was not loaded as expected after a loadPackage()");
		}

	}
	
	/**
	 * Returns true if the library is installed and available in the session, false else.
	 * @param name
	 */
	public static boolean isPackageInstalled(Rsession session, String name) {
		
		return session.isPackageInstalled(name, null);
		
	}

	/**
	 * Returns true if the library is already loaded in the session, false else.
	 * @param name
	 */
	public static boolean isPackageLoaded(Rsession session, String name) {
		return session.isPackageLoaded(name);
	}
	
	
	/**
	 * Attempts to install an R package in the current environment and load it in the current session.
	 * @param name
	 */
	public static void installPackage(Rsession session, String name) {

		// TODO encapsulte into try/catch
		session.installPackage(name, true);
		
	}
	
	/**
	 * Returns a string with the error if problem, or null if no problem.
	 * @param r
	 * @return
	 */
	public static String analyzeLastError(Rsession r) {
		return r.connection.getLastError();
	}
	
	/**
	 * If an error happened, throws a RRuntimeException with an error as meaningfull as possible.
	 * @param r
	 * @return
	 */
	public static void checkStatus(Rsession r) {
		
		if (r.status == Rsession.STATUS_ERROR)
			throw new RRuntimeException("error during the R computation");
		
		if (r.connection.getLastError() != null && !r.connection.getLastError().equals("OK"))
			throw new RRuntimeException("error during the R computation: R returned "+r.connection.getLastError());
		
	}
	
	
	public static Integer evalAsInteger(Rsession session, String cmd) {
		
		REXP o = session.eval(cmd);
		if (o == null) {
			throw new RRuntimeException("Error while evaluating R command: "+cmd);
		}
		
		try {
			return o.asInteger();
		} catch (REXPMismatchException e) {
			throw new RRuntimeException("Wrong return type (Integer expected) during R command "+cmd);
		}

		
	}
	

	public static Boolean evalAsBoolean(Rsession session, String cmd) {
		
		REXP o = session.eval(cmd);
		if (o == null) {
			throw new RRuntimeException("Error while evaluating R command: "+cmd);
		}
		
		try {
			return (o.asInteger()==1);
		} catch (REXPMismatchException e) {
			throw new RRuntimeException("Wrong return type (Integer expected) during R command "+cmd);
		}

		
	}
	

	public static String evalAsString(Rsession session, String cmd) {
		
		REXP o = session.eval(cmd);
		if (o == null) {
			throw new RRuntimeException("Error while evaluating R command: "+cmd);
		}
		
		try {
			return o.asString();
		} catch (REXPMismatchException e) {
			throw new RRuntimeException("Wrong return type (Integer expected) during R command "+cmd);
		}

		
	}
	
	public static Double evalAsDouble(Rsession session, String cmd) {
		
		REXP o = session.eval(cmd);
		if (o == null) {
			throw new RRuntimeException("Error while evaluating R command: "+cmd);
		}
		
		try {
			return o.asDouble();
		} catch (REXPMismatchException e) {
			throw new RRuntimeException("Wrong return type (Integer expected) during R command "+cmd);
		}

		
	}
	

}
