package io.util;

public class GSPerformanceUtil {

	private int stempCalls;
	private long latestStempCall;
	private double cumulStempCall;

	private long latestStempProportion;
	private double cumulStempProportion;

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

	public String getStempPerformance(int step){
		long thisStemp = System.currentTimeMillis(); 
		double timer = (thisStemp - latestStempCall)/1000d;
		if(latestStempCall != 0l)	
			cumulStempCall += timer;
		stempCalls += step;
		this.latestStempCall = thisStemp;
		return "Step "+(stempCalls)+" -> "+timer+" s / "+((double) Math.round(cumulStempCall * 1000) / 1000)+" s)";
	}

	public void sysoStempPerformance(int step, Object caller){
		String s = getStempPerformance(step);
		if(logSyso){
			if(firstSyso){
				System.out.println("\nMethod caller: "+caller.getClass().getSimpleName()+
						"\n-------------------------\n"+
						performanceTestDescription+
						"\n-------------------------");
				firstSyso = false;
			}
			System.out.println(s);
		}
	}

	public String getStempPerformance(double proportion){
		long thisStemp = System.currentTimeMillis(); 
		double timer = (thisStemp - latestStempProportion)/1000d;
		if(latestStempProportion != 0l)
			cumulStempProportion += timer;
		this.latestStempProportion = thisStemp;
		return (Math.round(Math.round(proportion*100)))+" % -> "+timer+" s / "+((double) Math.round(cumulStempProportion * 1000) / 1000)+" s)";
	}

	public void sysoStempPerformance(double proportion, Object caller){
		String s = getStempPerformance(proportion);
		if(logSyso){
			if(firstSyso){
				System.out.println("Method caller: "+caller.getClass().getSimpleName()+
						"\n-------------------------\n"+
						performanceTestDescription+
						"\n-------------------------");
				firstSyso = false;
			}
			System.out.println(s);
		}
	}

	public void resetStemp(){
		this.resetStempCall();
		this.resetStempProp();
		firstSyso = true;
		performanceTestDescription = "no reason";
	}

	public void resetStempCall(){
		stempCalls = 0;
		latestStempCall = 0l;
		cumulStempCall = 0d;
	}

	public void resetStempProp(){
		latestStempProportion = 0l;
		cumulStempProportion = 0d;
	}
}
