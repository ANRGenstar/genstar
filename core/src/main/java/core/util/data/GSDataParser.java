package core.util.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import core.metamodel.value.numeric.template.GSRangeTemplate;
import core.util.excpetion.GSIllegalRangedData;

/**
 * 
 * Object that can read data and: 
 * <p><ul>
 *  <li> {@link #getValueType(String)} give the implicit parsed data type ( {@link GSEnumDataType} )
 *  <li> {@link #getRangedDoubleData(String, boolean)} or {@link #getRangedIntegerData(String, boolean)}
 * </ul><p>
 * Can give explicit values from string ranged value data representation
 * 
 * @author kevinchapuis
 *
 */
public class GSDataParser {
	
	public static final String DEFAULT_NUM_MATCH = "#";

	private static String SPLIT_OPERATOR = " ";
	
	public enum NumMatcher{
		DOUBLE_MATCH_ENG("(\\-)?(\\d+\\.\\d+)(E(\\-)?\\d+)?"),
		DOUBLE_POSITIF_MATCH_ENG("(^\\d+\\.\\d+)(E\\-\\d+)?"),
		DOUBLE_MATCH_FR("(\\-)?(\\d+\\,\\d+)(E(\\-)?\\d+)?"),
		DOUBLE_POSITIF_MATCH_FR("(^\\d+\\,\\d+)(E\\-\\d+)?"),
		INT_POSITIF_MATCH("\\d+"),
		INT_MATCH("-?\\d+");
		
		private String match;
		
		private NumMatcher(String match){
			this.match = match;
		}
		
		/**
		 * Gives the string matcher
		 * @return
		 */
		public String getMatch(){return match;}

		/**
		 * Default numeric value String matcher which look for positive integer range values
		 * @return
		 */
		public static NumMatcher getDefault() {return NumMatcher.INT_POSITIF_MATCH;}
		
	}
	
	/**
	 * Methods that retrieve value type ({@link GSEnumDataType}) through string parsing <br/>
	 * Default type is {@value GSEnumDataType#STRING}
	 * 
	 * @param value
	 * @return
	 */
	public GSEnumDataType getValueType(String value){
		value = value.trim();
		if(value.matches(NumMatcher.INT_MATCH.getMatch()))
			return GSEnumDataType.Integer;
		if(value.matches(NumMatcher.DOUBLE_MATCH_ENG.getMatch()) || value.matches(NumMatcher.DOUBLE_MATCH_FR.getMatch()))
			return GSEnumDataType.Continue;
		if(Boolean.TRUE.toString().equalsIgnoreCase(value) || Boolean.FALSE.toString().equalsIgnoreCase(value))
			return GSEnumDataType.Boolean;
		try {
			this.getRangedDoubleData(value, NumMatcher.DOUBLE_MATCH_ENG);
			return GSEnumDataType.Range;
		} catch (Exception e) {
			return GSEnumDataType.Nominal;
		}
	}
	
