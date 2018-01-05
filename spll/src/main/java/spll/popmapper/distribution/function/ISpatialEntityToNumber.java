package spll.popmapper.distribution.function;

import java.util.function.Function;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;

public interface ISpatialEntityToNumber<N extends Number> extends Function<AGeoEntity<? extends IValue>, N> {
	
	public void updateFunctionState(AGeoEntity<? extends IValue> entity);
	
}
