package core.metamodel.attribute.emergent.function;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;

public class EntityCountFunction<E extends IEntity<? extends IAttribute<? extends IValue>>, U> 
	implements IEntityEmergentFunction<E, U, IntegerValue> {

	private IValueSpace<IntegerValue> vs;
	
	private IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter;
	private IValue[] matches;
	
	public EntityCountFunction(
			IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter,
			IValue... matches) {
		this.filter = filter;
		this.matches = matches;
	}
	
	public EntityCountFunction(IValueSpace<IntegerValue> vs,
			IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter,
			IValue... matches) {
		this.setValueSpace(vs);
		this.filter = filter;
		this.matches = matches;
	}

	@Override
	public IntegerValue apply(E entity, U useless) {
		return this.getValueSpace().proposeValue(
				Integer.toString(filter != null && matches != null ? 
					filter.retain(entity.getChildren(), matches).size() : 
						entity.getCountChildren())
				);
	}

	@Override
	public IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> getFilter() {
		return filter;
	}

	@Override
	public IValueSpace<IntegerValue> getValueSpace() {
		return this.vs;
	}

	@Override
	public void setValueSpace(IValueSpace<IntegerValue> vs) {
		this.vs = vs;
	}

}
