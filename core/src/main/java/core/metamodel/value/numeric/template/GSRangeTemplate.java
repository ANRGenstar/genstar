package core.metamodel.value.numeric.template;

import java.util.stream.Stream;

import core.util.data.GSDataParser.NumMatcher;

/**
 * Define how a string should be parsed to formated range data string
 * 
 * @author kevinchapuis
 *
 */
public class GSRangeTemplate {
	
	private String bottomBound, middle, topBound;
	private String match;
	
	private NumMatcher numMatcher; 
	
	public GSRangeTemplate(String lowerBound, String middle, String upperBound,
			String match, NumMatcher numMatcher){
		this.match = match;
		this.middle = middle;
		this.bottomBound = lowerBound;
		this.topBound = upperBound;
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
	 * Process the lone bottom bound (smallest number) to obtain a formated String representation
	 * 
	 * @param up
	 * @return
	 */
	public String getBottomTemplate(Number bottom){
		return bottomBound.replaceFirst(match, bottom.toString());
	}
	
	/**
	 * Process the lone top bound (largest number) to obtain a formated String representation
	 * 
	 * @param low
	 * @return
	 */
	public String getTopTemplate(Number top){
		return topBound.replaceFirst(match, top.toString());
	}
	
	/**
	 * Process the input string {@code value} to determine if it complies with this template
	 * of range data
	 * 
	 * @param value
	 * @return
	 */
	public boolean isValideRangeCandidate(String value) {
		String valueTemplate = value.replaceAll(numMatcher.getMatch(), match);
		return Stream.of(bottomBound, middle, topBound).anyMatch(template -> template.equals(valueTemplate));
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
		return "Template: {"+bottomBound+" ... "+middle+" ... "+topBound+"}";
	}
	
}
