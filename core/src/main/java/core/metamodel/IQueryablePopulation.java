package core.metamodel;

import java.util.Collection;
import java.util.Map;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public interface IQueryablePopulation<E extends IEntity<A>, A extends IAttribute<? extends IValue>> 
					extends IPopulation<E, A> {

	/**
	 * Returns the count of entities which have for this attribute 
	 * one of the given values
	 * @param attribute
	 * @param values
	 * @return
	 */
	public int getEntitiesHavingValues(A attribute, IValue ... values);
	
	public int getEntitiesHavingValues(Map<A,Collection<IValue>> attribute2values);

	//public Collection<E> getEntitiesHaving();
}
