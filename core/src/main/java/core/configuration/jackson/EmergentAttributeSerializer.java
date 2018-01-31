package core.configuration.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.EmergentAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EmergentAttributeSerializer extends StdSerializer<EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected EmergentAttributeSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
	}

	@Override
	public void serialize(
			EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> arg0,
			JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
