package core.metamodel.attribute.emergent.function;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public abstract class AEntityEmergentFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
		U, V extends IValue> 
	implements IEntityEmergentFunction<E, U, V> {

	private IValueSpace<V> vs;
	
	private IEntityChildFilter filter;
	private IValue[] matches;
	
	public AEntityEmergentFunction(IEntityChildFilter filter, IValue... matches) {
		this.filter = filter;
		this.matches = matches;
	}
	
	public AEntityEmergentFunction(IValueSpace<V> vs, IEntityChildFilter filter, IValue... matches) {
		this.setValueSpace(vs);
		this.filter = filter;
		this.matches = matches;
	}
	
	@Override
	public IEntityChildFilter getFilter() {
		return this.filter;
	}
	
	@Override
	public void setFilter(IEntityChildFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public IValue[] getMatchers(){
		return matches;
	}
	
	@Override
	public void setMatchers(IValue... matchers) {
		this.matches = matchers;
	}

	@Override
	public IValueSpace<V> getValueSpace() {
		return this.vs;
	}

	@Override
	public void setValueSpace(IValueSpace<V> vs) {
		this.vs = vs;
	}
	
}
