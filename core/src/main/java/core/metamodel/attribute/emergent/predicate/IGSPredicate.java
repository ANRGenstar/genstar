package core.metamodel.attribute.emergent.predicate;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IGSEntityTransposer;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXISTING_PROPERTY,
	      property = IGSPredicate.TYPE
	      )
@JsonSubTypes({
  @JsonSubTypes.Type(value = GSMatchPredicate.class)
})
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public interface IGSPredicate<T> extends Function<IEntity<? extends IAttribute<? extends IValue>>, Boolean> {

	public static final String TYPE = "PREDICATE TYPE";
	public static final String PREDICATE_TRANSPOSER = "PREDICATE SELECTOR";
	
	@JsonProperty(PREDICATE_TRANSPOSER)
	public IGSEntityTransposer<Boolean, T> getTransposer();
	
	@JsonProperty(PREDICATE_TRANSPOSER)
	public void setTransposer(IGSEntityTransposer<Boolean, T> transposer);
	
}
