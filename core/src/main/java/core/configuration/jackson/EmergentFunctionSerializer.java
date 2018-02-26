package core.configuration.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.EmergentAttribute;
import core.metamodel.attribute.emergent.EntityAggregatedAttributeFunction;
import core.metamodel.attribute.emergent.IEntityEmergentFunction;
import core.metamodel.attribute.emergent.aggregator.IAggregatorValueFunction;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EmergentFunctionSerializer extends StdSerializer<
	IEntityEmergentFunction<? extends IEntity<? extends IAttribute<? extends IValue>>, ?, ? extends IValue>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected EmergentFunctionSerializer() {
		this(null);
	}
	
	protected EmergentFunctionSerializer(Class<IEntityEmergentFunction<? extends IEntity<? extends IAttribute<? extends IValue>>, ?, ? extends IValue>> t) {
		super(t);
	}

	@Override
	public void serialize(
			IEntityEmergentFunction<? extends IEntity<? extends IAttribute<? extends IValue>>, ?, ? extends IValue> arg0,
			JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		// DO NOTHING => delegate to #serializeWithType because of polymorphism
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void serializeWithType(
			IEntityEmergentFunction<? extends IEntity<? extends IAttribute<? extends IValue>>, ?, ? extends IValue> function,
			JsonGenerator gen, SerializerProvider serializer, TypeSerializer typeSer) throws IOException {
		
		String type = typeSer.getTypeIdResolver().idFromValue(function);
		
		gen.writeFieldName(EmergentAttribute.FUNCTION);
		gen.writeStartObject();
		
			// TYPE
			gen.writeStringField(IEntityEmergentFunction.TYPE, type);
			
			// FILTER
			gen.writeFieldName(IEntityEmergentFunction.FILTER);
			gen.writeStartObject();
			gen.writeStringField(IEntityChildFilter.TYPE, function.getFilter().getType().toString());
			gen.writeObjectField(IEntityChildFilter.COMPARATOR, function.getFilter().getComparator());
			gen.writeArrayFieldStart(IEntityEmergentFunction.MATCHERS);
			for(IValue match : function.getMatchers())
				gen.writeString(match.getStringValue());
			gen.writeEndArray();
			gen.writeEndObject();
			
			if(type.equals(EntityAggregatedAttributeFunction.SELF)) {
				gen.writeFieldName(EntityAggregatedAttributeFunction.AGGREGATOR);
				gen.writeStartObject();
				gen.writeStringField(IAggregatorValueFunction.TYPE, typeSer.getTypeIdResolver()
						.idFromValue(((EntityAggregatedAttributeFunction) function)
								.getAggregationFunction()));
				gen.writeEndObject();
			}
			
		gen.writeEndObject();
		
	}
	
}
