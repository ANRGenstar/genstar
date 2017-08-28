package gospl.algo.sr.bn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Iterates over the combinations of values for the domains of each variable passed at initialization.
 * 
 * @author Samuel Thiriot
 *
 */
public final class IteratorCategoricalVariables implements Iterator<Map<NodeCategorical,String>> {

	
	/**
	 * the nodes we have to explore the values for 
	 */
	private final NodeCategorical[] nuisance;
	
	/**
	 * the current values we are exploring for each node
	 */
	private final int[] nodeIdx2valueIdx;
	
	private boolean hasNext;
	
	
	public IteratorCategoricalVariables(Collection<NodeCategorical> variables) {
				
		// store the variables in a way which can be accessed quickly in an indexed way
		this.nuisance = new NodeCategorical[variables.size()];
		variables.toArray(nuisance);
		
		// store the initial indices we explore (initialized at 0)
		this.nodeIdx2valueIdx = new int[nuisance.length];
		
		// at the beginning, we have something to explore if we have at least one variable having a value in its domain
		this.hasNext = true;
		/*
		for (NodeCategorical n: this.nuisance) {
			if (n.getDomainSize() > 0) {
				this.hasNext = true;
				break;
			}
		}*/
		
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public Map<NodeCategorical, String> next() {
		
		// create the combination we explore now
		Map<NodeCategorical,String> n2v = new HashMap<>(nuisance.length);
		for (int i=0; i<nodeIdx2valueIdx.length; i++) {
			n2v.put(nuisance[i], nuisance[i].getValueIndexed(nodeIdx2valueIdx[i]));
		}
		
		// skip to the next index
		int cursorParents = nodeIdx2valueIdx.length-1;
		if (nodeIdx2valueIdx.length == 0) {
			hasNext = false; 
		} else {
			nodeIdx2valueIdx[cursorParents]++;
			// ... if we are at the max of the domain size of the current node, then shift back
			while (nodeIdx2valueIdx[cursorParents] >= nuisance[cursorParents].getDomainSize()) {
				nodeIdx2valueIdx[cursorParents] = 0;
				cursorParents--;
				// maybe we finished the exploration ?
				if (cursorParents < 0) {
					hasNext = false;
					break;
				}
				// skip to the next one 
				nodeIdx2valueIdx[cursorParents]++;
			}
		}
		
		return n2v;
	}

	
}
