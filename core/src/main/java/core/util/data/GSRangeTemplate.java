package core.util.data;

import java.util.stream.Stream;

import core.util.data.GSDataParser.NumMatcher;

/**
 * Define how a string should be parsed to formated range data string
 * 
 * @author kevinchapuis
 *
 */
public class GSRangeTemplate {
	
	private String lowerBound, middle, upperBound;
	private String match;
	
	private NumMatcher numMatcher; 
	
	protected GSRangeTemplate(String lowerBound, String middle, String upperBound,
			String match, NumMatcher numMatcher){
		this.match = match;
		this.middle = middle;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.numMatcher = numMatcher;
	}
	
	/**
	 * Process lower and upper numeric bounds to obtain a formated String representation 
	 * 
	 * @param low
	 * @param up
	 * @return
	 */
	public String getMiddleTemplate(Number low, Number up){
		return middle.replaceFirst(match, low.toString())
				.replaceFirst(match, up.toString());
	}
	
	/**
	 * Process the lone lower bound to obtain a formated String representation
	 * 
	 * @param up
	 * @return
	 */
	public String getLowerTemplate(Number up){
		return lowerBound.replaceFirst(match, up.toString());
	}
	
	/**
	 * Process the lone upper bound to obtain a formated String representation
	 * 
	 * @param low
	 * @return
	 */
	public String getUpperTemplate(Number low){
		return upperBound.replaceFirst(match, low.toString());
	}
	
	/**
	 * Process the input string {@code value} to determine if it complies with this template
	 * of range data
	 * 
	 * @param value
	 * @return
	 */
	public boolean isValideRangeCandidate(String value) {
		// TODO Auto-generated method stub
		String valueTemplate = value.replaceAll(numMatcher.getMatch(), match);
		return Stream.of(lowerBound, middle, upperBound).anyMatch(template -> template.equals(valueTemplate));
	}

	/**
	 * Enum type that define matcher for numerical value to parse in String 
	 * 
	 * @return
	 */
	public NumMatcher getNumerciMatcher(){
		return numMatcher;
	}
	
	@Override
	public String toString(){
		return "Template: "+lowerBound+" ... "+middle+" ... "+upperBound;
	}
	
}
