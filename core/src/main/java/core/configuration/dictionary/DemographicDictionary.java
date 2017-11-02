package core.configuration.dictionary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.OTSDemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.STSDemographicAttribute;
import core.metamodel.entity.ADemoEntity;
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
	
	private Set<OTSDemographicAttribute<? extends IValue>> aggregAttSet;
	private Set<STSDemographicAttribute<? extends IValue>> mappedAttSet;
	
	private Set<STSDemographicAttribute<? extends IValue>> recordAttSet;
	
	public DemographicDictionary() {
		attSet = new HashSet<>();
	}
	
	public Set<DemographicAttribute<? extends IValue>> getAttributes() {
		return Stream.concat(mappedAttSet.stream(), 
				Stream.concat(attSet.stream(), aggregAttSet.stream()))
				.collect(Collectors.toSet());
	}
	
	public Set<STSDemographicAttribute<? extends IValue>> getRecordAttribute(){
		return recordAttSet;
	}
	
	// ---------------------------- SETTERS ---------------------------- //
	
	@SuppressWarnings("unchecked")
	public DemographicDictionary addAttributes(DemographicAttribute<? extends IValue>... attributes) {
		this.attSet.addAll(Arrays.asList(attributes));
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public DemographicDictionary addAggregatedAttributes(OTSDemographicAttribute<? extends IValue>... attributes) {
		this.aggregAttSet.addAll(Arrays.asList(attributes));
		return this;
	}

	@SuppressWarnings("unchecked")
	public DemographicDictionary addMappedAttributes(STSDemographicAttribute<? extends IValue>... attributes) {
		this.mappedAttSet.addAll(Arrays.asList(attributes));
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public DemographicDictionary addRecordAttributes(STSDemographicAttribute<? extends IValue>... attributes) {
		this.recordAttSet.addAll(Arrays.asList(attributes));
		return this;
	}
	
}
