package spll.localizer.linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;
import spll.localizer.constraint.ISpatialConstraint;
import spll.localizer.distribution.ISpatialDistribution;

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
	public Map<E, Optional<AGeoEntity<? extends IValue>>> getCandidates(Collection<E> entities,
			Collection<? extends AGeoEntity<? extends IValue>> candidates) {
		throw new UnsupportedOperationException("This method [SPLinker.getCandidates(...)] has not been yet implemented. "
				+ "Request developpers at https://github.com/ANRGenstar/genstar");
		
		/*
		List<ISpatialConstraint> otherConstraints = constraints.stream()
				.sorted((n1, n2) -> Integer.compare( n1.getPriority(), n2.getPriority()))
				.collect(Collectors.toList());
		
		Collection<SpllEntity> remainingEntities = null;
		List<AGeoEntity<? extends IValue>> spatialCandidates = new ArrayList<>(candidates);
		for (ISpatialConstraint cr : otherConstraints) {
			while (!cr.isConstraintLimitReach()) {
				
				for (ISpatialConstraint constraint : otherConstraints) {
					spatialCandidates = constraint.getCandidates(spatialCandidates);
				}
				
				remainingEntities = localizationInNestOp(remainingEntities, candidates, null);
				
				if (remainingEntities != null && !remainingEntities.isEmpty()) 
					cr.relaxConstraint(spatialCandidates);
				else return;

			}
		}
		*/
			
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
		List<ISpatialConstraint> scs = constraints.stream().sorted(
				(c1,c2) -> Integer.compare(c1.getPriority(), c2.getPriority()))
				.collect(Collectors.toList());
		switch(rule) {
		case LINEAR:
			do {
				List<AGeoEntity<? extends IValue>> newFilteredCandidates = new ArrayList<>(filteredCandidates);
				for(ISpatialConstraint sc : scs.stream()
						.filter(c -> !c.isConstraintLimitReach())
						.collect(Collectors.toList())) {
					newFilteredCandidates = sc.getCandidates(filteredCandidates);
					if(newFilteredCandidates.isEmpty()) {
						sc.relaxConstraint(newFilteredCandidates);
						newFilteredCandidates = sc.getCandidates(newFilteredCandidates);
					}
				}
				if(!newFilteredCandidates.isEmpty()) {
					return newFilteredCandidates;
				}
			} while(scs.stream().noneMatch(c -> !c.isConstraintLimitReach()));
			return Collections.emptyList();
		default:
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
