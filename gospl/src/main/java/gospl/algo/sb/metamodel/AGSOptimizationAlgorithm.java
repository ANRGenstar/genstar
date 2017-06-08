package gospl.algo.sb.metamodel;

import java.util.HashSet;
import java.util.Set;

import gospl.distribution.matrix.AFullNDimensionalMatrix;

public abstract class AGSOptimizationAlgorithm implements IGSOptimizationAlgorithm {

	private Set<AFullNDimensionalMatrix<Integer>> objectives;
	
	public AGSOptimizationAlgorithm() {
		this.objectives = new HashSet<>();
	}

	@Override
	public Set<AFullNDimensionalMatrix<Integer>> getObjectives() {
		return objectives;
	}

	@Override
	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives) {
		this.objectives.add(objectives);
	}
	
}
