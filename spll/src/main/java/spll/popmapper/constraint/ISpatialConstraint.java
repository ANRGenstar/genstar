package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;

/**
 * Represents a spatial constraint which might return the candidates compliant 
 * with this constraint. Might be relaxed.
 * 
 * @author Samuel Thiriot
 */
public interface ISpatialConstraint {
	
	public List<AGeoEntity<? extends IValue>> getCandidates(List<AGeoEntity<? extends IValue>> nests);
	
	public boolean updateConstraint(AGeoEntity<? extends IValue> nest);
	
	public void relaxConstraint(Collection<AGeoEntity<? extends IValue>> nests);
	
	public int getPriority();
	
	public boolean isConstraintLimitReach();
	
	public double getCurrentValue();
	
}
