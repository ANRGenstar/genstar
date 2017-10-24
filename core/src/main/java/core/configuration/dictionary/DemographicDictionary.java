package core.configuration.dictionary;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.AgregatedDemographicAttribute;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.pop.attribute.MappedDemographicAttribute;
import core.metamodel.value.IValue;

/**
 * Encapsulate the whole set of a given attribute type. For example, it can describe the all {@link DemographicAttribute}
 * used to model a {@link IPopulation} of {@link ADemoEntity} 
 * 
 * @author kevinchapuis
 *
 * @param <A>
 */
public class DemographicDictionary {

	private Set<DemographicAttribute<? extends IValue>> attSet;
	
	private Set<AgregatedDemographicAttribute<? extends IValue>> agregAttSet;
	private Set<MappedDemographicAttribute<? extends IValue, IValue>> mappedAttSet;
	
	private Set<MappedDemographicAttribute<? extends IValue, IValue>> recordAttSet;
	
	
	public Set<DemographicAttribute<? extends IValue>> getAttributes() {
		return Stream.concat(mappedAttSet.stream(), 
				Stream.concat(attSet.stream(), agregAttSet.stream()))
				.collect(Collectors.toSet());
	}
	
	public Set<MappedDemographicAttribute<? extends IValue, IValue>> getRecordAttribute(){
		return recordAttSet;
	}

}
