package gospl.algo.ipf.margin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
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
public abstract class AMargin<T extends Number> implements IMargin<APopulationAttribute, APopulationValue, T> {

	private APopulationAttribute controlAttribute;
	private APopulationAttribute seedAttribute;
	
	protected Map<Set<APopulationValue>, AControl<T>> marginalControl;
	
	/*
	 * protected constructor to unsure safe initialization
	 */
	protected AMargin(APopulationAttribute controlAttribute, APopulationAttribute seedAttribute){
		this.controlAttribute = controlAttribute;
		this.seedAttribute = seedAttribute;
		this.marginalControl = new HashMap<>();
	}
	
	@Override
	public AControl<T> getControl(Set<APopulationValue> seedMargin) {
		return marginalControl.get(seedMargin);
	}
	
	@Override
	public APopulationAttribute getControlDimension() {
		return controlAttribute;
	}
	
	@Override
	public APopulationAttribute getSeedDimension() {
		return seedAttribute;
	}

	@Override
	public int size() {
		return marginalControl.size();
	}

}
