package core.metamodel.attribute.emergent;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public abstract class AEntityEmergentFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
		U, V extends IValue> 
	implements IEntityEmergentFunction<E, U, V> {

	private IAttribute<V> referent;
	
	private IEntityChildFilter filter;
	private IValue[] matches;
	
	public AEntityEmergentFunction(IAttribute<V> referent, IEntityChildFilter filter, IValue... matches) {
		this.setReferentAttribute(referent);
		if(filter == null)
			this.filter = EChildFilter.All.getFilter();
		else
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
	public IAttribute<V> getReferentAttribute() {
		return this.referent;
	}

	@Override
	public void setReferentAttribute(IAttribute<V> referent) {
		this.referent = referent;
	}
	
}
