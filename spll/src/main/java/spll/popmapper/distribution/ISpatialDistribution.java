package spll.popmapper.distribution;

import java.util.List;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;

/**
 * Define the higher order concept to define and to draw a spatial entity from a discret distribution of spatial candidates
 * 
 * @author kevinchapuis
 *
 */
public interface ISpatialDistribution {

	/**
	 * Draw a spatial entity from a list of candidates, given that it will be link to the provided {@link SpllEntity} 
	 * @param entity
	 * @param candidates
	 * @return
	 */
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates);

}
