package core.metamodel.attribute.emergent;

import java.util.Collection;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.predicate.IGSPredicate;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class CompositeValueFunction<V extends IValue> implements 
	IGSValueFunction<Collection<IEntity<? extends IAttribute<? extends IValue>>>, V> {

	private Attribute<V> referent; 
	
	Map<IGSPredicate, V> predicates;
	
	@Override
	public V apply(Collection<IEntity<? extends IAttribute<? extends IValue>>> entities) {
		return predicates.stream().filter(predicate -> predicate.verify(entities));
	}

	@Override
	public Attribute<V> getReferent() {
		return referent;
	}

	@Override
	public void setReferent(Attribute<V> referent) {
		this.referent = referent;
	}



}
