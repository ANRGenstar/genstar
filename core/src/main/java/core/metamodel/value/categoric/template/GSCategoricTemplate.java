package core.metamodel.value.categoric.template;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

/**
 * TODO: extends {@link Format} method to fit java interface requirement
 * 
 * @author kevinchapuis
 *
 */
public class GSCategoricTemplate extends Format {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean caseSensitive;
	private String[] regex;
	
	public GSCategoricTemplate(){
		this(true);
	}
	
	public GSCategoricTemplate(boolean caseSensitive, String... regex){
		this.caseSensitive = caseSensitive;
		this.regex = regex;
	}
	
	/**
	 * Change parameter String value to desired nominal space format. 
	 * For example if the string should be case sensitive or not, or reflect
	 * a date
	 * 
	 * @param value
	 * @return
	 */
	public String getFormatedString(String string){
		String formatedString = caseSensitive ? string : string.toLowerCase();
		Arrays.asList(regex).stream().forEach(r -> formatedString
				.replaceAll(caseSensitive ? r : r.toLowerCase(), ""));
		return this.format(string);
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
