package io.util;

public class GSPerformanceUtil {

	private int stempCalls;
	private long latestStemp;
	private double cumulStemp;

	private boolean firstSyso;
	private String performanceTestDescription;

	private boolean logSyso;

	private GSPerformanceUtil(boolean logSyso){
		resetStemp();
		this.logSyso = logSyso;
	}

	public GSPerformanceUtil(String performanceTestDescription, boolean logSyso) {
		this(logSyso);
		this.performanceTestDescription = performanceTestDescription;
	}

	public String getStempPerformance(String message){
		long thisStemp = System.currentTimeMillis(); 
		double timer = (thisStemp - latestStemp)/1000d;
		if(latestStemp != 0l)	
			cumulStemp += timer;
		this.latestStemp = thisStemp;
		return message+" -> "+timer+" s / "+((double) Math.round(cumulStemp * 1000) / 1000)+" s)";
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
		if(logSyso)
			System.out.println(message);
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
		if(logSyso){
			if(firstSyso){
				System.out.println("Method caller: "+caller.getClass().getSimpleName()+
						"\n-------------------------\n"+
						performanceTestDescription+
						"\n-------------------------");
				firstSyso = false;
			}
			System.out.println(message);
		}
	}

}
