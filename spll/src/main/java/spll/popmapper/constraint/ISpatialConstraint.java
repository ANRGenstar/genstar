package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.pop.APopulationEntity;

public interface ISpatialConstraint {
	
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests);
	
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests, APopulationEntity entity);
	
	public boolean updateConstraint(APopulationEntity entity, AGeoEntity nest);
	
	public void relaxConstraint(Collection<AGeoEntity> nests);
	
	public int getPriority();
	
	public boolean isConstraintLimitReach();
	
	public double getCurrentValue();
	
}
