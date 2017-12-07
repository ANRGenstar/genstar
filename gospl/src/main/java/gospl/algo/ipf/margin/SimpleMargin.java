package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Set;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.control.AControl;

/**
 * Simple margin describe marginal that are equivalent for seed and control matrices. That means they either are
 * the exact same attribute or record linked attribute (a map of value among two attribute as a one-to-one relationship)
 * @author kevinchapuis
 *
 * @param <T>
 */
public class SimpleMargin<T extends Number> extends AMargin<T> implements IMargin<DemographicAttribute<? extends IValue>, IValue, T> {

	protected SimpleMargin(DemographicAttribute<? extends IValue> controlAttribute, 
			DemographicAttribute<? extends IValue> seedAttribute) {
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
