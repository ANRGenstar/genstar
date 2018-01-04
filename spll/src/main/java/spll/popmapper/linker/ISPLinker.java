package spll.popmapper.linker;

import java.util.Collection;
import java.util.Optional;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;
import spll.popmapper.constraint.ISpatialConstraint;
import spll.popmapper.distribution.ISpatialDistribution;

/**
 * Encapsulate the process that binds an entity of a population to a spatial
 * entity
 * <p>
 * Basically made of a spatial distribution that define probability of each
 * spatial entity candidate to be chosen to be bind with the entity, and spatial
 * constraints that will filter candidates to only keep acceptable ones
 * 
 * @author kevinchapuis
 *
 * @param <SD>
 */
public interface ISPLinker {

	/**
	 * Main method to link an entity to one candidate draw from a collection
	 * 
	 * @param entity
	 * @param candidates
	 * @return
	 */
	public Optional<AGeoEntity<? extends IValue>> getCandidate(SpllEntity entity,
			Collection<AGeoEntity<? extends IValue>> candidates);

	/**
	 * The distribution to be used
	 * 
	 * @return
	 */
	public ISpatialDistribution getDistribution();

	/**
	 * Add constraints to filter candidates with
	 * 
	 * @param constraints
	 */
	public void addConstraints(ISpatialConstraint... constraints);

	/**
	 * Get back the collection of constaints
	 * 
	 * @return
	 */
	public Collection<ISpatialConstraint> getConstraints();

}
