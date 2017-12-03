package core.configuration.jackson;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PathSerializer extends StdSerializer<Path> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected PathSerializer() {
		this(null);
	}
	
	protected PathSerializer(Class<Path> t) {
		super(t);
	}

	@Override
	public void serialize(Path value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeString(value.toString());
		gen.writeEndObject();
	}

}
