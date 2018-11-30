package core.metamodel.attribute.emergent.predicate;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IGSEntityTransposer;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class GSMatchPredicate<T> implements IGSPredicate<T> {

	@Override
	public Boolean apply(IEntity<? extends IAttribute<? extends IValue>> t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGSEntityTransposer<Boolean, T> getTransposer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransposer(IGSEntityTransposer<Boolean, T> transposer) {
		// TODO Auto-generated method stub
		
	}

}
