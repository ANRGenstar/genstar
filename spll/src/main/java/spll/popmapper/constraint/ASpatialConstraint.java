package spll.popmapper.constraint;

import java.util.Collection;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.value.IValue;

public abstract class ASpatialConstraint implements ISpatialConstraint{

	protected int priority = 1;
	protected boolean sortCandidates = false;
	protected double maxIncrease = 0.0;
	protected double increaseStep = 0.0;
	protected double currentValue = 0.0;
	protected int nbIncrements = 0;
	protected boolean constraintLimitReach;
	
	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isSortedCandidates() {
		return sortCandidates;
	}

	public void setSortedCandidates(boolean sortedCandidates) {
		this.sortCandidates = sortedCandidates;
	}

	public double getMaxIncrease() {
		return maxIncrease;
	}

	public void setMaxIncrease(double maxIncrease) {
		this.maxIncrease = maxIncrease;
	}

	public double getIncreaseStep() {
		return increaseStep;
	}

	public void setIncreaseStep(double increaseStep) {
		this.increaseStep = increaseStep;
	}
	
	@Override
	public void relaxConstraint(Collection<AGeoEntity<? extends IValue>> nests) {
		if (currentValue < maxIncrease) {
			currentValue = Math.min(currentValue + increaseStep, maxIncrease);
			constraintLimitReach = false;
			nbIncrements++;
		} else {
			constraintLimitReach = true;
		}
		relaxConstraintOp(nests);
	}


	public abstract void relaxConstraintOp(Collection<AGeoEntity<? extends IValue>> nests);
	
	public boolean isConstraintLimitReach() {
		return constraintLimitReach;
	}

	public double getCurrentValue() {
		return currentValue;
	}
	
	
}
