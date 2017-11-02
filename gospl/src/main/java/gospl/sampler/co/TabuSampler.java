package gospl.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.metamodel.entity.ADemoEntity;
import gospl.algo.co.tabusearch.TabuSearch;
import gospl.algo.co.tabusearch.list.GSTabuList;
import gospl.algo.co.tabusearch.solution.GSDuplicateShiftSolution;
import gospl.algo.co.tabusearch.solution.GSUniqueShiftSolution;

public class TabuSampler extends AOptiAlgoSampler<TabuSearch> {

	private Collection<ADemoEntity> sample;
	private RandomSampler basicSampler;
	
	public TabuSampler(int maxIterations, int tabuListSize) {
		this.algorithm = new TabuSearch(new GSTabuList(tabuListSize), 
				maxIterations);
	}

	@Override
	public List<ADemoEntity> draw(int numberOfDraw) {
		return new ArrayList<>(this.algorithm.run(new GSDuplicateShiftSolution(
				basicSampler.draw(numberOfDraw), sample)).getSolution());
	}
	
	@Override
	public Set<ADemoEntity> drawUnique(int numberOfDraw) {
		return new HashSet<>(this.algorithm.run(new GSUniqueShiftSolution(
				basicSampler.drawUnique(numberOfDraw), sample)).getSolution());
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
}
