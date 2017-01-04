package core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * 
 * @author Kevin Chapuis
 *
 */
public class GSPerformanceUtil {

	private int stempCalls;
	private long latestStemp;
	private double cumulStemp;

	private boolean firstSyso;
	private String performanceTestDescription;

	private Logger logger;
	
	private double objectif;

	private GSPerformanceUtil(Logger logger){
		resetStemp();
		this.logger = logger;
	}

	/**
	 * Should be called with the logger of the caller; for instance if the caller logger is "gospl.sampler.hierarchical", 
	 * performance will be logged into "gospl.sampler.hierarchical.performance" with an INFO level of verbosity. 
	 * @param performanceTestDescription
	 * @param logger
	 */
	public GSPerformanceUtil(String performanceTestDescription, Logger logger) {
		this(LogManager.getLogger(logger.getName()+"."+GSPerformanceUtil.class.getSimpleName()));
		this.performanceTestDescription = performanceTestDescription;
	}
	

	/**
	 * Information will be logged into "<calledclassname>.GSPerformanceUtil"
	 * @param performanceTestDescription
	 */
	public GSPerformanceUtil(String performanceTestDescription) {
		this(LogManager.getLogger());
		this.performanceTestDescription = performanceTestDescription;
	}

	public String getStempPerformance(String message){
		long thisStemp = System.currentTimeMillis(); 
		double timer = (thisStemp - latestStemp)/1000d;
		if(latestStemp != 0l)	
			cumulStemp += timer;
		this.latestStemp = thisStemp;
		return message+" -> "+timer+" s / "+((double) Math.round(cumulStemp * 1000) / 1000)+" s";
	}
	
	public String getStempPerformance(int stepFoward){
		stempCalls += stepFoward;
		return getStempPerformance("Step "+stempCalls);
	}

	public String getStempPerformance(double proportion){
		return getStempPerformance(Math.round(Math.round(proportion*100))+"%");
	}
	
	public void sysoStempPerformance(int step, Object caller){
		sysoStempMessage(getStempPerformance(step), caller);
	}

	public void sysoStempPerformance(double proportion, Object caller){
		sysoStempMessage(getStempPerformance(proportion), caller);
	}
	
	public void sysoStempPerformance(String message, Object caller){
		sysoStempMessage(getStempPerformance(message), caller);
	}
	
	public void sysoStempMessage(String message){
		logger.info(message);
	}
	
	public void resetStemp(){
		this.resetStempCalls();
		firstSyso = true;
		performanceTestDescription = "no reason";
	}

	public void resetStempCalls(){
		stempCalls = 0;
		latestStemp = 0l;
		cumulStemp = 0d;
	}
	
	private void sysoStempMessage(String message, Object caller){
		String callerString = caller.getClass().getSimpleName();
		if(caller.getClass().equals(String.class))
			callerString = caller.toString();
		
		if(firstSyso){
			logger.info("\nMethod caller: "+callerString+
					"\n-------------------------\n"+
					performanceTestDescription+
					"\n-------------------------");
			firstSyso = false;
		}
		logger.info(message);
	}

	public void setObjectif(double objectif) {
		this.objectif = objectif;
	}
	
	public double getObjectif(){
		return objectif;
	}
	
	public Logger getLogger(){
		return logger;
	}

}
