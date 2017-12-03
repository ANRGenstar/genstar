package spll;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;

/**
 * Represents a spatialized entity which can have attributes and a geometry. 
 * Such an entity might be generated.
 * 
 * @author Kevin Chapuis
 */
public class SpllEntity extends ADemoEntity {

	private ADemoEntity entity;
	
	private Point location = null;
	private AGeoEntity<? extends IValue> nest = null;
	
	private Geometry geometry = null;

	public SpllEntity(ADemoEntity entity) {
		super(entity.getAttributes().stream().collect(Collectors
				.toMap(Function.identity(), att -> entity.getValueForAttribute(att))));
		this.entity = entity;
	}
	
	/**
	 * Creates an SPLLEntity from a demo entity (with attributes)
	 * and a geometry
	 * @param entity
	 * @param geometry
	 */
	public SpllEntity(ADemoEntity entity, Geometry geometry) {
		super(entity.getAttributes().stream().collect(Collectors
				.toMap(Function.identity(), att -> entity.getValueForAttribute(att))));
		this.entity = entity;
		setGeometry(geometry);
	}
	
	
	/**
	 * Defines the geometry for this SpllEntity.
	 * If the geometry is a point, uses it as the location of this entity.
	 * Else defines the location as the centroid of this entity.
	 * @param geometry
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
		if (geometry instanceof Point) {
			this.location = (Point) geometry;
		} else {
			this.location = geometry.getCentroid();
		}
	}
	
	/**
	 * Returns the Geotools geometry of this entity.
	 * @return
	 */
	public final Geometry getGeometry() {
		return this.geometry;
	}

	@Override
	public SpllEntity clone() {
		SpllEntity spe = new SpllEntity(entity);
		spe.setLocation(this.location);
		spe.setNest(this.nest);
		return spe;
	}
	
	/**
	 * Retrieve the localtion of the agent as a point
	 * 
	 * @return a point of type {@link Point}
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * Change the location of the entity
	 * 
	 * @param location
	 */
	public void setLocation(Point location){
		this.location = location;
	}


	/**
	 * Retrieve the most significant enclosing geographical entity this
	 * entity is situated. It represents 'home's entity 
	 * 
	 * @return
	 */
	public AGeoEntity<? extends IValue> getNest(){
		return nest;
	}
	
	/**
	 * Change the nest of the entity
	 * 
	 * @param entity
	 */
	public void setNest(AGeoEntity<? extends IValue> nest){
		this.nest = nest;
	}

	
}
