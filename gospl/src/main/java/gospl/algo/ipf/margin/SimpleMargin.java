package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Set;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.control.AControl;

public class SimpleMargin<T extends Number> extends AMargin<T> implements IMargin<APopulationAttribute, APopulationValue, T> {

	protected SimpleMargin(APopulationAttribute controlAttribute, APopulationAttribute seedAttribute) {
		super(controlAttribute, seedAttribute);
	}

	@Override
	public Collection<Set<APopulationValue>> getSeedMarginalDescriptors() {
		return super.marginalControl.keySet();
	}
	
	/*
	 * Protected setter to unsure safe construction
	 */
	protected void addMargin(Set<APopulationValue> marginalDescriptor, AControl<T> control){
		super.marginalControl.put(marginalDescriptor, control);
	}

}
