package core.io.geo.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.attribute.AGeoAttribute;
import core.io.geo.entity.attribute.value.AGeoValue;
import core.metamodel.IEntity;

public abstract class AGeoEntity implements IEntity<AGeoAttribute, AGeoValue> {

	private Map<AGeoAttribute, AGeoValue> values; 
	private String gsName;
	private IEntity<?, ?> nest;
	
	public AGeoEntity(Set<AGeoValue> values, String gsName) {
		this.values = values.stream().collect(Collectors.toMap(AGeoValue::getAttribute, val -> val));
		this.gsName = gsName;
	}
	
	@Override
	public Collection<AGeoAttribute> getAttributes(){
		return values.keySet();
	}

	@Override
	public AGeoValue getValueForAttribute(AGeoAttribute attribute){
		return values.get(attribute);
	}
	
	public void addAttribute(AGeoAttribute attribute, AGeoValue value) {
		values.put(attribute, value);
	}
	
	
	/**
	 * The value associated with this attribute. 
	 * Return value is of type {@link IGeoValue} that can encapsulate either numerical or nominal value
	 * 
	 * @param property
	 * @return {@link IGeoValue}
	 */
	public AGeoValue getValueForAttribute(String property){
		Optional<AGeoAttribute> opAtt = values.keySet()
				.stream().filter(att -> att.getAttributeName().equals(property)).findFirst();
		if(opAtt.isPresent())
			return values.get(opAtt.get());
		throw new NullPointerException("Attribute "+property+" does not exist in "+this.getClass().getSimpleName());
	}

	@Override
	public Set<AGeoValue> getValues(){
		return new HashSet<>(values.values());
	}
	
	/**
	 * Gives the name of this attribute
	 * 
	 * @return
	 */
	public String getGenstarName(){
		return gsName;
	}
	
	/**
	 * A collection of property name for this geographical attribute
	 * 
	 * @return
	 */
	public Collection<String> getPropertiesAttribute(){
		return this.getAttributes().stream()
				.map(AGeoAttribute::getAttributeName).collect(Collectors.toList());
	}
	
	/**
	 * The attribute that match the given property name if any. In case, no
	 * attribute matches the name passed as argument return null
	 * 
	 * @param prop
	 * @return
	 */
	public AGeoAttribute getPropertyAttribute(String propertyName) {
		Optional<AGeoAttribute> opAtt = getAttributes().stream()
				.filter(att -> att.getAttributeName().equals(propertyName))
				.findFirst();
		if(!opAtt.isPresent())
			return null;
		return opAtt.get();
	}
	
	/**
	 * Gives the area (metrics refers to geometry's crs) of this attribute
	 * 
	 * @return
	 */
	public double getArea(){
		return getGeometry().getArea();
	}
	
	
	/**
	 * The geometry charcaterizes the attribute
	 * 
	 * @return {@link Geometry}
	 */
	public abstract Geometry getGeometry();
	
	/**
	 * The point characterizes the attribute location
	 * 
	 * @return {@link Point}
	 */
	@Override
	public Point getLocation() {
		return getGeometry().getCentroid();
	}

	@Override
	public IEntity<?, ?> getNest() {
		return nest;
	}

	//do nothing - it is not possible to modify the location of an agent
	@Override
	public void setLocation(Point location) {}

	
	@Override
	public void setNest(IEntity<?, ?> entity) {
		this.nest = entity;
	}
	
}
