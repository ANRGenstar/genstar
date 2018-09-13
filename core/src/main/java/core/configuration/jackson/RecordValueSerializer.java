package core.configuration.jackson;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.mapper.value.EncodedValueMapper;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.NominalValue;

/**
 * Transpose {@link EncodedValueMapper} into Json 
 * 
 * @author kevinchapuis
 *
 * @param <K>
 */
public class RecordValueSerializer<K extends IValue> extends StdSerializer<EncodedValueMapper<K>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String MATCH_SYMBOL = AttributeMapperSerializer.MATCHER_SYM;
	public static final String SPLIT_SYMBOL = AttributeMapperSerializer.SPLIT_SYM;

	protected RecordValueSerializer() {
		this(null);
	}
	
	protected RecordValueSerializer(Class<EncodedValueMapper<K>> t) {
		super(t);
	}

	@Override
	public void serialize(EncodedValueMapper<K> value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serializeWithType(EncodedValueMapper<K> mapper,
			JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
		gen.writeFieldName(typeSer.getPropertyName());
		gen.writeStartObject();
		gen.writeArrayFieldStart(EncodedValueMapper.MAPPING);
		for(String entry : this.getRecordList(mapper)) {
			gen.writeString(entry);
		}
		gen.writeEndArray();
		gen.writeEndObject();
	}
	
	/*
	 * return a list view of a record value mapper (a list of pair)
	 */
	private List<String> getRecordList(EncodedValueMapper<K> mapper){
		Map<K, Collection<NominalValue>> res = new HashMap<>();
		for(NominalValue rec : mapper.getRecords()) {
			K val = mapper.getRelatedValue(rec);
			if(!res.containsKey(val))
				res.put(val, new HashSet<>());
			res.get(val).add(rec);
		}
		
		return res.entrySet().stream().map(e -> e.getKey().getStringValue()
				+RecordValueSerializer.MATCH_SYMBOL
				+e.getValue().stream().map(r -> r.getStringValue())
					.collect(Collectors.joining(RecordValueSerializer.SPLIT_SYMBOL)))
				.collect(Collectors.toList());
	}
	
}
