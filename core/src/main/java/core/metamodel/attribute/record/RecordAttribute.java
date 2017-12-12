package core.metamodel.attribute.record;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;

public class RecordAttribute<K extends IValue, V extends IValue> extends DemographicAttribute<NominalValue> {

	private DemographicAttribute<V> proxy;
	private DemographicAttribute<K> referent;
	
	private RecordValueSpace valuesSpace = null;

	public RecordAttribute(String name, DemographicAttribute<V> proxy, 
			DemographicAttribute<K> referent) {
		super(name);
		this.proxy = proxy;
		this.referent = referent;
	}
	
	@Override
	public DemographicAttribute<K> getReferentAttribute() {
		return referent;
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
	
	public IValueSpace<V> getProxyValueSpace(){
		return proxy.getValueSpace();
	}

}
