package core.configuration.jackson.utilclass;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RangeAtt implements IAtt<RangeVal> {
	
	public static final String SEPARATOR = "separator";
	
	private Set<RangeVal> ranges;
	
	private RangeVal empty = new RangeVal(this, Double.NaN, Double.NaN);
	
	private String separator;
	
	private EDataType dataType = EDataType.Range;
	
	public RangeAtt() {this.ranges = new HashSet<>();}
	
	public RangeAtt(String separator){
		this.separator = separator;
		this.ranges = new HashSet<>();
	}

	@Override
	public RangeVal addValue(String values){
		RangeVal range = this.createRangeVal(values);
		ranges.add(range);
		return range;
	}
	
	@Override
	public Set<RangeVal> getValues(){
		return Collections.unmodifiableSet(ranges);
	}

	@Override
	public RangeVal getEmptyValue() {return empty;}
	
	@Override
	public void setEmptyValue(String values) {
		this.empty = this.createRangeVal(values);
	}
	
	@JsonProperty(RangeAtt.SEPARATOR)
	public String getSeparator() {return separator;}
	
	public void setSeparator(String separator) {this.separator = separator;}
	
	@JsonProperty(IAtt.TYPE)
	public EDataType getType() {return dataType;}
	
	// The inner creator
	private RangeVal createRangeVal(String values) {
		return new RangeVal(this, 
				Integer.decode(values.split(separator)[0]), 
				Integer.decode(values.split(separator)[1]));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((empty == null) ? 0 : empty.hashCode());
		result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
		result = prime * result + ((separator == null) ? 0 : separator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangeAtt other = (RangeAtt) obj;
		if (dataType != other.dataType)
			return false;
		if (empty == null) {
			if (other.empty != null)
				return false;
		} else if (!empty.equals(other.empty))
			return false;
		if (ranges == null) {
			if (other.ranges != null)
				return false;
		} else if (!ranges.equals(other.ranges))
			return false;
		if (separator == null) {
			if (other.separator != null)
				return false;
		} else if (!separator.equals(other.separator))
			return false;
		return true;
	}
	
	

}
