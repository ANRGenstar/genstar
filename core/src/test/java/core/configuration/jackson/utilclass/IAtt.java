package core.configuration.jackson.utilclass;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use=Id.NAME, include=As.WRAPPER_OBJECT)
@JsonSubTypes({ 
	  @Type(value = RangeAtt.class, name = "RANGE_ATT") 
	})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property=IAtt.TYPE)
@JsonDeserialize(using = AttDeserializer.class)
public interface IAtt<V extends IVal> {
	
	public enum EDataType {
		Range(RangeAtt.class);
		private Class<?> clazz;
		private EDataType(Class<?> clazz) {this.clazz = clazz;}
		public Class<?> getClazz() {return clazz;}
	}
	
	public static final String TYPE = "TYPE";
	public static final String ENCAPS_SET = "values";
	
	public V addValue(String value);
	@JsonProperty(IAtt.ENCAPS_SET)
	public Set<V> getValues();
	
	public V getEmptyValue();
	public void setEmptyValue(String value);
}