	/**
	 * Can extract the template of the range data in given string representation
	 * 
	 * @see GSRangeTemplate
	 * 
	 * WARNING: untested
	 * FIXME: only get template with integer based range data
	 * 
	 * @param range
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public GSRangeTemplate getRangeTemplate(List<String> ranges, String match, NumMatcher numMatcher) throws GSIllegalRangedData {
		List<Integer> rangeInt = new ArrayList<>();
		for(String range : ranges)
			rangeInt.addAll(this.getRangedIntegerData(range, numMatcher));
		Collections.sort(rangeInt);
		String lowerBound = "", upperBound = "", middle = "";
		for(String range : ranges){
			List<Integer> ints = this.getRangedIntegerData(range, numMatcher); 
			if(ints.size() == 1) { 
				if(ints.get(0).equals(rangeInt.get(0)))
					lowerBound = range.replaceAll(rangeInt.get(0).toString(), match);
				else if(ints.get(0).equals(rangeInt.get(rangeInt.size()-1)))
					upperBound = range.replaceAll(rangeInt.get(rangeInt.size()-1).toString(), match);
			} else if(middle.isEmpty())
				middle = range.replaceAll(numMatcher.getMatch(), match);
			else if(middle != null){
				String newMiddle = range.replaceAll(numMatcher.getMatch(), match);
				if(!newMiddle.equalsIgnoreCase(middle))
					throw new GSIllegalRangedData("Range template has more than 3 range format");
			}
		}
		return new GSRangeTemplate(lowerBound, middle, upperBound, match, numMatcher);
	}
	
	/**
	 * Extract range template from a list of ranges and given number matcher
	 * 
	 * @param ranges
	 * @param numMatcher
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public GSRangeTemplate getRangeTemplate(List<String> ranges, NumMatcher numMatcher) throws GSIllegalRangedData {
		return getRangeTemplate(ranges, DEFAULT_NUM_MATCH, numMatcher);
	}
	
	/**
	 * default replacement of match {@link NumMatcher}
	 * 
	 * @see #getRangeTemplate(List, String, NumMatcher)
	 * 
	 * @param ranges
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public GSRangeTemplate getRangeTemplate(List<String> ranges) throws GSIllegalRangedData {
		return getRangeTemplate(ranges, DEFAULT_NUM_MATCH, NumMatcher.getDefault());
	}

	/**
	 * Parses double range values from string representation. There is no need for specifying <br/>
	 * any delimiter, although the method rely on proper {@link Double} values string encoding. <br/>
	 * If negative value is true delimiter can't be the null "-" symbol
	 * 
	 * @param range
	 * @return {@link List} of min and max double values based on {@code range} string representation 
	 * @throws GSIllegalRangedData
	 */
	public List<Double> getRangedDoubleData(String range, NumMatcher numMatcher) throws GSIllegalRangedData{
		List<Double> list = new ArrayList<>();
		List<String> stringRange = this.getNumbers(range, numMatcher.getMatch());
		stringRange.stream().forEach(s -> s.trim());
		stringRange = stringRange.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		if(stringRange.isEmpty())
			throw new GSIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GSIllegalRangedData("The string ranged data " +range+ " has more than 2 (lower / upper) values");
	    for(String i : stringRange)
	    	list.add(Double.valueOf(i));
		return list;
	}

	/**
	 * {@link #getRandedDoubleData(String, boolean)} for specification.  Also this method allow for {@code minVal} <br/>
	 * {@code maxVal} forced value: this is intended to encoded ranged value from "min implicit double value" (e.g. age = 0) <br/> 
	 * to ranged parsed integer value or from ranged parsed to "max implicit double value" 
	 *
	 * @param range
	 * @param nullValue
	 * @param minVal
	 * @return {@link List} of min and max double values based on given {@code minVal} and parsed max {@code range}
	 * @throws GSIllegalRangedData
	 */
	@Deprecated
	public List<Double> getRangedData(String range, NumMatcher numMatcher, Double minVal, Double maxVal) throws GSIllegalRangedData{
		List<Double> list = new ArrayList<>();
		range = range.replaceAll(numMatcher.getMatch(), SPLIT_OPERATOR);
		List<String> stringRange = Arrays.asList(range.trim().split(SPLIT_OPERATOR));
		stringRange.stream().forEach(s -> s.trim());
		stringRange = stringRange.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		if(stringRange.isEmpty())
			throw new GSIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GSIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
		if (stringRange.size() == 1){
			if(minVal == null && maxVal == null)
				throw new GSIllegalRangedData("for implicit bounded values, either min or max value in argument must be set to a concret value !");
			if(maxVal == null && minVal != null)
				stringRange.add(0, String.valueOf(minVal));
			else if(minVal == null && maxVal != null)
				stringRange.add(String.valueOf(maxVal));
			if(Double.valueOf(stringRange.get(0)) - minVal <= maxVal - Double.valueOf(stringRange.get(0)))
				stringRange.add(0, String.valueOf(minVal));
			else
				stringRange.add(String.valueOf(maxVal));
		}
	    for(String i : stringRange)
	    	list.add(Double.valueOf(i));
		return list;
	}

	/**
	 * Parses int range values from string representation. There is no need for specifying <br/>
	 * any delimiter, although the method rely on proper {@link Integer} values string encoding. <br/>
	 * If null value is true delimiter can't be the null "-" symbol
	 * 
	 * @param range
	 * @return {@link List} of min and max integer values based on {@code range} string representation
	 * @throws GSIllegalRangedData
	 */
	public List<Integer> getRangedIntegerData(String range, NumMatcher numMatcher) throws GSIllegalRangedData{
		List<Integer> list = new ArrayList<>();
		List<String> stringRange = this.getNumbers(range, numMatcher.getMatch());
		stringRange.stream().forEach(s -> s.trim());
		stringRange = stringRange.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		if(stringRange.isEmpty())
			throw new GSIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GSIllegalRangedData("The string ranged data " +range+ " has more than 2 (lower / upper) values");
	    for(String i : stringRange)
	    		list.add(Integer.valueOf(i));
		return list;
	}
	
