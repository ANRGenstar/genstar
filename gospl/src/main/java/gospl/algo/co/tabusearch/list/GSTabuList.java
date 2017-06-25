package gospl.algo.co.tabusearch.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gospl.algo.co.metamodel.IGSSampleBasedCOSolution;

public class GSTabuList implements ITabuList {
	
	List<IGSSampleBasedCOSolution> tabuList = new ArrayList<>();
	private int maxSize;
	
	public GSTabuList(int size){
		if(size < 1)
			this.maxSize = 1;
		else
			this.maxSize = size;
	}

	@Override
	public Iterator<IGSSampleBasedCOSolution> iterator() {
		return tabuList.iterator();
	}

	@Override
	public void add(IGSSampleBasedCOSolution solution) {
		if(tabuList.size() < maxSize){
			tabuList.remove(tabuList.get(0));
			tabuList.add(solution);
		} else
			tabuList.add(solution);
	}

	@Override
	public Boolean contains(IGSSampleBasedCOSolution solution) {
		return tabuList.contains(solution);
	}

	@Override
	public void updateSize(Integer currentIteration, IGSSampleBasedCOSolution bestSolutionFound) {
		// TODO Auto-generated method stub

	}

}
