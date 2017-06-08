package gospl.algo.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.metamodel.pop.APopulationEntity;
import core.util.random.GenstarRandom;
import gospl.algo.sampler.IEntitySampler;
import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;
import gospl.algo.sb.tabusearch.TabuSearch;
import gospl.algo.sb.tabusearch.list.GSTabuList;
import gospl.algo.sb.tabusearch.solution.GSDuplicateShiftSolution;
import gospl.algo.sb.tabusearch.solution.GSUniqueShiftSolution;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public class TabuSampler implements IEntitySampler {

	private Collection<APopulationEntity> sample;
	private RandomSampler basicSampler;

	private IGSSampleBasedCOSolution solution;
	private TabuSearch tabu;
	
	public TabuSampler(int maxIterations, int tabuListSize) {
		this.tabu = new TabuSearch(new GSTabuList(tabuListSize), 
				maxIterations);
	}
	
	@Override
	public APopulationEntity draw() {
		if(solution == null)
			throw new RuntimeException("You cannot draw a unique entity before "
					+ "drawing an entire collection of entities, i.e. a solution to tabu search");
		return (APopulationEntity) this.solution.getSolution().toArray()[GenstarRandom.getInstance().nextInt(sample.size())];
	}

	@Override
	public List<APopulationEntity> draw(int numberOfDraw) {
		return new ArrayList<>(tabu.run(new GSDuplicateShiftSolution(
				basicSampler.draw(numberOfDraw), sample)).getSolution());
	}
	
	@Override
	public Set<APopulationEntity> drawUnique(int numberOfDraw) {
		return new HashSet<>(tabu.run(new GSUniqueShiftSolution(
				basicSampler.drawUnique(numberOfDraw), sample)).getSolution());
	}

	@Override
	public void setSample(Collection<APopulationEntity> sample) {
		this.sample = sample;
		this.basicSampler = new RandomSampler();
		basicSampler.setSample(sample);
	}

	@Override
	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives) {
		this.tabu.addObjectives(objectives);
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
}
