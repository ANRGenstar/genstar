package core.configuration.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.value.IValue;

public class RecordAttributeSerializer extends StdSerializer<RecordAttribute<? extends IAttribute<? extends IValue>, 
		? extends IAttribute<? extends IValue>>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RecordAttributeSerializer() {
		this(null);
	}
	
	protected RecordAttributeSerializer(
			Class<RecordAttribute<? extends IAttribute<? extends IValue>, ? extends IAttribute<? extends IValue>>> t) {
		super(t);
	}

	@Override
	public void serialize(
			RecordAttribute<? extends IAttribute<? extends IValue>, ? extends IAttribute<? extends IValue>> arg0,
			JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		
		
	}

}
