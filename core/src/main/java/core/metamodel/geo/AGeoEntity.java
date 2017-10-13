package core.metamodel.geo;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.metamodel.IEntity;
import core.metamodel.value.IValue;

public abstract class AGeoEntity implements IEntity<AGeoAttribute<IValue>> {
 
	private String gsName;
	
	private Map<AGeoAttribute<IValue>, IValue> attributes;
	
	@Override
	public Collection<AGeoAttribute<IValue>> getAttributes(){
		return attributes.keySet();
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
	public Point getLocation() {
		return getGeometry().getCentroid();
	}
	
}
