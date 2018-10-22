package core.metamodel.attribute.mapper.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.attribute.MappedAttribute;
import core.metamodel.attribute.mapper.IAttributeMapper;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeSpace;
import core.metamodel.value.numeric.RangeValue;
import core.metamodel.value.numeric.RangeValue.RangeBound;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.random.GenstarRandomUtils;

/**
 * Map an ordered value space with numerical value, including {@link IntegerValue}, 
 * {@link ContinuousValue} and {@link RangeValue}. One mapper can bind one ordered value
 * with any of the three. 
 * 
 * @author kevinchapuis
 *
 */
public class NumericValueMapper implements IAttributeMapper<IValue, OrderedValue> {
	
	private Map<IValue, OrderedValue> mapper;

	private MappedAttribute<IValue, OrderedValue> relatedAttribute;

	private static Attribute<ContinuousValue> CA = AttributeFactory.createNIU(ContinuousValue.class);
	private static Attribute<IntegerValue> IA = AttributeFactory.createNIU(IntegerValue.class);
	private static Attribute<RangeValue> RA = AttributeFactory.createNIU(RangeValue.class);
	private static GSDataParser GSDP = new GSDataParser();
	
	public NumericValueMapper() {
		this.mapper = new HashMap<>();
	}
	
	// --------
	
	/**
	 * Add a map between nominal value and range from {@code bValue} to {@code tValue}
	 *  
	 * @param nominal
	 * @param bValue
	 * @param tValue
	 */
	public void add(OrderedValue nominal, Number bValue, Number tValue) {
		mapper.put(RA.getValueSpace().proposeValue(bValue.toString()+" : "+tValue.toString()), nominal);
	}
	
	/**
	 * Add a map between nominal value and range from one numerical value and defined lower/upper bound.
	 * The {@link RangeBound} {@code rb} argument describes what should be the other value, 
	 * either {@link RangeBound#LOWER} or {@link RangeBound#UPPER}.
	 * 
	 * @param nominal
	 * @param value
	 * @param rb
	 */
	public void add(OrderedValue nominal, Number value, RangeBound rb) {
		String theString = rb.equals(RangeBound.UPPER) ? 
				value.toString()+" : "+((RangeSpace)RA.getValueSpace()).getMax()
				: ((RangeSpace)RA.getValueSpace()).getMin()+" : "+value.toString();
		mapper.put(RA.getValueSpace().proposeValue(theString), nominal);
	}
	
	/**
	 * Add a map between nominal value and one numerical value, either int or double (in fact any floating value)
	 * 
	 * @param nominal
	 * @param value
	 */
	public void add(OrderedValue nominal, Number value) {
		GSEnumDataType type = GSDP.getValueType(value.toString());
		switch (type) {
		case Integer:
			mapper.put(IA.getValueSpace().proposeValue(value.toString()), nominal);
			break;
		case Continue:
			mapper.put(CA.getValueSpace().proposeValue(value.toString()), nominal);
			break;
		default:
			throw new IllegalArgumentException(value+" cannot be transpose to any numerical value");
		}
	}
	
	@Override
	public boolean add(IValue mapTo, OrderedValue mapWith) {
		if(Stream.of(GSEnumDataType.Continue, GSEnumDataType.Integer, GSEnumDataType.Range)
				.noneMatch(type -> type.equals(mapWith.getValueSpace().getType())))
			return false;
		mapper.put(mapTo, mapWith);
		return true;
	}
	
	// --------
	
	/**
	 * 
	 * @param nominal
	 * @return
	 */
	public IValue getValue(OrderedValue nominal) {
		Optional<Entry<IValue, OrderedValue>> opt = mapper.entrySet().stream()
				.filter(entry -> entry.getValue().equals(nominal)).findFirst();
		if(opt.isPresent())
			return opt.get().getKey();
		throw new NullPointerException("There is no "+nominal+" value within this mapped attribute ("+this+")");
	}
	
