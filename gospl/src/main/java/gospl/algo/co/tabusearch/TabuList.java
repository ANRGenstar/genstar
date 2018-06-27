package gospl.algo.co.tabusearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

public class TabuList implements ITabuList {
	
	List<ISyntheticPopulationSolution> tabuList = new ArrayList<>();
	private int maxSize;
	
	public TabuList(int size){
		if(size < 1)
			this.maxSize = 1;
		else
			this.maxSize = size;
	}

	@Override
	public Iterator<ISyntheticPopulationSolution> iterator() {
		return tabuList.iterator();
	}

	@Override
	public void add(ISyntheticPopulationSolution solution) {
		if(tabuList.size() == maxSize)
			tabuList.remove(tabuList.get(0));
		tabuList.add(solution);
	}

	@Override
	public boolean contains(ISyntheticPopulationSolution solution) {
		return tabuList.contains(solution);
	}
	
	@Override
	public int maxSize() {
		return maxSize;
	}
	
	@Override
	public int getSize() {
		return tabuList.size();
	}

	@Override
	public void updateSize(Integer currentIteration, ISyntheticPopulationSolution bestSolutionFound) {
		// TODO Auto-generated method stub
		
	}

}
