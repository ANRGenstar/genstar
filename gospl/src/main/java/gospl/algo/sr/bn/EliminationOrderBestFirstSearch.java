package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EliminationOrderBestFirstSearch {

	public static final class NodeExplored implements Comparable<NodeExplored> {
		
		public final List<NodeCategorical> prefix;
		public final int width;
		
		/**
		 * subgraph which results of applying prefix to the original graph
		 */
		public final MoralGraph subgraph;
		
		/**
		 * lower bound on the treewidth of subgraph
		 */
		public final int lowerbound;

		public final int max;
		
		public NodeExplored(List<NodeCategorical> prefix, int width, MoralGraph subgraph, int lowerbound) {
			this.prefix = prefix;
			this.width = width;
			this.subgraph = subgraph;
			this.lowerbound = lowerbound;
			this.max = Math.max(lowerbound, width);
		}


		@Override
		public int compareTo(NodeExplored o) {
			return this.max - o.max;
		}


		@Override
		public boolean equals(Object obj) {
			try {
				NodeExplored other = (NodeExplored)obj;
				return subgraph.variables().equals(other.subgraph.variables()) && prefix.equals(other.prefix);
			} catch (ClassCastException e) {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return subgraph.variables().hashCode();
		}
	
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(); 
			sb.append("prefix [").append(prefix.stream().map(v->v.name).collect(Collectors.joining(","))).append("]");
			sb.append(" / graph {");
			sb.append(subgraph.variables().stream().map(v->v.name).collect(Collectors.joining(","))).append("}");
			sb.append(" (width:").append(width).append(", lowerbound:").append(lowerbound);
			return sb.toString();
		}
		
	}
	
	protected Map<Set<NodeCategorical>,NodeExplored> openList = new HashMap<>();
	protected SortedSet<NodeExplored> openListSorted = new TreeSet<>();

	protected Map<Set<NodeCategorical>,NodeExplored> closedList = new HashMap<>();
	
	private Logger logger = LogManager.getLogger();
	private final MoralGraph g;

	private long totalExplored =  0;
	
	private EliminationOrderBestFirstSearch(MoralGraph g) {
		this.g = g;
	}
	

	private void addOpenList(NodeExplored n) {

		openList.put(n.subgraph.variables(), n);
		openListSorted.add(n);
		
	}
	
	private void removeOpenList(NodeExplored n) {
		openList.remove(openList.entrySet().stream()
				.filter(e -> e.getValue().equals(n)).findAny()
				.orElseThrow(NullPointerException::new).getKey()
				);
		openListSorted.remove(n);
	}
	
	/**
	 * Returns an optimal elimination order using the deep first search algorithm
	 * (see algo 19 named DFS_OEO Darwiche p291)
	 * @return
	 */
	protected List<NodeCategorical> computeEliminationOrder() {
		
		logger.info("searching for elimination order in {}", g.variables().stream().map(v -> v.name).collect(Collectors.joining(",")));

		addOpenList(new NodeExplored(Collections.emptyList(), 0, g, g.getLowerBoundFromClique()));
		
		totalExplored =  0;
		
		while (!openList.isEmpty()) {
			
			// TODO is it too late ? 
			
			// take the best node so far
			NodeExplored node = openListSorted.first();
			removeOpenList(node);
			
			logger.debug("exploring best elimination orders based on {}", node);


			// maybe this one is the best already ? 
			if (node.subgraph.isEmpty()) {
				logger.info("found an optimal elimination order having width {}: {} ({} iterations)", 
						node.width, 
						node.prefix.stream().map(v -> v.name).collect(Collectors.joining(",")), 
						totalExplored
								);
				return node.prefix;
			}
			
			// or not... :-/
			for (NodeCategorical n: node.subgraph.variables()) {
				
				logger.trace("we might add node {} to {}", n, node.prefix);

				totalExplored++;
				
				List<NodeCategorical> prefix2 = new ArrayList<>(node.prefix.size()+1);
				prefix2.addAll(node.prefix);
				prefix2.add(n);
				
				MoralGraph g2 = node.subgraph.clone();
				g2.remove(n);
				
				int width2 = Math.max(node.subgraph.getNeighboors(n), node.width);
				
				int lowerbound2 = g2.getLowerBoundFromClique();
				
				NodeExplored alreadyExplored = openList.get(g2.variables());
				if (alreadyExplored !=null) {
					if (width2 < alreadyExplored.width) {

						logger.debug("we already explored {} but we found a better width {} (instead of {}); keeping this better solution", g2.variables(), width2, alreadyExplored.width);
						
						openList.remove(openList.entrySet().stream()
								.filter(e -> e.getValue().equals(n)).findAny()
								.orElseThrow(NullPointerException::new).getKey()
								);
						addOpenList(new NodeExplored(
								prefix2, 
								width2, 
								g2, 
								lowerbound2
								));
						
					} else {
						logger.trace("we already explored {} with a better width {} (instead of {}); keeping this old solution", g2.variables(), alreadyExplored.width, width2);
					}
				} else {
					alreadyExplored = closedList.get(g2.variables());
					if (alreadyExplored == null) {
						// its the first time we visited this; store that for later memory
						addOpenList(new NodeExplored(
								prefix2, 
								width2, 
								g2, 
								lowerbound2
								));
					}
					
				}
			
				
				
			}
			
			// we processed this node; consider it closed. 
			closedList.put(node.subgraph.variables(),node);
		}
		
		throw new RuntimeException("oops, we where not able to find any optimal elimination order...");
		
		
	}
	

	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn) {
		EliminationOrderBestFirstSearch dfs = new EliminationOrderBestFirstSearch(new MoralGraph(bn));
		return dfs.computeEliminationOrder();
	}

	
	public static List<NodeCategorical> computeEliminationOrder(CategoricalBayesianNetwork bn, Set<NodeCategorical> consideredNodes) {
		EliminationOrderBestFirstSearch dfs = new EliminationOrderBestFirstSearch(new MoralGraph(bn, consideredNodes));
		return dfs.computeEliminationOrder();
	}

}
