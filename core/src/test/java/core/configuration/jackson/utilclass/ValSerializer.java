package core.configuration.jackson.utilclass;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ValSerializer extends StdSerializer<IVal> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValSerializer() {
		this(null);
	}
	
	protected ValSerializer(Class<IVal> t) {
		super(t);
	}

	@Override
	public void serialize(IVal value, JsonGenerator gen, 
			SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(IVal.VALUE, value.getStringValue());
		gen.writeEndObject();
	}

}
