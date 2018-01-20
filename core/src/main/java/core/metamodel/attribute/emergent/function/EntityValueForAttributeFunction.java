package core.metamodel.attribute.emergent.function;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EntityValueForAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
		 A extends IAttribute<? extends IValue>> 
	implements IEntityEmergentFunction<E, A, IValue> {

	private IValueSpace<IValue> vs;
	
	private IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter;
	private IValue[] matches;
	
	public EntityValueForAttributeFunction(
			IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter,
			IValue... matches) {
		this.filter = filter;
		this.matches = matches;
	}
	
	public EntityValueForAttributeFunction(IValueSpace<IValue> vs,
			IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> filter,
			IValue... matches) {
		this.setValueSpace(vs);
		this.filter = filter;
		this.matches = matches;
	}

	@Override
	public IValue apply(E entity, A attribute) {
		return filter.choseOne(entity.getChildren(), matches)
				.getValueForAttribute(attribute.getAttributeName());
	}

	@Override
	public IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> getFilter() {
		return this.filter;
	}

	@Override
	public IValueSpace<IValue> getValueSpace() {
		return this.vs;
	}

	@Override
	public void setValueSpace(IValueSpace<IValue> vs) {
		this.vs = vs;
	}

}
