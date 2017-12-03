package core.configuration.jackson.utilclass;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import core.configuration.jackson.utilclass.IAtt.EDataType;

public class AttDeserializer extends StdDeserializer<IAtt<? extends IVal>>  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AttDeserializer() {
		this(null);
	}
	
	public AttDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public IAtt<? extends IVal> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectMapper om = (ObjectMapper) jp.getCodec();
		JsonNode node = om.readTree(jp);
		switch (EDataType.valueOf(node.findValue(IAtt.TYPE).asText())) {
		case Range:
			Collection<String> values = node.findValue(IAtt.ENCAPS_SET)
				.findValues(IVal.VALUE).stream().map(val -> val.asText())
				.collect(Collectors.toSet());
			RangeAtt ra = new RangeAtt(node.get(RangeAtt.SEPARATOR).asText());
			values.stream().forEach(value -> ra.addValue(value));
			return ra;
		default:
			throw new IllegalArgumentException("Unkown attribute type");
		}
	}

}
