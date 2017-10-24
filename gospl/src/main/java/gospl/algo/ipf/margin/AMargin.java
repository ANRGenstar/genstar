package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.algo.ipf.AGosplIPF;
import gospl.distribution.matrix.control.AControl;

/**
 * Represent a marginal descriptor for a dimension of an abstract {@link AGosplIPF}
 * <p>
 * <ul>
 *  <li> Stores marginal descriptors binding control to seed coordinate
 *  <li> Stores marginal control, i.e. control total associated to a specific control marginal descriptor
 * </ul>
 * <p>
 * 
 * @author kevinchapuis
 *
 */
public abstract class AMargin<T extends Number> implements IMargin<DemographicAttribute<? extends IValue>, IValue, T> {

	private DemographicAttribute<? extends IValue> controlAttribute;
	private DemographicAttribute<? extends IValue> seedAttribute;
	
	protected Map<Set<IValue>, AControl<T>> marginalControl;
	
	/*
	 * protected constructor to unsure safe initialization
	 */
	protected AMargin(DemographicAttribute<? extends IValue> controlAttribute, DemographicAttribute<? extends IValue> seedAttribute){
		this.controlAttribute = controlAttribute;
		this.seedAttribute = seedAttribute;
		this.marginalControl = new HashMap<>();
	}
	
	@Override
	public Collection<AControl<T>> getControls(){
		return Collections.unmodifiableCollection(marginalControl.values());
	}
	
	@Override
	public AControl<T> getControl(Set<IValue> seedMargin) {
		return marginalControl.get(seedMargin);
	}
	
	@Override
	public DemographicAttribute<? extends IValue> getControlDimension() {
		return controlAttribute;
	}
	
	@Override
	public DemographicAttribute<? extends IValue> getSeedDimension() {
		return seedAttribute;
	}

	@Override
	public int size() {
		return marginalControl.size();
	}

}
