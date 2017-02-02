package gospl.algo.ipf.margin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.control.AControl;

/**
 * @see IMargin
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public class ComplexMargin<T extends Number> extends AMargin<T> implements IMargin<APopulationAttribute, APopulationValue, T> {

	private Map<Set<APopulationValue>, Set<APopulationValue>> marginalDescriptors;
	
	protected ComplexMargin(APopulationAttribute controlAttribute, APopulationAttribute seedAttribute) {
		super(controlAttribute, seedAttribute);
		this.marginalDescriptors = new HashMap<>();
	}
	
	/**
	 * Add a complex marginal descriptor: 
	 * <ul>
	 * <li> a control descriptor
	 * <li> a seed descriptor
	 * <li> a control number
	 * </ul>
	 * <p>
	 * 
	 * @param controlDescriptor
	 * @param control
	 * @param seedDescriptor
	 */
	public void addMarginal(Set<APopulationValue> controlDescriptor, AControl<T> control,
			Set<APopulationValue> seedDescriptor){
		marginalDescriptors.put(controlDescriptor, seedDescriptor);
		super.marginalControl.put(seedDescriptor, control);
	}
	
	@Override
	public Collection<Set<APopulationValue>> getSeedMarginalDescriptors() {
		return marginalDescriptors.values();
	}
	
	public Set<APopulationValue> getSeedMarginalDescriptor(Set<APopulationValue> controlDescriptor){
		return marginalDescriptors.get(controlDescriptor);
	}
	
}
