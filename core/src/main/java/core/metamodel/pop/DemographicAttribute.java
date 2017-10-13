package core.metamodel.pop;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;
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
 * {@link DemographicAttribute} are defined at population level, where all entity should have
 * a value for each.
 * 
 * @author kevinchapuis
 * @author Duc an vo
 */
public class DemographicAttribute<V extends IValue> implements IAttribute<V> {

	private IValueSpace<V> valuesSpace;
	
	private String name;

	private String description = null;
	
	public DemographicAttribute(String name, IValueSpace<V> valueSpace) {
		this.valuesSpace = valueSpace;
	}
	
	// ----------------- IAttribute contract methods ----------------- //
	
	@Override
	public final String getAttributeName() {
		return name;
	}
	
	@Override
	public IValueSpace<V> getValueSpace(){
		return valuesSpace;
	}
	
	// --------------------------------------------------------------- //
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * The {@link IAttribute} this attribute target: should be itself, 
	 * but could indicate disaggregated linked {@link IAttribute} or record linked one
	 * 
	 * @return
	 */
	public DemographicAttribute<? extends IValue> getReferentAttribute(){
		return this;
	}
	
	/**
	 * Tests if this attribute is linked to the one passed as argument.
	 * It returns true if one of these are true:
	 * <p>
	 * <ul>
	 * <li> {@code this.equals(attribute)}
	 * <li> {@code this.equals(attribute.getReferentAttribute())}
	 * <li> {@code this.getReferentAttribute().equals(attribute)}
	 * </ul>
	 * <p>
	 *  
	 * TODO: see if it can be removed
	 *  
	 * @param attribute
	 * @return
	 */
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		if(this.equals(attribute) || this.equals(attribute.getReferentAttribute())
				|| this.getReferentAttribute().equals(attribute))
			return true;
		return false;
	}
	
	public IValue getEmptyValue(){
		return valuesSpace.getEmptyValue();
	}

	public Set<? extends IValue> findMappedAttributeValues(IValue value){
		if(this.getValueSpace().contains(value) ||
				this.getValueSpace().isValidCandidate(value.getStringValue()))
			return Stream.of(value).collect(Collectors.toSet());
		return Collections.emptySet();
	}

	////////////////////////////////////////////////////////////////
	// ------------------------- UTILITY ------------------------ //
	////////////////////////////////////////////////////////////////


	@Override
	public String toString(){
		return name+" ("+this.getValueSpace().getType()+") - "+this.getValueSpace()+" values";
	}


}
