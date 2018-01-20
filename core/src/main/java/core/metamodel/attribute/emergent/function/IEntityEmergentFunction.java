package core.metamodel.attribute.emergent.function;

import java.util.function.BiFunction;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public interface IEntityEmergentFunction<
		E extends IEntity<? extends IAttribute<? extends IValue>>, U, V extends IValue> 
	extends BiFunction<E, U, V> {
	
	public IEntityChildFilter<IEntity<? extends IAttribute<? extends IValue>>> getFilter();
	
	public IValueSpace<V> getValueSpace();
	
	public void setValueSpace(IValueSpace<V> vs);
	
}
