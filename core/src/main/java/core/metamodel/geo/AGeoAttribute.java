package core.metamodel.geo;

import core.metamodel.IAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;


/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGeoAttribute<V extends IValue> implements IAttribute<V> {

	private String name;
	private IValueSpace<V> vs;

	public AGeoAttribute(IValueSpace<V> vs, String name) {
		this.vs = vs;
		this.name = name;
	}
	
	@Override
	public String getAttributeName() {
		return name;
	}
	
	@Override
	public IValueSpace<V> getValueSpace(){
		return vs;
	}


}
