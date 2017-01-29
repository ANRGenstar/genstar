package core.metamodel.pop;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IAttribute;
import core.util.data.GSEnumDataType;

/**
 * The abstract expression of attribute's entity population schema. This schema
 * is defined to be the same for all entity in a given population, and can be 
 * described as:
 * <p>
 * <ul>
 * <li> <b>Name</b> = a unique and characterized name, beware it will drive data case sensitive
 * automatic reading, hence it should have exactly the same form as in data file 
 * <li> <b>Values</b> = the set of all possible values, concrete entities can get for this attribute.
 * Apart from possible values, most attribute can have an empty value, refereed as {@code emptyValue}
 * <li> <b>Data type</b> = a {@link GSEnumDataType} that gives information on value's content type
 * <li> <b>Referent attribute</b> = an attribute this one referees to. That means values of the first are
 * explicitly bind to values of the second
 * </ul>
 * <p> 
 * {@link APopulationAttribute} are defined at population level, where all entity should have
 * a value for each.
 * 
 * @author kevinchapuis
 * @author Duc an vo
 */
public abstract class APopulationAttribute implements IAttribute<APopulationValue> {

	private APopulationAttribute referentAttribute;
	private String name;
	private GSEnumDataType dataType;

	private Set<APopulationValue> values = new HashSet<>();
	private APopulationValue emptyValue;

	public APopulationAttribute(String name, GSEnumDataType dataType, APopulationAttribute referentAttribute) {
		this.name = name;
		this.dataType = dataType;
		// WARNING: Referent attribute could not be of type AggregatedAttribute (call exception ?)
		this.referentAttribute = referentAttribute;
	}

	public APopulationAttribute(String name, GSEnumDataType dataType) {
		this.name = name;
		this.dataType = dataType;
		this.referentAttribute = this;
	}

	@Override
	public String getAttributeName() {
		return name;
	}

	public GSEnumDataType getDataType() {
		return dataType;
	}

	/**
	 * The {@link IAttribute} this attribute target: should be itself, but could indicate disaggregated linked {@link IAttribute} or record linked one
	 * 
	 * @return
	 */
	public APopulationAttribute getReferentAttribute() {
		return referentAttribute;
	}

	@Override
	public Set<APopulationValue> getValues() {
		return Collections.unmodifiableSet(values);
	}

	@Override
	public boolean setValues(Set<APopulationValue> values) {
		if(this.values.isEmpty())
			return this.values.addAll(values);
		return false;
	}

	@Override
	public APopulationValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(APopulationValue emptyValue) {
		this.emptyValue = emptyValue;
	}	

	/**
	 * A record attribute represents a purely utility attribute (for instance, the number of agent of age 10)
	 * 
	 * @return
	 */
	public abstract boolean isRecordAttribute();

	/**
	 * Find a value that fit with the one in argument according to attribute state. It
	 * either can return: 
	 * <p><ul> 
	 * <li> a set of mapped value, with {@link MappedAttribute}
	 * <li> the empty value, with {@link RecordAttribute}
	 * <li> the value itself or the empty value if not any matches, 
	 * with all other type of {@link IAttribute}
	 * </ul><p>
	 * 
	 * @param disVal
	 * @return a set of values
	 */
	public Set<APopulationValue> findMappedAttributeValues(APopulationValue val) {
		if(values.contains(val))
			Stream.of(val).collect(Collectors.toSet());
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}


	////////////////////////////////////////////////////////////////
	// ------------------------- UTILITY ------------------------ //
	////////////////////////////////////////////////////////////////


	@Override
	public String toString(){
		return name+" ("+dataType+") - "+values.size()+" values";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.size());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		APopulationAttribute other = (APopulationAttribute) obj;
		if (dataType != other.dataType)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (values.size() != other.values.size())
			return false;
		return true;
	}

}
