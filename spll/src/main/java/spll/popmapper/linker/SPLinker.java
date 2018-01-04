package spll.popmapper.linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;
import spll.popmapper.constraint.ISpatialConstraint;
import spll.popmapper.distribution.ISpatialDistribution;

/**
 * General implementation for spatial linker - meant to link entity with a spatial entity
 * using a pre-defined distribution and optional spatial constraints
 * 
 * @author kevinchapuis
 *
 */
public class SPLinker implements ISPLinker {
	
	private ISpatialDistribution distribution;
	private Collection<ISpatialConstraint> constraints;
	
	public SPLinker(ISpatialDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public Optional<AGeoEntity<? extends IValue>> getCandidate(SpllEntity entity,
			Collection<AGeoEntity<? extends IValue>> candidates) {
		List<AGeoEntity<? extends IValue>> filteredCandidates = new ArrayList<>(candidates);
		for(ISpatialConstraint sc : constraints) {
			List<AGeoEntity<? extends IValue>> newFilteredCandidates = sc.getCandidates(filteredCandidates);
			if(newFilteredCandidates.isEmpty()) {
				do {
					sc.relaxConstraint(filteredCandidates);
					newFilteredCandidates = sc.getCandidates(filteredCandidates);
				} while(newFilteredCandidates.isEmpty() &&
						!sc.isConstraintLimitReach());
			}
			if(newFilteredCandidates.isEmpty())
				return Optional.empty();
		}
		return Optional.ofNullable(distribution.getCandidate(entity, filteredCandidates));
	}

	@Override
	public ISpatialDistribution getDistribution() {
		return distribution;
	}

	@Override
	public void addConstraints(ISpatialConstraint... constraints) {
		this.constraints.addAll(Arrays.asList(constraints));
		
	}

	@Override
	public Collection<ISpatialConstraint> getConstraints() {
		return Collections.unmodifiableCollection(constraints);
	}

}
