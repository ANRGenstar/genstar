package gospl.algo.sr.bn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * represent a Bayesian Network (Directed Acyclic Graph) made of NodeCategorical. 
 * Can add nodes, get them in their natural order. 
 * Also provides the possibility to read and write in XMLBIF format. 
 * 
 * @author Samuel Thiriot
 *
 */
public class BayesianNetwork<N extends AbstractNode<N>> {

	private Logger logger = LogManager.getLogger();

	protected Set<N> nodes = new HashSet<>();
	
	protected Map<String,N> name2node = new HashMap<>();
	
	protected List<N> nodesEnumeration;

	private final String name;
	
	/**
	 * Creates a network.
	 * @param name
	 */
	public BayesianNetwork(String name) {
		this.name = name;
	}

	/**
	 * add a node inside the network if this nodes does not already exists there. 
	 * @param n
	 */
	public void add(N n) {
		notifyNodesChanged();
		nodes.add(n);
	}
	
	/**
	 * adds a set of nodes, at least the ones not added yet. 
	 * @param nns
	 */
	public void addAll(Collection<N> nns) {
		notifyNodesChanged();
		nodes.addAll(nns);
	}
	
	/**
	 * returns true if this node already belongs this network
	 * @param n
	 * @return
	 */
	public boolean containsNode(N n) {
		return nodes.contains(n);
	}
	
	/**
	 * returns the NodeCategorical having the id asked for .
	 * @param id
	 * @return
	 */
	public N getVariable(String id) {
		
		N res = name2node.get(id);
		if (res != null)
			return res;
		
		for (N n: nodes) {
			if (n.getName().equals(id)) {
				name2node.put(id, n);
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Called when there is a change in the state of the network). Resets 
	 * internal information that will be recomputed on demand.
	 */
	public void notifyNodesChanged() {
		nodesEnumeration = null;
		name2node.clear();
	}
	
	/**
	 * returns the nodes in their order for browing them from root to leafs
	 * @return
	 */
	public List<N> enumerateNodes() {
		
		if (nodesEnumeration != null)
			return nodesEnumeration;
		
		nodesEnumeration = new LinkedList<>();
		
		List<N> toProcess = new LinkedList<>(nodes);
		
		insert: while (!toProcess.isEmpty()) {
		
			N n = toProcess.remove(0);
			logger.debug("considering {}", n.name);

			int idxMin = 0;
			for (N p: n.getParents()) {
				int idxParent = nodesEnumeration.indexOf(p);
				if (idxParent == -1) {
					// reinsert in the queue for further process
					toProcess.add(n);
					continue insert;
				} else  if (idxParent+1 > idxMin) {
					// this node was already added to our network. 
					idxMin = idxParent+1;
				}
				
			}
			nodesEnumeration.add(idxMin, n);
			logger.debug("order for nodes: {}", nodesEnumeration);
		}
		
		
		logger.debug("order for nodes: {}", nodesEnumeration);
	
		return nodesEnumeration;
	}
	
	/**
	 * returns all the nodes. 
	 * @return
	 */
	public Set<N> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
	
	/**
	 * @url http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/
	 * @param f
	 * @throws FileNotFoundException
	 */
	public void saveAsXMLBIF(File f) throws FileNotFoundException {

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<BIF VERSION=\"0.3\">\n<NETWORK>\n");
		sb.append("<NAME>").append(StringEscapeUtils.escapeXml10(name)).append("</NAME>\n");

		for (N n: enumerateNodes())
			n.toXMLBIF(sb);
			
		sb.append("</NETWORK>\n</BIF>\n");

		PrintStream ps = new PrintStream(f);
		ps.print(sb.toString());
		ps.close();
	}
	
	public void saveAsXMLBIF(String filename) throws FileNotFoundException {
		
		saveAsXMLBIF(new File(filename));
		
	}
	
	/**
	 * returns true if every node is valid
	 * @return
	 */
	public boolean isValid() {
		
		for (N n: nodes) {
			if (!n.isValid())
				return false;
		}
		
		return true;
	}
	
	public Map<N,List<String>> collectInvalidProblems() {
		
		Map<N,List<String>> res = new HashMap<>();
		for (N n: nodes) {
			List<String> problems = n.collectInvalidityReasons();
			if (problems != null)
				res.put(n, problems);
		} 
		
		return res;
	}
	
	
	public Set<N> getAllAncestors(N n) {

		Set<N> res = new HashSet<>(n.getParents());
		res.add(n);
		
		Set<N> toProcess = new HashSet<>(n.getParents());
		Set<N> processed = new HashSet<>();
		
		while (!toProcess.isEmpty()) {
			Iterator<N> it = toProcess.iterator();
			N c = it.next();
			it.remove();
			processed.add(c);
			res.addAll(c.getParents());
			toProcess.addAll(c.getParents());
			toProcess.removeAll(processed);
		}
		
		return res;
	}
	
	public Set<N> getParents(AbstractNode<N> n) {
		return n.getParents();
	}		

	public Set<N> getChildren(AbstractNode<N> n) {
		Set<N> res = null;
		for (N node: nodes) {
			if (node.getParents().contains(n)) {
				if (res == null)
					res = new HashSet<>();
				res.add(node);
			}
		}
		if (res == null)
			return Collections.emptySet();
		return res;
	}
	
	
	
	/*
	private BigDecimal getCached(
			Map<NodeCategorical,String> node2value, 
			Map<NodeCategorical,String> evidence) {
		
		Map<Map<NodeCategorical,String>,BigDecimal> res = evidence2event2proba.get(evidence);
		if (res == null)
			return null;
		return res.get(node2value);
		
	}
	
	private void storeCache(
			Map<NodeCategorical,String> node2value, 
			Map<NodeCategorical,String> evidence,
			BigDecimal d
			) {
		Map<Map<NodeCategorical,String>,BigDecimal> res = evidence2event2proba.get(evidence);
		if (res == null) {
			res = new HashMap<>();
			evidence2event2proba.put(evidence, res);
		}
		res.put(node2value, d);
	}
	*/
	
	
	
}
