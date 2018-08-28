package core.metamodel.attribute.mapper.value;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.RecordValueSerializer;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;

/**
 * Enable the use of several string form for any {@link IValue}, those are called record and are 
 * encoded value. They are linked to one value of parametric type K that extends {@link IValue}. On the
 * contrary, one value can have several encoded form. 
 * 
 * @author kevinchapuis
 *
 * @param <K>
 */
@JsonTypeName(RecordValueMapper.SELF)
@JsonSerialize(using = RecordValueSerializer.class)
public class RecordValueMapper<K extends IValue> {
	
	public static final String SELF = "ENCODED VALUES";
	public static final String ATTRIBUTE_NAME = "ENCODE ATT";
	public static final String MAPPING = "MAPPING";
	
	private Map<NominalValue,K> mapper;
	
	private Attribute<NominalValue> self;
	
	/**
	 * Default constructor
	 */
	public RecordValueMapper() {
		this.mapper = new HashMap<>();
		this.self = AttributeFactory.getFactory().createNominalAttribute(ATTRIBUTE_NAME, new GSCategoricTemplate());
	}
	
	/**
	 * Constructor that add mapped record
	 * @param mapper
	 */
	public RecordValueMapper(Map<String,K> mapper){
		this();
		for(Entry<String, K> entry : mapper.entrySet()) {
			this.putMapping(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * Add a new record to encode K value given in parameter
	 * @param value
	 * @param records
	 */
	public void putMapping(K value, String... records) {
		for(String record : records) {
			this.mapper.put(this.self.getValueSpace().addValue(record), value);
		}
	}
	
	/**
	 * Get the related K value associated to a particular {@link IValue} record
	 * @param record
	 * @return
	 */
	public K getRelatedValue(IValue record) {
		return mapper.get(record);
	}
	
	/**
	 * Get the related K value associated to a particular String record
	 * @param record
	 * @return
	 */
	public K getRelatedValue(String record) {
		return mapper.get(this.self.getValueSpace().getValue(record));
	}
	
	/**
	 * Retrieve all possible String form of a {@link IValue} 
	 * @param value
	 * @return
	 */
	public Collection<? extends IValue> getRecords(K value){
		return mapper.keySet().stream().filter(k -> mapper.get(k).equals(value))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all the encoded values, called records
	 * @return
	 */
	public Collection<NominalValue> getRecords(){
		return Collections.unmodifiableSet(mapper.keySet());
	} 
	
}
