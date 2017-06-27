package gospl.algo.sr.bn;

import java.io.File;
import java.io.IOException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

public class CategoricalBayesianNetwork extends BayesianNetwork<NodeCategorical> {

	private Logger logger = LogManager.getLogger();

	protected Map<NodeCategorical,Factor> node2factor = new HashMap<>();
	
	public CategoricalBayesianNetwork(String name) {
		super(name);

	}
	
	/**
	 * Returns the node as a factor, or the corresponding value 
	 * @param n
	 * @return
	 */
	public Factor getFactor(NodeCategorical n) {
		
		// cached ? 
		Factor res = node2factor.get(n);
		
		if (res == null) {
			res = n.asFactor();
			node2factor.put(n, res);
		}
		
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


	/**
	 * reads a bayesian network from a String containing a network description in XMLBIF format. 
	 * @param xmlStr
	 * @return
	 */
	public static CategoricalBayesianNetwork readFromXMLBIF(String xmlStr) {
		
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
    	CategoricalBayesianNetwork bn = new CategoricalBayesianNetwork(networkName);
		
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
	public static CategoricalBayesianNetwork loadFromXMLBIF(File f) {
		
		try {
			return readFromXMLBIF(FileUtils.readFileToString(f));
		} catch (IOException e) {
			throw new IllegalArgumentException("unable to read file "+f, e);
		}
	}
	
	public Set<NodeCategorical> getAllAncestors(NodeCategorical n) {

		Set<NodeCategorical> res = new HashSet<>(n.getParents());
		res.add(n);
		
		Set<NodeCategorical> toProcess = new HashSet<>(n.getParents());
		Set<NodeCategorical> processed = new HashSet<>();
		
		while (!toProcess.isEmpty()) {
			Iterator<NodeCategorical> it = toProcess.iterator();
			NodeCategorical c = it.next();
			it.remove();
			processed.add(c);
			res.addAll(c.getParents());
			toProcess.addAll(c.getParents());
			toProcess.removeAll(processed);
		}
		
		return res;
	}
	

	protected List<NodeCategorical> rankVariablesForMultiplication(Collection<NodeCategorical> nodes) {
		// by default, does nothing of interest.
		// should be overriden with something more smart.
		return new ArrayList<NodeCategorical>(nodes);
	}
	
	public IteratorCategoricalVariables iterateDomains() {
		
		return new IteratorCategoricalVariables(this.enumerateNodes());
	}
	
	public IteratorCategoricalVariables iterateDomains(Collection<NodeCategorical> nn) {
		if (!this.nodes.containsAll(nn))
			throw new IllegalArgumentException("some of these nodes "+nn+" do not belong this Bayesian network "+this.nodes);
		return new IteratorCategoricalVariables(nn);
	}
	
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
	
	public BigDecimal jointProbabilityFromFactors(Map<NodeCategorical,String> node2value) {
		
		Map<NodeCategorical,String> remaining = new HashMap<>(node2value);
		
		Factor f = null;
		
		for (NodeCategorical n: enumerateNodes()) {
			Factor fn = n.asFactor();
			if (f == null)
				f = fn;
			else 
				f = f.multiply(fn);
		}
		
		return f.get(node2value);
	}

	/**
	 * Prunes the Bayesian network by removing the variable n, and updating all the probabilities for
	 * other nodes in the network. 
	 * @param node
	 */
	public void prune(NodeCategorical n) {
		
	}
		
}
