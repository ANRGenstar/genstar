package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;

import core.metamodel.geo.AGeoEntity;

public interface ISpatialConstraint {
	
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests);
	
	public boolean updateConstraint(AGeoEntity nest);
	
	public void relaxConstraint(Collection<AGeoEntity> nests);
	
	public int getPriority();
	
	public boolean isConstraintLimitReach();
	
	public double getCurrentValue();
	
}
