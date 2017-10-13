package spll;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.pop.ADemoEntity;

public class SpllEntity extends ADemoEntity {

	private ADemoEntity entity;
	
	private Point location = null;
	private AGeoEntity nest = null;

	public SpllEntity(ADemoEntity entity) {
		super(entity.getValues().stream().collect(Collectors
				.toMap(val -> val.getAttribute(), Function.identity())));
		this.entity = entity;
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
	 * Retrieve the most significant enclosing geographical entity this
	 * entity is situated. It represents 'home's entity 
	 * 
	 * @return
	 */
	public AGeoEntity getNest(){
		return nest;
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
	 * Change the nest of the entity
	 * 
	 * @param entity
	 */
	public void setNest(AGeoEntity nest){
		this.nest = nest;
	}
	
}
