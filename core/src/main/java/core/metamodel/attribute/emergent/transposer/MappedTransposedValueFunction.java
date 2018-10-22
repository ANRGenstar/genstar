package core.metamodel.attribute.emergent.transposer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;

public class MappedTransposedValueFunction<RV extends IValue> implements ITransposeValueFunction<IValue, RV> {

	private final Map<Collection<IValue>, RV> mapper;
	private List<Collection<IValue>> listedMapper;
	
	public MappedTransposedValueFunction(Map<Collection<IValue>, RV> mapper) {
		this.mapper = mapper;
		this.listedMapper = mapper.keySet().stream().sorted(new Comparator<Collection<IValue>>() {

			@Override
			public int compare(Collection<IValue> o1, Collection<IValue> o2) {
				// TODO Auto-generated method stub
				return o1.size() - o2.size() > 0 ? -1 : 1;
			}
			
		}).collect(Collectors.toList());
	}
	
	@Override
	public RV transpose(Collection<IValue> values, IValueSpace<RV> valueSpace) {
		// TODO Auto-generated method stub
		Optional<Collection<IValue>> opt = listedMapper.stream().filter(k -> values.containsAll(k)).findAny();
		return opt.isPresent() ? mapper.get(opt.get()) : valueSpace.getEmptyValue();
	}

	@Override
	public Collection<IValue> reverse(RV value, IValueSpace<IValue> valueSpace) {
		// TODO Auto-generated method stub
		return mapper.containsValue(value) ? 
				mapper.entrySet().stream().filter(e -> e.getValue().equals(value)).findFirst().get().getKey() 
				: Collections.emptyList();
	}

}
