package core.configuration.dictionary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.MappedDemographicAttribute;
import core.metamodel.attribute.demographic.OTODemographicAttribute;
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
	
	private Set<MappedDemographicAttribute<? extends IValue, ? extends IValue>> mappedAttSet;
	private Set<OTODemographicAttribute<? extends IValue, ? extends IValue>> recordAttSet;
	
	public DemographicDictionary() {
		attSet = new HashSet<>();
	}
	
	public Set<DemographicAttribute<? extends IValue>> getAttributes() {
		return Stream.concat(mappedAttSet.stream(), 
				Stream.concat(attSet.stream(), mappedAttSet.stream()))
				.collect(Collectors.toSet());
	}
	
	public Set<OTODemographicAttribute<? extends IValue, ? extends IValue>> getRecordAttribute(){
		return recordAttSet;
	}
	
	// ---------------------------- SETTERS ---------------------------- //
	
	/**
	 * Add attributes to this dictionary
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DemographicDictionary addAttributes(DemographicAttribute<? extends IValue>... attributes) {
		this.attSet.addAll(Arrays.asList(attributes));
		return this;
	}
	
	/**
	 * Add record attributes
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DemographicDictionary addRecordAttributes(OTODemographicAttribute<? extends IValue, ? extends IValue>... attributes) {
		this.recordAttSet.addAll(Arrays.asList(attributes));
		return this;
	}

	/**
	 * Add attributes linked to other attributes
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DemographicDictionary addMappedAttributes(MappedDemographicAttribute<? extends IValue, ? extends IValue>... attributes) {
		this.mappedAttSet.addAll(Arrays.asList(attributes));
		return this;
	}
	
}
