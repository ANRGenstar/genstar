package core.configuration.jackson.utilclass.dummy;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class AnimalDeserializer extends StdDeserializer<IAnimal> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected AnimalDeserializer() {
		this(null);
	}

	protected AnimalDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public IAnimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectMapper om = (ObjectMapper) p.getCodec();
		JsonNode node = om.readTree(p);
		switch (node.findValue(IAnimal.NAME).asText()) {
		case "michel":
			return new Lion("michel");
		default:
			throw new IllegalArgumentException("Unkown attribute type");
		}
	}

}
