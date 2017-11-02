package spll;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;

public class SpllEntity extends ADemoEntity {

	private ADemoEntity entity;
	
	private Point location = null;
	private AGeoEntity<? extends IValue> nest = null;

	public SpllEntity(ADemoEntity entity) {
		super(entity.getAttributes().stream().collect(Collectors
				.toMap(Function.identity(), att -> entity.getValueForAttribute(att))));
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
