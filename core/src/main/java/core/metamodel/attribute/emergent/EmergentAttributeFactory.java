package core.metamodel.attribute.emergent;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.function.EntityAggregatedAttributeFunction;
import core.metamodel.attribute.emergent.function.EntityCountFunction;
import core.metamodel.attribute.emergent.function.EntityValueForAttributeFunction;
import core.metamodel.attribute.emergent.function.IEntityEmergentFunction;
import core.metamodel.attribute.emergent.function.aggregator.BooleanAggValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.BooleanAggValueFunction.BooleanAggregationStyle;
import core.metamodel.attribute.emergent.function.aggregator.ContinueSumValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.IAggregateValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.IntegerSumValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.NominalAggValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.OrderedAggValueFunction;
import core.metamodel.attribute.emergent.function.aggregator.RangeAggValueFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerSpace;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeSpace;
import core.metamodel.value.numeric.RangeValue;

/**
 * The factory to build emergent attribute
 *  
 * @author kevinchapuis
 *
 */
public class EmergentAttributeFactory {

	private static EmergentAttributeFactory eaf = new EmergentAttributeFactory();

	private EmergentAttributeFactory(){}

	public static EmergentAttributeFactory getInstance() {
		return eaf;
	}

	// -------------------------------------------- //

	/**
	 * Main generic methods to create emergent attribute from a emergent function
	 * @param name
	 * @param entity
	 * @param emergentFunction
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, U, V extends IValue> 
	EmergentAttribute<V, E, U> getAttribute(String name, IEntityEmergentFunction<E, U, V> emergentFunction) {
		EmergentAttribute<V, E, U> attribute = new EmergentAttribute<>(name);
		attribute.setValueSpace(emergentFunction.getValueSpace());
		attribute.setFunction(emergentFunction);
		return attribute;
	}

	/**
	 * Attribute that will count the number of sub-entities. As for any emergent attribute
	 * it is possible to filter agent before counting; makes it possible to number sub-entities
	 * that match any predicate
	 * 
	 * @param name
	 * @param entity
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>> 
	EmergentAttribute<IntegerValue, E, Object> getCountAttribute(String name, 
			IEntityChildFilter filter, IValue... matches) {
		EmergentAttribute<IntegerValue, E, Object> attribute = new EmergentAttribute<>(name);
		attribute.setValueSpace(new IntegerSpace(attribute));
		attribute.setFunction(new EntityCountFunction<E, Object>(attribute.getValueSpace(), filter, matches));
		return attribute;
	}

	/**
	 * Attribute that will retrieve the value of one particular sub-entity attribute. This is done
	 * by providing a filter function which drive the selection process (filter and sort sub entities)
	 * and then retrieve associated value for the given attribute
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, V extends IValue, A extends IAttribute<V>> 
	EmergentAttribute<V, E, A> getValueOfAttribute(String name, A attribute,  
			IEntityChildFilter filter, IValue... matches) {
		EmergentAttribute<V, E, A> eAttribute = new EmergentAttribute<>(name);
		eAttribute.setValueSpace(attribute.getValueSpace());
		eAttribute.setFunction(new EntityValueForAttributeFunction<E, A, V>(attribute.getValueSpace(), filter, matches));
		return eAttribute;
	}
	
	/**
	 * Attribute that will aggregate several value to a unique value: for example, it can be used to sum up
	 * the revenue of all individual of a household (and works even if it is integer, continuous or range value) 
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param aggFunction
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, V extends IValue, A extends IAttribute<V>, R extends IValue> 
	EmergentAttribute<R, E, A> getAggregatedValueOfAttribute(String name, A attribute,  
			IAggregateValueFunction<R, V> aggFunction, IEntityChildFilter filter, IValue... matches) {
		EmergentAttribute<R, E, A> eAttribute = new EmergentAttribute<>(name);
		eAttribute.setValueSpace(aggFunction.getValueSpace());
		eAttribute.setFunction(new EntityAggregatedAttributeFunction<E, A, V, R>(aggFunction, filter, matches));
		return eAttribute;
	}

	// ------------------------------ Default aggregated emergent attribute creation ------------------------------ //
	
	/**
	 * Boolean value aggregation with style ;)
	 * 
	 * @see BooleanAggValueFunction
	 * @see BooleanAggregationStyle
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<BooleanValue>>
	EmergentAttribute<BooleanValue, E, A> getAggregatedBooleanAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		return this.getAggregatedValueOfAttribute(name, attribute, 
				new BooleanAggValueFunction(attribute.getValueSpace()), filter, matches);
	}
	
	/**
	 * Integer value sum
	 * 
	 * @see IntegerSumValueFunction
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<IntegerValue>>
	EmergentAttribute<IntegerValue, E, A> getAggregatedIntegerAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		return this.getAggregatedValueOfAttribute(name, attribute, 
				new IntegerSumValueFunction(attribute.getValueSpace()), filter, matches);
	}
	
	/**
	 * Continuous value sum
	 * 
	 * @see ContinueSumValueFunction
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<ContinuousValue>>
	EmergentAttribute<ContinuousValue, E, A> getAggregatedContinuousAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		return this.getAggregatedValueOfAttribute(name, attribute, 
				new ContinueSumValueFunction(attribute.getValueSpace()), filter, matches);
	}
	
	/**
	 * Nominal value aggregation as a concatenation of string with parametric separator
	 * 
	 * @see NominalAggValueFunction
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<NominalValue>>
	EmergentAttribute<NominalValue, E, A> getAggregatedNominalAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		return this.getAggregatedValueOfAttribute(name, attribute, 
				new NominalAggValueFunction(attribute.getValueSpace()), filter, matches);
	}
	
	/**
	 * Ordered value aggregation based on sum of weights of ordered value to compute new ordering
	 * 
	 * @see OrderedAggValueFunction
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<OrderedValue>>
	EmergentAttribute<OrderedValue, E, A> getAggregatedOrderedAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		EmergentAttribute<OrderedValue, E, A> eAttribute = new EmergentAttribute<>(name);
		eAttribute.setValueSpace(new OrderedSpace(eAttribute, new GSCategoricTemplate()));
		eAttribute.setFunction(new EntityAggregatedAttributeFunction<E, A, OrderedValue, OrderedValue>(
				new OrderedAggValueFunction(eAttribute), filter, matches));
		return eAttribute;
	}
	
	/**
	 * Range value sum of bounds
	 * 
	 * @see RangeAggValueFunction
	 * 
	 * @param name
	 * @param entity
	 * @param attribute
	 * @param filter
	 * @param matches
	 * @return
	 */
	public <E extends IEntity<? extends IAttribute<? extends IValue>>, A extends IAttribute<RangeValue>>
	EmergentAttribute<RangeValue, E, A> getAggregatedRangeAttribute(String name, A attribute,
			IEntityChildFilter filter, IValue... matches){
		EmergentAttribute<RangeValue, E, A> eAttribute = new EmergentAttribute<>(name);
		RangeSpace rs = new RangeSpace(eAttribute, attribute.getValueSpace().getValues()
				.iterator().next().getValueSpace().getRangeTemplate());
		eAttribute.setValueSpace(rs);
		eAttribute.setFunction(new EntityAggregatedAttributeFunction<E, A, RangeValue, RangeValue>(
				new RangeAggValueFunction(rs), filter, matches));
		return eAttribute;
	}
}
