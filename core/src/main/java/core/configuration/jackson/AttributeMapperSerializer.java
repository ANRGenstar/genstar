package core.configuration.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.demographic.map.IAttributeMapper;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class AttributeMapperSerializer<K extends IValue, V extends IValue> 
extends StdSerializer<IAttributeMapper<K, V>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String MATCHER_SYM = " : ";
	public static final String SPLIT_SYM = "; ";

	protected AttributeMapperSerializer() {
		this(null);
	}

	protected AttributeMapperSerializer(Class<IAttributeMapper<K, V>> t) {
		super(t);
	}

	@Override
	public void serialize(IAttributeMapper<K, V> mapper, JsonGenerator gen,
			SerializerProvider provider) throws IOException {
		// DO NOTHING

	}

	@Override
	public void serializeWithType(IAttributeMapper<K, V> mapper, 
			JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {

		GSEnumDataType keyDataType = mapper.getRelatedAttribute().getValueSpace().getType();
		List<String> entryMapper = null;
		if(keyDataType.equals(GSEnumDataType.Order))
			entryMapper = this.getOrderedMapperEntries(mapper);
		else
			entryMapper = this.getMapperEntries(mapper);
		
		gen.writeStartObject();
		gen.writeStringField(typeSer.getPropertyName(), typeSer.getTypeIdResolver().idFromValue(mapper));
		gen.writeArrayFieldStart(IAttributeMapper.THE_MAP);
		for(String entry : entryMapper)
			gen.writeString(entry);
		gen.writeEndArray();
		gen.writeEndObject();
	}

	/*
	 * Transpose mapper to a List of String key / value binding
	 */
	private List<String> getMapperEntries(IAttributeMapper<K, V> mapper) {
		return mapper.getRawMapper().entrySet().stream().map(entry -> 
				this.getStringValues(entry.getKey()) + 
					AttributeMapperSerializer.MATCHER_SYM + this.getStringValues(entry.getValue()))
				.collect(Collectors.toList());
	}

	/*
	 * Transpose mapper to a List of ordered String key / value binding
	 */
	private List<String> getOrderedMapperEntries(IAttributeMapper<K, V> mapper) {
		List<IValue> orderedValues = new ArrayList<>();
		mapper.getRelatedAttribute().getValueSpace()
					.getValues().iterator().forEachRemaining(orderedValues::add);
		return mapper.getRawMapper().entrySet().stream().map(entry -> 
					this.getStringValues(orderedValues.stream()
						.filter(ok -> entry.getKey().contains(ok))
						.collect(Collectors.toList())) + 
					AttributeMapperSerializer.MATCHER_SYM + this.getStringValues(entry.getValue()))
				.collect(Collectors.toList());
	}

	/*
	 * Inner private Set of value writer
	 */
	private String getStringValues(Collection<? extends IValue> collection) {
		if(collection.isEmpty())
			return "";
		if(collection.size() == 1)
			return collection.iterator().next().getStringValue();
		return collection.stream().map(IValue::getStringValue)
			.collect(Collectors.joining(AttributeMapperSerializer.SPLIT_SYM));
	}

}
