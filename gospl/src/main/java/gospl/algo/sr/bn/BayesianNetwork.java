package gospl.algo.sr.bn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

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
		for (N n: nodes) {
			if (n.getName().equals(id))
				return n;
		}
		return null;
	}
	
	/**
	 * Called when there is a change in the state of the network). Resets 
	 * internal information that will be recomputed on demand.
	 */
	public void notifyNodesChanged() {
		nodesEnumeration = null;
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
	
	/**
	 * reads a bayesian network from a String containing a network description in XMLBIF format. 
	 * @param xmlStr
	 * @return
	 */
	public static BayesianNetwork<NodeCategorical> readFromXMLBIF(String xmlStr) {
		
        Document document;
		try {
			document = DocumentHelper.parseText(xmlStr);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalArgumentException("invalid XML BIF format", e);
		}


		
    	final String networkName = document.selectSingleNode("/BIF/NETWORK/NAME").getText().trim();

		// add them all into a Bayesian net
		BayesianNetwork<NodeCategorical> bn = new BayesianNetwork<>(networkName);
		
        // read the variables
		Map<String,NodeCategorical> id2node = new HashMap<>();
		{
	        List<?> variables = document.selectNodes("/BIF/NETWORK/VARIABLE");
	        for (Iterator<?> iterVars = variables.iterator(); iterVars.hasNext(); ) {
	            
	        	Node nodeVariable = (Node)iterVars.next();
	        	
	        	final String variableName = nodeVariable.selectSingleNode("./NAME").getText().trim();
	        	
	        	NodeCategorical n = new NodeCategorical(bn, variableName);
	        	id2node.put(variableName, n);
	        	
	        	for (Object nodeRaw : nodeVariable.selectNodes("./OUTCOME")) {
	        		Node nodeOutcome = (Node)nodeRaw;
	        		n.addDomain(nodeOutcome.getText().trim());
	        	}
	        
	        }
		}
		
		// read the definitions
		{
	        List<?> definitions = document.selectNodes("/BIF/NETWORK/DEFINITION");
	        for (Iterator<?> iterDefinition = definitions.iterator(); iterDefinition.hasNext(); ) {
	            
	        	Node nodeDefinition = (Node)iterDefinition.next();
	        	
	        	// decode to which variable this definition is related
	        	final String variableName = nodeDefinition.selectSingleNode("./FOR").getText().trim();
	        	final NodeCategorical n = id2node.get(variableName);
	        	
	        	// find and add the parents
	        	for (Object nodeGivenRaw: nodeDefinition.selectNodes("./GIVEN")) {
	        		Node nodeGiven = (Node)nodeGivenRaw;
	        		NodeCategorical np = id2node.get(nodeGiven.getText().trim());
	        		if (np == null)
	        			throw new IllegalArgumentException("unknown node "+nodeGiven.getText().trim());
	        		n.addParent(np);
	        	}
	        	
	        	// now set the probabilities
	        	final String tableContent = nodeDefinition.selectSingleNode("./TABLE").getText().trim();
	        	List<BigDecimal> values = new LinkedList<>();
	        	for (String tok : tableContent.split("[ \t]+")) {
	        		try {
	        			values.add(new BigDecimal(tok));
	        		} catch (NumberFormatException e) {
	        			throw new IllegalArgumentException("error while parsing this value as a BigDecimal: "+tok,e);
	        		}
	        	}
	        	BigDecimal[] valuesA = new BigDecimal[values.size()];
	        	values.toArray(valuesA);
	        	n.setProbabilities(valuesA);
	        
	        }
		
		}
		
		bn.addAll(id2node.values());

		// check it is ok
		if (!bn.isValid()) {
			
			throw new IllegalArgumentException("the bn is not valid: "+
				bn.collectInvalidProblems()
				.entrySet()
				.stream()
				.map(k2v -> (String)(k2v.getKey().getName() + ":" + k2v.getValue()))
				.collect(Collectors.joining(", ")));
		}
		
		return bn;
	}
	
	/**
	 * Loads a network from a file which contains a Bayesian network .
	 * @param f
	 * @return
	 */
	public static BayesianNetwork<NodeCategorical> loadFromXMLBIF(File f) {
		
		try {
			return readFromXMLBIF(FileUtils.readFileToString(f));
		} catch (IOException e) {
			throw new IllegalArgumentException("unable to read file "+f, e);
		}
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
			return Collections.EMPTY_SET;
		return res;
	}
	
	/**
	 * ranks the nodes per count of zeros in the CPT 
	 * @param all
	 * @return
	 */
	protected List<NodeCategorical> rankVariablesPerZeros(Collection<NodeCategorical> all) {
		
		//logger.info("should rank variables {}", all);
		List<NodeCategorical> res = new ArrayList<>(all);
		
		Collections.sort(res, new Comparator<NodeCategorical>() {

			@Override
			public int compare(NodeCategorical n1, NodeCategorical n2) {
				
				return n2.getCountOfZeros() - n1.getCountOfZeros();

			}
			
		});
		
		//logger.info("ranked variables {}", res);
		
		return res;
	}

	
	private Map<Map<NodeCategorical,String>,Map<Map<NodeCategorical,String>,BigDecimal>> evidence2event2proba = new HashMap<>();
	
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
	
	/**
	 * For a list of variable and values, computes the corresponding joint probability.
	 * Takes as an input all the 
	 * @param node2value
	 * @param node2probabilities 
	 * @return
	 */
	public BigDecimal jointProbability(
			Map<NodeCategorical,String> node2value, 
			Map<NodeCategorical,String> evidence) {
		
		/*BigDecimal res = getCached(evidence, node2value);
		if (res != null)
			return res;
		*/
		logger.trace("computing joint probability p({})", node2value);

		BigDecimal res = BigDecimal.ONE;
		
		for (NodeCategorical n: rankVariablesPerZeros(node2value.keySet())) {
			
			// optimisation: compute first the nodes having a lot of zeros to stop computation asap
			String v = node2value.get(n);
			
			if (!node2value.keySet().containsAll(n.getParents())) {
				throw new InvalidParameterException("wrong parameters: expected values for each parent of "+n+": "+n.getParents());
			}
			BigDecimal p = null;
			
			// find the probability to be used
			if (evidence.containsKey(n)) {
				if (evidence.get(n).equals(v)) {
					p = BigDecimal.ONE;
				} else {
					p = BigDecimal.ZERO;
				}
			} else if (n.hasParents()) {
				// if there are parent values, let's create the probability by reading the CPT 
				Map<NodeCategorical,String> s = n.getParents().stream().collect(Collectors.toMap(p2 -> p2, p2 -> node2value.get(p2)));
				p = n.getProbability(v, s);
			} else {
				// no parent values. Let's use the ones of our CPT
				p = n.getProbability(v);
			}
			logger.trace("p({}={})={}", n.name, v, p);

			// use it
			if (p.compareTo(BigDecimal.ZERO)==0) {
				// optimisation: stop if multiplication by 0
				res = BigDecimal.ZERO;
				break;
			} else if (p.compareTo(BigDecimal.ONE) != 0) {
				// optimisation: multiply only if useful
				res = res.multiply(p);
				InferencePerformanceUtils.singleton.incMultiplications();
			}
			
		}
		
		logger.debug("computed joint probability p({})={}", node2value, res);

		//storeCache(node2value, evidence, res);
		
		return res;
		
	}
	
	
}
