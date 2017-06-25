package gospl.algo.sr.bn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

public class BayesianNetwork {

	public Set<NodeCategorical> nodes = new HashSet<>();
	
	private final String name;
	
	public BayesianNetwork(String name) {
		this.name = name;
	}

	public void add(NodeCategorical n) {
		nodes.add(n);
	}
	

	public void addAll(Collection<NodeCategorical> nns) {
		nodes.addAll(nns);
	}
	
	public boolean containsNode(NodeCategorical n) {
		return nodes.contains(n);
	}
	
	public NodeCategorical getVariable(String id) {
		for (NodeCategorical n: nodes) {
			if (n.getName().equals(id))
				return n;
		}
		return null;
	}
	
	public List<NodeCategorical> enumerateNodes() {
		
		List<NodeCategorical> res = new LinkedList<>();
		
		for (NodeCategorical n: nodes) {
			
			int idxMin = 0;
			for (NodeCategorical p: n.getParents()) {
				int idxParent = res.indexOf(p);
				if (idxParent+1 > idxMin) {
					// this node was already added to our network. 
					idxMin = idxParent+1;
				}
			}
			res.add(idxMin, n);
		
		}
	
		return res;
	}
	
	public Set<NodeCategorical> getNodes() {
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

		for (NodeCategorical n: enumerateNodes())
			n.toXMLBIF(sb);
			
		sb.append("</NETWORK>\n</BIF>\n");

		PrintStream ps = new PrintStream(f);
		ps.print(sb.toString());
		ps.close();
	}
	
	public void saveAsXMLBIF(String filename) throws FileNotFoundException {
		
		saveAsXMLBIF(new File(filename));
		
	}
	
	public boolean isValid() {
		
		for (NodeCategorical n: nodes) {
			if (!n.isValid())
				return false;
		}
		
		return true;
	}
	
	public static BayesianNetwork readFromXMLBIF(String xmlStr) {
		
        Document document;
		try {
			document = DocumentHelper.parseText(xmlStr);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalArgumentException("invalid XML BIF format", e);
		}


        // read the variables
		Map<String,NodeCategorical> id2node = new HashMap<>();
		{
	        List<?> variables = document.selectNodes("/BIF/NETWORK/VARIABLE");
	        for (Iterator<?> iterVars = variables.iterator(); iterVars.hasNext(); ) {
	            
	        	Node nodeVariable = (Node)iterVars.next();
	        	
	        	final String variableName = nodeVariable.selectSingleNode("./NAME").getText().trim();
	        	
	        	NodeCategorical n = new NodeCategorical(variableName);
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
		
    	final String networkName = document.selectSingleNode("/BIF/NETWORK/NAME").getText().trim();

		// add them all into a Bayesian net
		BayesianNetwork bn = new BayesianNetwork(networkName);
		bn.addAll(id2node.values());
		
		// check it is ok
		if (!bn.isValid()) {
			throw new IllegalArgumentException("the bn is not valid");
		}
		
		return bn;
	}
	
	public static BayesianNetwork loadFromXMLBIF(File f) {
		
		try {
			return readFromXMLBIF(FileUtils.readFileToString(f));
		} catch (IOException e) {
			throw new IllegalArgumentException("unable to read file "+f, e);
		}
	}
	
}
