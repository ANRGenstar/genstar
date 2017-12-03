package core.configuration.jackson.utilclass;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ValSerializer.class)
public interface IVal {
	
	public static final String VALUE = "input";
	
	public IAtt<? extends IVal> getAttribute();
	
	@JsonProperty(IVal.VALUE)
	public String getStringValue();
	
}
