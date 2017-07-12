package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the elimination order as defined by Derwiche in algo DFS_OEO,  
 * with an optimization based on his theorem 9.2.
 * 
 * TODO for performance: add the seed order 
 * TODO for performance: implement a best-first algo as in 
 * 
 * @author Samuel Thiriot
 *
 */
public final class EliminationOrderDeepFirstSearch {

	public static int CACHE_ALREADY_EXPLORED = 100000;
	
	private Logger logger = LogManager.getLogger();

	private final MoralGraph g;
	
	private List<NodeCategorical> bestEliminationOrder = null;
	private int bestWidth = Integer.MAX_VALUE;
	
	private LRUMap<Set<NodeCategorical>,Set<Set<NodeCategorical>>> alreadyExplored = new LRUMap<>(CACHE_ALREADY_EXPLORED);
	
	private long totalExplored =  0;

	private EliminationOrderDeepFirstSearch(MoralGraph g) {
		
		this.g = g;
	}
	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn) {
		return computeEliminationOrder(bn, 5);
	}
	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn, int maxTimeToSearchMinutes) {
		EliminationOrderDeepFirstSearch dfs = new EliminationOrderDeepFirstSearch(new MoralGraph(bn));
		return dfs.computeEliminationOrder(maxTimeToSearchMinutes);
	}
	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn, Set<NodeCategorical> consideredNodes) {
		return computeEliminationOrder(bn, consideredNodes, 5);
	}
	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn, Set<NodeCategorical> consideredNodes, int maxTimeToSearchMinutes) {
		EliminationOrderDeepFirstSearch dfs = new EliminationOrderDeepFirstSearch(new MoralGraph(bn, consideredNodes));
		return dfs.computeEliminationOrder(maxTimeToSearchMinutes);
	}

	/**
	 * Returns an optimal elimination order using the deep first search algorithm
	 * (see algo 19 named DFS_OEO Darwiche p291)
	 * @return
	 */
	protected List<NodeCategorical> computeEliminationOrder(int maxTimeToSearchMinutes) {
		
		totalExplored = 0;
		
		computeEliminationOrderAux(
				this.g,
				Collections.emptyList(),
				0,
				maxTimeToSearchMinutes*60*1000,
				System.currentTimeMillis()
				);
		
		logger.info("found best elimination order having width {} : {} (in {} iterations)", bestWidth, bestEliminationOrder, totalExplored);
		
		alreadyExplored.clear();
		// suggest a gc, as we released quiet a large amount of memory 
		Runtime.getRuntime().gc();
		return bestEliminationOrder;
		
	}
	
	
	protected void computeEliminationOrderAux(
			MoralGraph subGraph,
			List<NodeCategorical> eliminationPrefix,
			int width,
			long maxTimeToSearchMilli,
			long timestampStarted
			) {
		
		if (bestEliminationOrder != null & System.currentTimeMillis() - timestampStarted > maxTimeToSearchMilli) {
			logger.warn("don't have more time to explore the elimination order. stopping there");
			return;
		}
		logger.trace("studying subgraph {} having width {}", subGraph, width);

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
				
				totalExplored++;
				
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
					InferencePerformanceUtils.singleton.incCacheMiss();
				} else {
				}
				if (computedForThisSubgraph.contains(eliminationPrefix2set)) {
					logger.trace("we already explored the prefix {}, pruning", eliminationPrefix2);
					InferencePerformanceUtils.singleton.incCacheHit();
					continue;
				}
				
				int width2 = Math.max(width, countNeighboors);
				
				// recursive search
				computeEliminationOrderAux(subGraph2, eliminationPrefix2, width2, maxTimeToSearchMilli, timestampStarted);
				
				// store in cache to avoid useless further computations
				computedForThisSubgraph.add(eliminationPrefix2set);
				
			}
		}
		
	}



}
