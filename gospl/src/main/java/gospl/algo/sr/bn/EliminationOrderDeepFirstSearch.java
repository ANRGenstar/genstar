package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the elimination order as defined by Derwiche in algo DFS_OEO,  
 * with an optimization based on his theorem 9.2
 * 
 * @author Samuel Thiriot
 *
 */
public class EliminationOrderDeepFirstSearch {

	private Logger logger = LogManager.getLogger();

	private final MoralGraph g;
	
	private List<NodeCategorical> bestEliminationOrder = null;
	private int bestWidth = Integer.MAX_VALUE;
	
	private Map<Set<NodeCategorical>,Set<Set<NodeCategorical>>> alreadyExplored = new HashMap<>();
	
	private EliminationOrderDeepFirstSearch(MoralGraph g) {
		
		this.g = g;
	}
	
	public static List<NodeCategorical> computeEliminationOrderDeepFirstSearch(CategoricalBayesianNetwork bn) {
		EliminationOrderDeepFirstSearch dfs = new EliminationOrderDeepFirstSearch(new MoralGraph(bn));
		return dfs.computeEliminationOrderDeepFirstSearch();
	}

	/**
	 * Returns an optimal elimination order using the deep first search algorithm
	 * (see algo 19 named DFS_OEO Darwiche p291)
	 * @return
	 */
	protected List<NodeCategorical> computeEliminationOrderDeepFirstSearch() {
		
		computeEliminationOrderDeepFirstSearchAux(
				this.g,
				Collections.emptyList(),
				0
				);
		
		logger.info("found best elimination order having width {} : {}", bestWidth, bestEliminationOrder);
		return bestEliminationOrder;
		
	}
	
	
	protected void computeEliminationOrderDeepFirstSearchAux(
			MoralGraph subGraph,
			List<NodeCategorical> eliminationPrefix,
			int width
			) {
		
		logger.debug("studying subgraph {} having width {}", subGraph, width);

		if (subGraph.isEmpty()) {
			// we have a complete order !
			logger.debug("found complete order having width {}", width);

			if (width < bestWidth) {
				bestEliminationOrder = eliminationPrefix;
				bestWidth = width;
				logger.debug("current best order having {}: {}", bestWidth, bestEliminationOrder);
			}
		} else {
			
			int b = subGraph.getLowerBoundFromClique();
					//lowerBoundWidth(subGraph);
			
			if (Math.max(width, b) >= bestWidth) {
				logger.trace("pruning because lowerbound {} and current width {} are less promising than {}.", b, width, bestWidth);
				return;
			}
			
			for (NodeCategorical n: subGraph.variables()) {
				
				logger.trace("exploring {}", n);

				int countNeighboors = subGraph.getNeighboors(n);
			
				MoralGraph subGraph2 = subGraph.clone();
				subGraph2.remove(n);
				
				List<NodeCategorical> eliminationPrefix2 = new ArrayList<>(eliminationPrefix);
				eliminationPrefix2.add(n);
				
				Set<NodeCategorical> eliminationPrefix2set = new HashSet<>(eliminationPrefix2);
				Set<Set<NodeCategorical>> computedForThisSubgraph = alreadyExplored.get(subGraph2.variables());
				if (computedForThisSubgraph == null) {
					computedForThisSubgraph = new HashSet<>();
					alreadyExplored.put(subGraph2.variables(), new HashSet<>());
				}
				if (computedForThisSubgraph.contains(eliminationPrefix2set)) {
					logger.trace("we already explored the prefix {}, pruning", eliminationPrefix2);
					continue;
				}
				
				int width2 = Math.max(width, countNeighboors);
				
				// recursive search
				computeEliminationOrderDeepFirstSearchAux(subGraph2, eliminationPrefix2, width2);
				
				// store in cache to avoid useless further computations
				computedForThisSubgraph.add(eliminationPrefix2set);
				
			}
		}
		
	}



}