	/**
	 * 
	 * @param nominal
	 * @return
	 */
	public Number getNumericValue(OrderedValue nominal) {
		IValue res = this.getValue(nominal);
		switch (res.getValueSpace().getType()) {
		case Integer:
			return Integer.valueOf(res.getStringValue());
		case Continue:
			return Double.valueOf(res.getStringValue());
		case Range:
			// TODO compute typical standard deviation
			double std = this.getStandardRange();
			RangeValue rv = (RangeValue) res;
			Number lb = rv.getBottomBound();
			Number ub = rv.getTopBound();
			if(lb.intValue() == Integer.MIN_VALUE ||
					lb.doubleValue() == Double.MIN_VALUE)
				lb = ub.doubleValue() - std;
			if(ub.intValue() == Integer.MAX_VALUE ||
					ub.doubleValue() == Double.MAX_VALUE)
				ub = lb.doubleValue() + std;
			return GenstarRandomUtils.rnd(lb, ub);
		default:
			throw new IllegalArgumentException("Cannot get numerical value of "+res);
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public OrderedValue getNominal(Number value) {
		Optional<IValue> opt = mapper.keySet().stream().filter(k -> this.validate(value, k)).findFirst();
		if(opt.isPresent())
			return mapper.get(opt.get());
		throw new NoSuchElementException("There is no relevant numeric value mapper to "+value);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public boolean contains(Number value) {
		return mapper.values().stream().anyMatch(v -> this.validate(value, v));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends IValue> getMappedValues(IValue value) {
		if(mapper.containsKey(value))
			return Collections.singleton(mapper.get(value));
		return Collections.emptyList();
	}
	
	@Override
	public void setRelatedAttribute(MappedAttribute<IValue, OrderedValue> relatedAttribute) {
		this.relatedAttribute = relatedAttribute;
	}

	@Override
	public MappedAttribute<IValue, OrderedValue> getRelatedAttribute() {
		return relatedAttribute;
	}

	@Override
	public Map<Collection<IValue>, Collection<OrderedValue>> getRawMapper() {
		return mapper.entrySet().stream()
				.collect(Collectors.toMap(
						entry->Collections.singleton(entry.getKey()), 
						entry->Collections.singleton(entry.getValue())));
	}
	
	// ------------- UTILS
	
	/*
	 * 
	 */
	private boolean validate(Number v1, IValue v2) {
		switch (v2.getValueSpace().getType()) {
		case Continue:
			return v1.doubleValue() == Double.valueOf(v2.getStringValue());
		case Integer:
			return v1.intValue() == Integer.valueOf(v2.getStringValue());
		case Range:
			RangeValue rv = (RangeValue)v2;
			return v1.doubleValue() >= rv.getBottomBound().doubleValue()
					&& v1.doubleValue() <= rv.getTopBound().doubleValue();
		default:
			throw new IllegalArgumentException(v2.getValueSpace().getType()
					+" is not an acceptable value type for Numeric Value Mapper");
		}
	}
	
	/*
	 * 
	 */
	private double getStandardRange() {
		GSDataParser gsdp = new GSDataParser();
		List<Double> vals = new ArrayList<>();
		for(IValue v : mapper.keySet()) {
			switch (v.getType()) {
			case Range:
				RangeValue rv = (RangeValue) v;
				vals.add(rv.getBottomBound().doubleValue());
				vals.add(rv.getTopBound().doubleValue());
				break;
			default:
				vals.add(gsdp.getDouble(v.getStringValue()));
				break;
			}
		}
		Collections.sort(vals);
		Map<Double, Integer> ranges = new HashMap<>();
		for(int i = 1 ; i < vals.size(); i++) {
			double lRange = vals.get(i) - vals.get(i-1);
			if(ranges.containsKey(lRange))
				ranges.put(lRange, ranges.get(lRange)+1);
			else
				ranges.put(lRange,1);
		}
		double sor = ranges.entrySet().stream()
				.mapToDouble(entry -> entry.getKey()*Math.pow(entry.getValue(),1.5))
				.sum();
		double factor = ranges.values().stream()
				.mapToDouble(v -> Math.pow(v, 1.5)).sum();
		return sor/factor;
	}
	
}
