package core.configuration.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.emergent.filter.IGSEntityTransposer;

public class EmergentTransposerSerializer extends StdSerializer<IGSEntityTransposer<?,?>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected EmergentTransposerSerializer() {
		this(null);
	}

	protected EmergentTransposerSerializer(Class<IGSEntityTransposer<?, ?>> t) {
		super(t);
	}

	@Override
	public void serialize(IGSEntityTransposer<?,?> arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void serializeWithType(IGSEntityTransposer<?, ?> transposer,
			JsonGenerator gen, SerializerProvider serializer, TypeSerializer typeSer) throws IOException {
		
		//gen.writeFieldName(EmergentAttribute.TRANSPOSER);
		gen.writeStartObject();
		gen.writeStringField(IGSEntityTransposer.TYPE, typeSer.getTypeIdResolver().idFromValue(transposer));
		
		gen.writeObjectField(IGSEntityTransposer.COMPARATOR, transposer.getComparator());
		
		/*
		gen.writeFieldName(IGSEntityTransposer.COMPARATOR);
		EntityComparatorSerializer ecs = new EntityComparatorSerializer();
		ecs.serialize(transposer.getComparator(), gen, serializer);
		*/
		
		gen.writeStringField(IGSEntityTransposer.MATCH_TYPE, transposer.getMatchType().toString());
		
		gen.writeObjectField(IGSEntityTransposer.MATCHERS, transposer.getMatcher());
		
		/*
		gen.writeFieldName(IGSEntityTransposer.MATCHERS);
		EntityMatcherSerializer ems = new EntityMatcherSerializer();
		ems.serializeWithType(transposer.getMatcher(), gen, serializer, typeSer);
		*/
		
		gen.writeEndObject();
		
		
	}

}
