package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Set;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.control.AControl;

public class SimpleMargin<T extends Number> extends AMargin<T> implements IMargin<DemographicAttribute<? extends IValue>, IValue, T> {

	protected SimpleMargin(DemographicAttribute<? extends IValue> controlAttribute, DemographicAttribute<? extends IValue> seedAttribute) {
		super(controlAttribute, seedAttribute);
	}

	@Override
	public Collection<Set<IValue>> getSeedMarginalDescriptors() {
		return super.marginalControl.keySet();
	}
	
	/*
	 * Protected setter to unsure safe construction
	 */
	protected void addMargin(Set<IValue> marginalDescriptor, AControl<T> control){
		super.marginalControl.put(marginalDescriptor, control);
	}

}
