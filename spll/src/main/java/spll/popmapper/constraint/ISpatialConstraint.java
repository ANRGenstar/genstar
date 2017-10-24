package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.value.IValue;

public interface ISpatialConstraint {
	
	public List<AGeoEntity<? extends IValue>> getSortedCandidates(List<AGeoEntity<? extends IValue>> nests);
	
	public boolean updateConstraint(AGeoEntity<? extends IValue> nest);
	
	public void relaxConstraint(Collection<AGeoEntity<? extends IValue>> nests);
	
	public int getPriority();
	
	public boolean isConstraintLimitReach();
	
	public double getCurrentValue();
	
}
