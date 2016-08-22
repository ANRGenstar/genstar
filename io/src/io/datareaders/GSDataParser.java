package io.datareaders;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gospl.metamodel.attribut.DataType;
import io.datareaders.exception.GenstarIllegalRangedData;

/**
 * 
 * Object that can read data and: 
 * <p><ul>
 *  <li> {@link #getValueType(String)} give the implicit parsed data type ( {@link DataType} )
 *  <li> {@link #getRangedDoubleData(String, boolean)} or {@link #getRangedIntegerData(String, boolean)}
 * </ul><p>
 * Can give explicit values from string ranged value data representation
 * 
 * @author kevinchapuis
 *
 */
public class GSDataParser {
	
	public GSDataParser(){}
	
	/**
	 * Methods that retrieve value type ({@link DataType}) through string parsing <br/>
	 * Default type is {@value DataType#STRING}
	 * 
	 * @param value
	 * @return
	 */
	public DataType getValueType(String value){
		value = value.trim();
		if(value.matches("(\\-)?(\\d+\\.\\d+)(E(\\-)?\\d+)?") || value.matches("(\\-)?(\\d+\\,\\d+)(E(\\-)?\\d+)?"))
			return DataType.Double;
		if(value.matches("(\\-)?\\d+"))
			return DataType.Integer;
		if(Boolean.TRUE.toString().equalsIgnoreCase(value) || Boolean.FALSE.toString().equalsIgnoreCase(value))
			return DataType.Boolean;
		return DataType.String;
	}

	/**
	 * Parses double range values from string representation. There is no need for specifying <br/>
	 * any delimiter, although the method rely on proper {@link Double} values string encoding. <br/>
	 * If null value is true delimiter can't be the null "-" symbol
	 * 
	 * @param range
	 * @return {@link List} of min and max double values based on {@code range} string representation 
	 * @throws GenstarIllegalRangedData
	 */
	public List<Double> getRangedDoubleData(String range, boolean nullValue) throws GenstarIllegalRangedData{
		List<Double> list = new ArrayList<>();
		if(nullValue)
			range = range.replaceAll("^-?[\\d+\\.\\d+][E\\-\\d+]?", " ");
		else 
			range = range.replaceAll("[^\\d+\\.\\d+][E\\-\\d+]?", " ");
		List<String> stringRange = Arrays.asList(range.trim().split(" "));
		if(stringRange.isEmpty())
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
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
	 * @throws GenstarIllegalRangedData
	 */
	@Deprecated
	public List<Double> getRangedData(String range, boolean nullValue, Double minVal, Double maxVal) throws GenstarIllegalRangedData{
		List<Double> list = new ArrayList<>();
		if(nullValue)
			range = range.replaceAll("^-?[\\d+\\.\\d+][E\\-\\d+]?", " ");
		else 
			range = range.replaceAll("[^\\d+\\.\\d+][E\\-\\d+]?", " ");
		List<String> stringRange = Arrays.asList(range.trim().split(" "));
		if(stringRange.isEmpty())
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
		if (stringRange.size() == 1){
			if(minVal == null && maxVal == null)
				throw new GenstarIllegalRangedData("for implicit bounded values, either min or max value in argument must be set to a concret value !");
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
	 * @throws GenstarIllegalRangedData
	 */
	public List<Integer> getRangedIntegerData(String range, boolean nullValue) throws GenstarIllegalRangedData{
		List<Integer> list = new ArrayList<>();
		range = range.replaceAll(Pattern.quote("+"), " ");
		if(nullValue)
			range = range.replaceAll("[^-?\\d+]", " ");
		else
			range = range.replaceAll("[^\\d+]", " ");
		List<String> stringRange = new ArrayList<>(Arrays.asList(range.trim().split(" ")));
		if(stringRange.isEmpty())
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
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
	 * @throws GenstarIllegalRangedData
	 */
	@Deprecated
	public List<Integer> getRangedData(String range, boolean nullValue, Integer minVal, Integer maxVal) throws GenstarIllegalRangedData{
		List<Integer> list = new ArrayList<>();
		if(nullValue)
			range = range.replaceAll("[^-?\\d+]", " ");
		else
			range = range.replaceAll("[^\\d]+", " ");
		List<String> stringRange = new ArrayList<>(Arrays.asList(range.trim().split(" ")));
		if(stringRange.isEmpty())
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " does not represent any value");
		if(stringRange.size() > 2)
			throw new GenstarIllegalRangedData("The string ranged data " +range+ " has more than 2 (min / max) values");
		if(stringRange.size() == 1){
			if(minVal == null && maxVal == null)
				throw new GenstarIllegalRangedData("for implicit bounded values, either min or max value in argument must be set to a concret value !");
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

	    Locale theLocale = Locale.getDefault();
	    NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
	    Number theNumber;
	    try {
	        theNumber = numberFormat.parse(value);
	        return theNumber.doubleValue();
	    } catch (ParseException e) {
	        String valueWithDot = value.replaceAll(",",".");
	        return Double.valueOf(valueWithDot);
	    }
	}

	/**
	 * Parse a {@link String} and retrieves numerical values
	 * 
	 * @param trim
	 * @return
	 */
	public List<String> getNumber(String string) {
		List<String> numbers = new ArrayList<>();
		Pattern p = Pattern.compile("^-?[\\d+][\\.\\d+]?[E\\-\\d+]?");
		Matcher m = p.matcher(string);
		while (m.find()) {
		  numbers.add(m.group());
		}
		//String s = string.replaceAll("^-?[\\d+][\\.\\d+]?[E\\-\\d+]?", " ");
		//return Arrays.asList(s.trim().split(" "));
		return numbers;
	}

}
