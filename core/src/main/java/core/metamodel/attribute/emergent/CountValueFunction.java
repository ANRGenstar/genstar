package core.metamodel.attribute.emergent;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.util.data.GSDataParser;

@JsonTypeName(CountValueFunction.SELF)
public class CountValueFunction<E extends IEntity<? extends IAttribute<? extends IValue>>, V extends IValue> 
	implements IGSValueFunction<Collection<E>, V> {

	public final static String SELF = "EMERGENT COUNT FUNCTION";
	
	private Map<Integer, V> mapping;
	private Attribute<V> referent;
	
	public CountValueFunction(Attribute<V> referent, Map<Integer, V> mapping) {
		this.referent = referent;
		this.mapping = checkMapping(mapping);
	}
	
	public CountValueFunction(Attribute<V> referent) {
		this.referent = referent;
	}
	
	@Override
	public V apply(Collection<E> entity) {
		if(mapping == null)
			return referent.getValueSpace().getValue(Integer.toString(entity.size()));
		return mapping.get(entity.size());
	}

	@Override
	public Attribute<V> getReferent() {
		return this.referent;
	}

	@Override
	public void setReferent(Attribute<V> referent) {
		this.referent = referent;
	}

	@JsonProperty(IGSValueFunction.MAPPING)
	public Map<Integer, V> getMapping() {
		return Collections.unmodifiableMap(this.mapping);
	}

	@JsonProperty(IGSValueFunction.MAPPING)
	public void setMapping(Map<Integer, V> mapping) {
		this.mapping = this.checkMapping(mapping);
	}
	
	/*
	 * Check mapping between integer and value v contains in referent attribute
	 */
	private Map<Integer, V> checkMapping(Map<?, V> mapping) {
		GSDataParser gsdp = new GSDataParser();
		if(mapping.values().stream().anyMatch(v -> !referent.getValueSpace().contains(v)))
			throw new IllegalArgumentException("Trying to setup mapping with inconsistent value mapping :"
					+mapping.values().stream().filter(v -> !referent.getValueSpace().contains(v))
					.map(IValue::getStringValue).collect(Collectors.joining(";")));
		if(mapping.keySet().stream().anyMatch(k -> !(k instanceof Integer)))
			throw new IllegalArgumentException("Trying to setup count mapping with not integer key :"
					+mapping.keySet().stream().filter(k -> !gsdp.getValueType(k.toString()).isNumericValue())
					.map(k -> k.toString()).collect(Collectors.joining(";")));
		return mapping.keySet().stream().collect(Collectors.toMap(
				key -> Integer.valueOf(key.toString()), 
				key -> mapping.get(key)));
	}

}
