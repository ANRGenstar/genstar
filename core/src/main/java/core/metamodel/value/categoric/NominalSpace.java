package core.metamodel.value.categoric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSEnumDataType;

/**
 * A set of value of nominal type
 * 
 * @author kevinchapuis
 *
 */
public class NominalSpace implements IValueSpace<NominalValue> {

	private IAttribute<NominalValue> attribute; 
	
	protected Map<String, NominalValue> values;
	private NominalValue emptyValue;
	
	private GSCategoricTemplate ct;
	
	public NominalSpace(IAttribute<NominalValue> attribute, GSCategoricTemplate ct){
		this.attribute = attribute;
		this.values = new HashMap<>();
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
	public NominalValue proposeValue(String value) {
		return new NominalValue(this, value);
	}
	
	@Override
	public NominalValue getInstanceValue(String value) {
		return new NominalValue(this, ct.getFormatedString(value));
	}
	
	@Override
	public NominalValue addValue(String value) throws IllegalArgumentException {
		String val = ct.getFormatedString(value);
		NominalValue nv = values.get(val);
		if(nv == null) {
			nv = new NominalValue(this, val);
			this.values.put(val, nv);
		}
		return nv;
	}
	
	@Override
	public Set<NominalValue> getValues(){
		return new HashSet<>(values.values());
	}

	@Override
	public NominalValue getValue(String value) throws NullPointerException {
		NominalValue val = values.get(ct.getFormatedString(value));
		if(val == null)
			throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
		return val;
	}
	
	@Override
	public boolean contains(IValue value) {
		if(!value.getClass().equals(NominalValue.class))
			return false;
		return values.values().contains(value);
	}
	
	@Override
	public NominalValue getEmptyValue() {
		return emptyValue;
	}
	
	@Override
	public void setEmptyValue(String value){
		String val = ct.getFormatedString(value);
		NominalValue nv = values.get(val);
		if(nv == null)
			nv = new NominalValue(this, val);
		this.emptyValue = nv;
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
