package core.configuration.jackson.utilclass.dummy;

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
	  @Type(value = Lion.class, name = "LION")
	})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property=IAnimal.NAME)
@JsonDeserialize(using=AnimalDeserializer.class)
public interface IAnimal {
	
	public static final String NAME = "NAME"; 

	@JsonProperty(IAnimal.NAME)
	public String getName();
	public void setName(String name);
	
}
