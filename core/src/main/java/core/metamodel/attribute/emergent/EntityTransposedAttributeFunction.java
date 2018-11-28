package core.metamodel.attribute.emergent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.transposer.ITransposeValueFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Transpose a set of attribute value to one emergent attribute value given the provided {@link ITransposeValueFunction} called {@code transposer}
 * 
 * TODO: make it functional - but still have problem on how to serialize/deserialize because of too much parametrics
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <A>
 * @param <IV>
 * @param <RV>
 */
public class EntityTransposedAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>, 
		U, RV extends IValue> extends AEntityEmergentFunction<E, U, RV> {
	
	private ITransposeValueFunction<IValue, RV> transposer; 

	public EntityTransposedAttributeFunction(IAttribute<RV> referent, 
			ITransposeValueFunction<IValue, RV> transposer,
			IEntityChildFilter filter, IValue[] matches) {
		super(referent, filter, matches);
		this.transposer = transposer;
	}

	@Override
	public RV apply(E entity, U useless) {
		
		Collection<IValue> toTranspose = this.getFilter().retain(entity.getChildren(), this.getMatchers())
				.stream().flatMap(e -> e.getValues().stream())
				.collect(Collectors.toSet()); 
		toTranspose.addAll(entity.getValues());
		
		return transposer.transpose(toTranspose, 
				this.getReferentAttribute().getValueSpace());
	}

	@Override
	public Collection<Set<IValue>> reverse(RV value, U useless) {
		// TODO Auto-generated method stub
		return Collections.singleton(new HashSet<>(transposer.reverse(value, null)));
	}
	
	

}
