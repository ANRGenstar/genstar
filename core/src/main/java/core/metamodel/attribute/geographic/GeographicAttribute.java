package core.metamodel.attribute.geographic;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;


/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeName(GeographicAttribute.SELF)
public class GeographicAttribute<V extends IValue> implements IAttribute<V> {

	public static final String SELF = "GEOGRAPHIC ATTRIBUTE";
	
	private String name;
	private GeographicValueSpace<V> vs;

	protected GeographicAttribute(String name) {
		this.name = name;
	}
	
	@Override
	public String getAttributeName() {
		return name;
	}
	
	@Override
	public GeographicValueSpace<V> getValueSpace(){
		return vs; 
	} 
	 
	@Override
	public void setValueSpace(IValueSpace<V> valueSpace) {
		this.vs = new GeographicValueSpace<>(valueSpace);
		
	}
	
	@SuppressWarnings("unchecked")
	public void setNoData(V... values) {
		vs.addExcludedValues(Arrays.asList(values));
	}

}
