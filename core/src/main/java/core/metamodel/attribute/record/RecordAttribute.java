package core.metamodel.attribute.record;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;

public class RecordAttribute<R extends IAttribute<? extends IValue>, 
	P extends IAttribute<V>, V extends IValue> implements IAttribute<NominalValue> {

	private final P proxy;
	private final R referent;
	
	private RecordValueSpace valuesSpace = null;
	
	private final String name;

	public RecordAttribute(String name, P proxy, R referent) {
		this.name = name;
		this.proxy = proxy;
		this.referent = referent;
	}
	
	@Override
	public String getAttributeName() {
		return name;
	}
	
	@Override
	public IValueSpace<NominalValue> getValueSpace() {
		if(valuesSpace == null)
			valuesSpace = new RecordValueSpace(this, new GSCategoricTemplate(), this.getAttributeName());
		return valuesSpace;
	}

	
	@Override
	public void setValueSpace(IValueSpace<NominalValue> valueSpace) {
		// DO NOTHING
	}
	
	/**
	 * Get the referent attribute to which record will be linked
	 * @return
	 */
	public R getReferentAttribute() {
		return referent;
	}
	
	/**
	 * Get the proxy attribute that store record relationship. 
	 * 
	 * @return
	 */
	public P getProxyAttribute(){
		return proxy;
	}

}
