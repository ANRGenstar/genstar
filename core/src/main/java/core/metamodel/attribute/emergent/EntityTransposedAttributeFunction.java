package core.metamodel.attribute.emergent;

import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.aggregator.ITransposeValueFunction;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
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
		A extends IAttribute<IV>, IV extends IValue, RV extends IValue> 
			extends AEntityEmergentFunction<E, A, RV>
			implements IEntityEmergentFunction<E, A, RV> {
	
	private ITransposeValueFunction<IV, RV> tranposer; 

	public EntityTransposedAttributeFunction(IAttribute<RV> referent, 
			ITransposeValueFunction<IV, RV> transposer,
			IEntityChildFilter filter, IValue[] matches) {
		super(referent, filter, matches);
		this.tranposer = transposer;
	}

	@Override
	public RV apply(E entity, A attribute) {
		// TODO Auto-generated method stub
		return tranposer.transpose(this.getFilter().retain(entity.getChildren(), this.getMatchers())
				.stream().map(e -> attribute.getValueSpace()
						.getValue(entity.getValueForAttribute(attribute.getAttributeName()).getStringValue()))
				.collect(Collectors.toSet()), this.getReferentAttribute().getValueSpace());
	}
	
	

}
