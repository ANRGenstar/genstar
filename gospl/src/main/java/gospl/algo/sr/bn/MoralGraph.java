package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class MoralGraph extends SingleGraph {

	private Logger logger = LogManager.getLogger();

	protected Map<NodeCategorical,String> variable2nodeId = new HashMap<>();
	protected Map<String,NodeCategorical> nodeId2variable = new HashMap<>();
	
	protected final CategoricalBayesianNetwork bn;
	
	/**
	 * Moralization of a graph from a Bayesian network
	 * @param bn
	 */
	public MoralGraph(CategoricalBayesianNetwork bn) {
		
		this(bn, bn.getNodes());
	}

	public MoralGraph(CategoricalBayesianNetwork bn, Set<NodeCategorical> nodes) {
		super("bayesian_moral", true, false);
		
		this.bn = bn;
		
		// first add nodes
		for (NodeCategorical n: bn.enumerateNodes()) {
			
			if (!nodes.contains(n))
				continue;
			
			// create this node
			String id = Integer.toString(variable2nodeId.size()+1);
			
			logger.trace("adding node {} with id {}", n, id);

			this.addNode(id);
			variable2nodeId.put(n, id);
			nodeId2variable.put(id, n);
			
		}
		
		// add links from variable to parent
		for (NodeCategorical n: bn.enumerateNodes()) {
			
			if (!nodes.contains(n))
				continue;
			
			String id = variable2nodeId.get(n);
			
			// add edges for each link of parent and children
			for (NodeCategorical p: n.getParents()) {
				
				if (!nodes.contains(p))
					continue;
				
				String idTo = variable2nodeId.get(p);
				
				logger.trace("adding edge {}->{} between parent {} and children {}", idTo, id, p, n);

				addEdge(
						idTo+"->"+id, 
						idTo, 
						id
						);
				
			}

				
		}
		
		
		// add links between parents 
		for (NodeCategorical n: bn.enumerateNodes()) {
			
			if (!nodes.contains(n))
				continue;
			
			String id = variable2nodeId.get(n);
			
			for (NodeCategorical p: n.getParents()) {
				
				if (!nodes.contains(p))
					continue;
				
				String idTo = variable2nodeId.get(p);
			
				// link all the parents
				for (NodeCategorical p2: n.getParents()) {
					
					if (!nodes.contains(p2))
						continue;
					
					if (p==p2)
						continue;
					
					String idTo2 = variable2nodeId.get(p2);

					try {
						addEdge(
							idTo+"--"+idTo2, 
							idTo, 
							idTo2
							);
						logger.trace("adding edge {}--{} between parents {} and {} of {}", idTo, idTo2, p, p2, n);

						
					} catch (EdgeRejectedException e) {
						// ignore it
					} catch (IdAlreadyInUseException e2) {
						// ignore it
					}
				}
				
			}

				
		}
				
	}

	public MoralGraph clone() {
		return new MoralGraph(this.bn, variables());
	}
	
	/**
	 * removes a node from the subgraph
	 * @param n
	 */
	public void remove(NodeCategorical n) {
		String id = variable2nodeId.remove(n);
		nodeId2variable.remove(id);
		removeNode(id);
	}
	
	/**
	 * returns the count of cliques
	 * @return
	 */
	public int getMaxCliqueSize() {
		
		int biggestSize = 0;
		
		for (List<Node> clique : Toolkit.getMaximalCliques(this)) {
			if (clique.size() > biggestSize)
				biggestSize = clique.size();
		}
		
		return biggestSize;
	}

	public int getLowerBoundFromClique() {
		return getMaxCliqueSize()-1;
	}

	public boolean isEmpty() {
		return variable2nodeId.isEmpty();
	}

	public Set<NodeCategorical> variables() {

		return variable2nodeId.keySet();
	}

	public boolean contains(NodeCategorical p) {
		return variable2nodeId.containsKey(p);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (NodeCategorical n : variables()) {
			sb.append(n.name);
			sb.append(",");
		}
		return sb.toString();
	}

	public int getNeighboors(NodeCategorical n) {
		return super.getNode(variable2nodeId.get(n)).getDegree();
	}
}
