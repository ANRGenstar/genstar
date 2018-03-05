package core.configuration.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.MappedAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EmergentAttributeSerializer extends StdSerializer<EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String EMPTY_REFERENT = "NONE";

	public EmergentAttributeSerializer() {
		this(null);
	}
	
	protected EmergentAttributeSerializer(Class<EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?>> t) {
		super(t);
	}

	@Override
	public void serialize(
			EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> attribute,
			JsonGenerator gen, SerializerProvider provider) throws IOException {
		// DO NOTHING
	}
	
	@Override
	public void serializeWithType(EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> attribute,
			JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
		
		gen.writeStartObject();
		gen.writeFieldName(typeSer.getTypeIdResolver().idFromValue(attribute));
		gen.writeStartObject();
		gen.writeStringField(IAttribute.NAME, attribute.getAttributeName());
		gen.writeStringField(MappedAttribute.REF, attribute.getReferentAttribute().getAttributeName());
		EmergentFunctionSerializer fs = new EmergentFunctionSerializer();
		fs.serializeWithType(attribute.getFunction(), gen, serializers, typeSer);
		gen.writeEndObject();
		gen.writeEndObject();
		
	}

}
