package core.metamodel.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import core.util.data.GSDataParser;

public abstract class AGeoEntity<V extends IValue> implements IEntity<Attribute<? extends V>> {
 
	/**
	 * The unique identifier of the entity. See {@link EntityUniqueId}
	 */
	private String id = null;
		
	private String gsName;
	
	private Map<Attribute<? extends V>, V> attributes;
	

	/**
	 * The proxy use for the localization of agents and area computation: 
	 * by default, the geometry of the entity
	 */
	protected Geometry proxyGeometry;
	
	/**
	 * The type of the agent (like "household" or "building"), 
	 * or null if undefined
	 */
	private String type;
	
	
	public AGeoEntity(Map<Attribute<? extends V>, V> attributes, String id) {
		this.attributes = attributes;
		this.gsName = id;
	}

	@Override
	public Collection<Attribute<? extends V>> getAttributes() {
		return attributes.keySet();
	}
	
	@Override
	public Map<Attribute<? extends V>, IValue> getAttributeMap() {
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public boolean hasAttribute(Attribute<? extends V> attribute) {
		return attributes.containsKey(attribute);
	}

	@Override
	public Collection<V> getValues() {
		return attributes.values();
	}

	@Override
	public V getValueForAttribute(Attribute<? extends V> attribute) {
		return attributes.get(attribute);
	}

	@Override
	public V getValueForAttribute(String property) {
		return attributes.get(attributes.keySet().stream()
				.filter(att -> att.getAttributeName().equals(property)).findAny().get());
	}
	
	// ---------------------- Geo-location contract for GeoEntities ---------------------- //
	
	/**
	 * Translate the given value into numeric data. If this value
	 * is not of number type would return NaN
	 * 
	 * @param attribute
	 * @return
	 */
	public Number getNumericValueForAttribute(Attribute<? extends V> attribute) {
		if(attribute.getValueSpace().getType().isNumericValue())
			return new GSDataParser().parseNumbers(this.getValueForAttribute(attribute).getStringValue());
		return Double.NaN;
	}
	
	/**
	 * Based on #getNumericValueForAttribute(Attribute) method, using 
	 * {@link #getValueForAttribute(String)} to retrieve proper attribute
	 * 
	 * @param attribute
	 * @return
	 */
	public Number getNumericValueForAttribute(String attribute) {
		return this.getNumericValueForAttribute(attributes.keySet().stream()
				.filter(att -> att.getAttributeName().equals(attribute)).findAny().get());
	}
	
	/**
	 * The geometry charcaterizes the attribute
	 * 
	 * @return {@link Geometry}
	 */
	public abstract Geometry getGeometry();
	
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
				.map(Attribute::getAttributeName).collect(Collectors.toList());
	}
	
	/**
	 * Gives the area (metrics refers to geometry's crs) of this attribute
	 * 
	 * @return
	 */
	public double getArea(){
		return getProxyGeometry().getArea();
	}
	
	@Override
	public final void _setEntityId(String novelid) throws IllegalStateException {
		if (this.id != null)
			throw new IllegalArgumentException("cannot change the identifier of an agent; "+
						"this agent already had id "+this.id+" but we were asked "+
					"to change it for "+novelid);
		this.id = novelid;
	}

	@Override
	public final String getEntityId() throws IllegalStateException {
		if (this.id == null)
			throw new IllegalStateException("no id is defined yet for agent "+this.toString());
		return this.id;
	}
	
	@Override
	public final boolean _hasEntityId() {
		return this.id != null;
	}

	/**
	 * The point characterizes the attribute location
	 * 
	 * @return {@link Point}
	 */
	public Point getLocation() {
		return getGeometry().getCentroid();
	}
	
	@Override
	public final boolean hasEntityType() {
		return type != null;
	}

	@Override
	public final String getEntityType() {
		return type;
	}

	@Override
	public void setEntityType(String type) {
		this.type = type;
	}
	

	@Override
	public boolean hasParent() {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public IEntity<?> getParent() {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public void setParent(IEntity<?> e) {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public boolean hasChildren() {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public int getCountChildren() {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public Set<IEntity<?>> getChildren() {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}

	@Override
	public void addChild(IEntity<?> e) {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");	}

	@Override
	public void addChildren(Collection<IEntity<?>> e) {
		throw new NotImplementedException("geo entities are not compliant with the multilevel population framework");
	}
	
	public Geometry getProxyGeometry() {
		return proxyGeometry == null ? getGeometry() : proxyGeometry;
	}

	public void setProxyGeometry(Geometry proxyGeometry) {
		this.proxyGeometry = proxyGeometry;
	}


	
}
