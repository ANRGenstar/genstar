package core.metamodel.value.categoric;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSEnumDataType;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class NominalSpace implements IValueSpace<NominalValue> {

	private IAttribute<NominalValue> attribute; 
	
	private Set<NominalValue> values;
	private NominalValue emptyValue;
	
	private GSCategoricTemplate ct;
	
	public NominalSpace(IAttribute<NominalValue> attribute, GSCategoricTemplate ct){
		this.attribute = attribute;
		this.values = new HashSet<>();
		this.emptyValue = new NominalValue(this, null);
		this.ct = ct;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Nominal;
	}
	
	@Override
	public boolean isValidCandidate(String value) {
		return true;
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //
	
	@Override
	public NominalValue addValue(String value) throws IllegalArgumentException {
		NominalValue nv = null;
		try {
			nv = this.getValue(value);
		} catch (NullPointerException e) {
			nv = new NominalValue(this, ct.getFormatedString(value));
			this.values.add(nv);
		}
		return nv;
	}
	
	@Override
	public Set<NominalValue> getValues(){
		return Collections.unmodifiableSet(values);
	}

	@Override
	public NominalValue getValue(String value) throws NullPointerException {
		String formatedValue = ct.getFormatedString(value);
		Optional<NominalValue> opValue = values.stream()
				.filter(v -> v.getStringValue().equals(formatedValue)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}
	
	@Override
	public NominalValue getEmptyValue() {
		return emptyValue;
	}
	
	@Override
	public void setEmptyValue(String value){
		try {
			this.emptyValue = this.getValue(value);
		} catch (NullPointerException e) {
			this.emptyValue = new NominalValue(this, value); 
		}
	}

	@Override
	public IAttribute<NominalValue> getAttribute() {
		return attribute;
	}
	
	/**
	 * Gives the template used to elaborate proper formated value for this value space
	 * @return
	 */
	public GSCategoricTemplate getCategoricTemplate() {
		return ct;
	}
	
	// ---------------------------------------------- //
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.getHashCode();
		result = prime * result + ct.hashCode();
		return result;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.isEqual(obj) && this.ct.equals(((NominalSpace)obj).getCategoricTemplate());
	}
	
	@Override
	public String toString() {
		return this.toPrettyString();
	}
	
}
