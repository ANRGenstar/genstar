package core.util.data;

public class RangeTemplate {
	
	private String lowerBound, middle, upperBound;
	private String match;
	
	public RangeTemplate(String lowerBound, String middle, String upperBound,
			String match){
		this.match = match;
		this.middle = middle;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public String getmiddleTemplate(Number low, Number up){
		return middle.replaceFirst(match, low.toString())
				.replaceFirst(match, up.toString());
	}
	
	public String getLowerTemplate(Number up){
		return lowerBound.replaceFirst(match, up.toString());
	}
	
	public String getUpperTemplate(Number low){
		return upperBound.replaceFirst(match, low.toString());
	}
	
	@Override
	public String toString(){
		return "Template: "+lowerBound+" ... "+middle+" ... "+upperBound;
	}
	
}