	/**
	 * {@link #getRangedIntegerData(String, boolean)} for specification. Also this method allow for {@code minVal} <br/>
	 * {@code maxVal} forced value: this is intended to encoded ranged value from "min implicit integer value" (e.g. age = 0) <br/> 
	 * to ranged parsed integer value or from ranged parsed to "max implicit integer value" 
	 * 
	 * @param range
	 * @param nullValue
	 * @param minVal
	 * @return {@link List} of min and max values
	 * @throws GSIllegalRangedData
	 */
	@Deprecated
	public List<Integer> getRangedData(String range, NumMatcher numMatcher, Integer minVal, Integer maxVal) throws GSIllegalRangedData{
		List<Integer> list = new ArrayList<>();
		range = range.replaceAll(numMatcher.getMatch(), SPLIT_OPERATOR);
		List<String> stringRange = Arrays.asList(range.trim().split(SPLIT_OPERATOR));
		stringRange.stream().forEach(s -> s.trim());
		stringRange = stringRange.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		if(stringRange.isEmpty())
			throw new GSIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GSIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
		if(stringRange.size() == 1){
			if(minVal == null && maxVal == null)
				throw new GSIllegalRangedData("for implicit bounded values, either min or max value in argument must be set to a concret value !");
			if(maxVal == null && minVal != null)
				stringRange.add(0, String.valueOf(minVal));
			else if(minVal == null && maxVal != null)
				stringRange.add(String.valueOf(maxVal));
			else if(Integer.valueOf(stringRange.get(0)) - minVal <= maxVal - Integer.valueOf(stringRange.get(0)))
				stringRange.add(0, String.valueOf(minVal));
			else
				stringRange.add(String.valueOf(maxVal));
		}
	    for(String i : stringRange)
	    	list.add(Integer.valueOf(i));
		return list;
	}
	
	/**
	 * Parse a {@link String} that represents a double value either with ',' or '.' <br/>
	 * decimal value separator given the {@link Locale#getDefault()} category
	 * 
	 * @see http://stackoverflow.com/questions/4323599/best-way-to-parsedouble-with-comma-as-decimal-separator
	 * 
	 * @param value
	 * @return double value
	 */
	public Double getDouble(String value) {
	    if (value == null || value.isEmpty())
	    		throw new NumberFormatException(value);

	    try {
	    		return Double.valueOf(value);
		} catch (NumberFormatException e) {
			Locale theLocale = Locale.getDefault();
		    NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
		    try {
				return numberFormat.parse(value).doubleValue();
			} catch (ParseException e1) {
				String valueWithDot = value.replaceAll(",",".");
		        return Double.valueOf(valueWithDot);
			}
		}
	}

	/**
	 * Parse a {@link String} and retrieve any numerical match independently
	 * 
	 * @param trim
	 * @return
	 */
	public List<String> getNumber(String string) {
		return this.getNumbers(string, NumMatcher.getDefault().getMatch());
	}
	
	/**
	 * Parse a {@link String} and retrieves numerical values
	 * 
	 * @param string
	 * @param matcher
	 * @return
	 */
	public List<Number> getNumbers(String string, NumMatcher matcher){
		return this.getNumbers(string, matcher.getMatch()).stream().map(num -> this.parseNumbers(num))
				.collect(Collectors.toList());
	}

	/**
	 * Parse a string to return a Number either double or integer
	 * 
	 * @param stringVal
	 * @return
	 */
	public Number parseNumbers(String stringVal) {
		switch (this.getValueType(stringVal)) {
		case Continue:
			return Double.valueOf(getNumbers(stringVal, 
					NumMatcher.DOUBLE_MATCH_ENG.getMatch()).get(0));
		case Integer:
			return Integer.valueOf(getNumbers(stringVal,
					NumMatcher.INT_MATCH.getMatch()).get(0));
		default:
			return Double.NaN;
		}
	}
	
	/*
	 * 
	 */
	private List<String> getNumbers(String string, String match){
		List<String> numbers = new ArrayList<>();
		Pattern p = Pattern.compile(match);
		Matcher m = p.matcher(string);
		while (m.find())
		  numbers.add(m.group());
		return numbers;
	}

}
