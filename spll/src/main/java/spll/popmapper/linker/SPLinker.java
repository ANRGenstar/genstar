package spll.popmapper.linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.popmapper.constraint.ISpatialConstraint;
import spll.popmapper.distribution.ISpatialDistribution;

/**
 * General implementation for spatial linker - meant to link entity with a spatial entity
 * using a pre-defined distribution and optional spatial constraints
 * 
 * @author kevinchapuis
 *
 */
public class SPLinker<E extends ADemoEntity> implements ISPLinker<E> {
	
	private ISpatialDistribution<E> distribution;
	private List<ISpatialConstraint> constraints;
	private ConstraintsReleaseRule rule;
	
	public SPLinker(ISpatialDistribution<E> distribution) {
		this.distribution = distribution;
		this.constraints = new ArrayList<>();
		this.rule = ConstraintsReleaseRule.PRIORITY;
	}
	
	public SPLinker(ISpatialDistribution<E> distribution, ConstraintsReleaseRule rule) {
		this(distribution);
		this.rule = rule;
	}

	@Override
	public Optional<AGeoEntity<? extends IValue>> getCandidate(E entity,
			Collection<? extends AGeoEntity<? extends IValue>> candidates) {

		Collection<AGeoEntity<? extends IValue>> filteredCandidates = this.filter(candidates);

		return filteredCandidates.isEmpty() ? Optional.empty() : 
			Optional.ofNullable(distribution.getCandidate(entity, new ArrayList<>(filteredCandidates)));
	}
	
	@Override
	public void setDistribution(ISpatialDistribution<E> distribution) {
		this.distribution = distribution;
	}

	@Override
	public ISpatialDistribution<E> getDistribution() {
		return distribution;
	}
	
	@Override
	public Collection<AGeoEntity<? extends IValue>> filter(
			Collection<? extends AGeoEntity<? extends IValue>> candidates) {
		List<AGeoEntity<? extends IValue>> filteredCandidates = new ArrayList<>(candidates);
		switch(rule) {
		case LINEAR:
			throw new UnsupportedOperationException("This feature has not been yet implemented. "
					+ "Request developpers at https://github.com/ANRGenstar/genstar");
		default:
			List<ISpatialConstraint> scs = constraints.stream().sorted(
					(c1,c2) -> Integer.compare(c1.getPriority(), c2.getPriority()))
					.collect(Collectors.toList());
			for(ISpatialConstraint sc : scs) {
				List<AGeoEntity<? extends IValue>> newFilteredCandidates = sc.getCandidates(filteredCandidates);
				if(newFilteredCandidates.isEmpty()) {
					do {
						sc.relaxConstraint(filteredCandidates);
						newFilteredCandidates = sc.getCandidates(filteredCandidates);
					} while(!newFilteredCandidates.isEmpty() &&
							!sc.isConstraintLimitReach());
				}
				if(newFilteredCandidates.isEmpty())
					return Collections.emptyList();
				filteredCandidates = newFilteredCandidates;
			}
			return filteredCandidates;
		}
		
	}

	@Override
	public void setConstraints(List<ISpatialConstraint> constraints) {
		this.constraints = constraints;
	}
	
	@Override
	public void addConstraints(ISpatialConstraint... constraints) {
		this.constraints.addAll(Arrays.asList(constraints));
		
	}

	@Override
	public List<ISpatialConstraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	@Override
	public ConstraintsReleaseRule getConstraintsReleaseRule() {
		return this.rule;
	}

	@Override
	public void setConstraintsReleaseRule(ConstraintsReleaseRule rule) {
		this.rule = rule;
	}

}
