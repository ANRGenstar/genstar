package core.metamodel.attribute.geographic;

import java.util.Arrays;
import java.util.Collection;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.IValue;


/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class GeographicAttribute<V extends IValue> implements IAttribute<V> {

	private String name;
	private GeographicValueSpace<V> vs;

	public GeographicAttribute(GeographicValueSpace<V> vs, String name) {
		this.vs = vs;
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
	
	@SuppressWarnings("unchecked")
	public void setNoData(V... values) {
		Collection<V> noDataValues = Arrays.asList(values);
		vs.addExcludedValues(noDataValues);
	}

}
