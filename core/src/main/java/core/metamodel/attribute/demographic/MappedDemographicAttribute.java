package core.metamodel.attribute.demographic;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

public abstract class MappedDemographicAttribute<V extends IValue, M extends IValue> extends DemographicAttribute<V> {

	private DemographicAttribute<M> referentAttribute;

	public MappedDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<M> referentAttribute) {
		super(name, valueSpace);
		this.referentAttribute = referentAttribute;
	}
	
	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referentAttribute);	
	}
	
	@Override
	public DemographicAttribute<M> getReferentAttribute(){
		return referentAttribute;
	}
	
	public abstract boolean addMappedValue(V mapTo, M mapWith);

}
