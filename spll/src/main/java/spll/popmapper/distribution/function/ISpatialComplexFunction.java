package spll.popmapper.distribution.function;

import java.util.Collection;
import java.util.function.BiFunction;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;

public interface ISpatialComplexFunction<N extends Number> extends BiFunction<AGeoEntity<? extends IValue>, SpllEntity, N> {

	public void updateFunctionState(Collection<SpllEntity> entities, Collection<AGeoEntity<? extends IValue>> candidates);
	
	public void clear();
	
}
