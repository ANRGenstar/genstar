package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.pop.APopulationEntity;

public interface SpatialConstraint {
	
	
	public abstract List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests);
	
	public abstract List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests, APopulationEntity entity);
	
	public abstract boolean updateConstraint(APopulationEntity entity, AGeoEntity nest);
	
	public abstract void relaxConstraint(Collection<AGeoEntity> nests);
	
	public abstract int getPriority();
	
	public abstract boolean isConstraintLimitReach();
	
	public abstract double getCurrentValue();
	
}
