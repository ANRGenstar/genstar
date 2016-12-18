package gospl.algo;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import core.io.survey.entity.attribut.AGenstarAttribute;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;

/**
 * Encodes the links between attributes.
 * 
 * @author Samuel Thiriot 
 */
public class AttributesDependanciesGraph {

	private Logger logger = LogManager.getLogger();

	private Graph graph = new SingleGraph("dependancies",true,false);
	
	private final static String NODE_ATTRIBUTE_ATTRIBUTE = "surveyattribute";
	private final static String EDGE_ATTRIBUTE_MATRIX = "matrix";
	private final static String NODE_ATTRIBUTE_SUBGRAPH_ID = "componentid";

	private final static String EDGE_ATTRIBUTE_TYPE = "type";

	/**
	 * Tags the type of edge inside the dependancy graph.
	 */
	public enum EdgeDependancyType {
		/**
		 * The link between two attributes is known thanks to a frequency dependancy
		 */
		GLOBAL_FREQUENCY,
		/**
		 * Attributes are linked as a "refered" node.
		 */
		REFERENT
	}
	
	public AttributesDependanciesGraph() {
		

	}

	public static AttributesDependanciesGraph constructDependancies(ASegmentedNDimensionalMatrix<?> segmentedMatrix) {
		
		AttributesDependanciesGraph dependancies = new AttributesDependanciesGraph();
		
		for (AFullNDimensionalMatrix<?> currentMatrix: segmentedMatrix.getMatrices()) {

			// add all the nodes 
			for (AGenstarAttribute attribute: currentMatrix.getDimensions()) {
				dependancies.addKnownAttribute(attribute);				
			}
			
			// TODO check that currentMatrix.getMetaDataType();
			
			// add the dependancies
			dependancies.addKnownGlobalFrequency(currentMatrix);
			
		}
		
		return dependancies;
	}
	
	/**
	 * Adds this attribute as a known attribute in the graph
	 * @param attribute
	 */
	public void addKnownAttribute(AGenstarAttribute attribute) {
		
		logger.debug("should add node {}", attribute.getAttributeName());
		
		Node n = graph.getNode(attribute.getAttributeName());
		if (n == null) {
			logger.debug("creating node {}", attribute.getAttributeName());
			// create the node 
			n = graph.addNode(attribute.getAttributeName());
			n.setAttribute(NODE_ATTRIBUTE_ATTRIBUTE, attribute);
		} else {
			// ensure the node attribute data is ok
			if (n.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE) != attribute) {
				throw new IllegalArgumentException("The node "+attribute.getAttributeName()+" was already declared with a different attribute content.");
			}
		}
		
		if (attribute.getReferentAttribute() != attribute) {
			addKnownAttribute(attribute.getReferentAttribute());
			// add edge TODO
			Edge e = graph.addEdge(
					"edge"+graph.getEdgeCount(), 
					attribute.getAttributeName(), 
					attribute.getReferentAttribute().getAttributeName(),
					true	// not directed for a global frequency
					);
			e.setAttribute(EDGE_ATTRIBUTE_TYPE, EdgeDependancyType.REFERENT);
		}
	}
	
	
	public void addKnownGlobalFrequency(AFullNDimensionalMatrix<?> currentMatrix) {
		List<AGenstarAttribute> attributes = new ArrayList<>();
		attributes.addAll(currentMatrix.getDimensions());
		for (int i=0; i<attributes.size();i++) {
			for (int j=i+1; j<attributes.size();j++) {
				Edge e = graph.addEdge(
						"edge"+graph.getEdgeCount(), 
						attributes.get(i).getAttributeName(), 
						attributes.get(j).getAttributeName(),
						false	// not directed for a global frequency
						);
				e.setAttribute(EDGE_ATTRIBUTE_MATRIX, currentMatrix);
				e.setAttribute(EDGE_ATTRIBUTE_TYPE, EdgeDependancyType.GLOBAL_FREQUENCY);
			}
		}
	}
	

	/**
	 * Returns a graphstream algorithm for connected components detection 
	 * initialized the right way. 
	 */
	@SuppressWarnings("unused")
	private ConnectedComponents getAlgoConnectedComponents() {
		ConnectedComponents cc = new ConnectedComponents();
		cc.init(graph);
		cc.setCountAttribute(NODE_ATTRIBUTE_SUBGRAPH_ID);
		return cc;
	}
	/**
	 * Returns true if the graph is connected, that is made of one unique graph.
	 * @return
	 */
	public boolean isConnected() {
		ConnectedComponents cc = new ConnectedComponents(graph);
		return cc.getConnectedComponentsCount(2)>1;
	}
	
	/**
	 *	returns the number of subgraphs.
	 * @return
	 */
	public int getCountOfConnectedComponents() {
		ConnectedComponents cc = new ConnectedComponents(graph);
		return cc.getConnectedComponentsCount();
	}
	
	/**
	 * returns the sets of the survey attributes which belong to independnat components.
	 * @return
	 */
	public Collection<Set<AGenstarAttribute>> getConnectedComponents() {
		
		ConnectedComponents cc = new ConnectedComponents(graph);
		cc.setCountAttribute(NODE_ATTRIBUTE_SUBGRAPH_ID);
		cc.compute();
		
		int count = cc.getConnectedComponentsCount();
		
		if (count == 1) {
			// quick exit
			LinkedList<Set<AGenstarAttribute>> res = new LinkedList<>();
			res.add(
					graph.getNodeSet()
						.stream()
						.map(node -> (AGenstarAttribute)node.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE))
						.collect(Collectors.toSet()));
			
			return res;
		}
		
		// the algo puts an id of component into every node. Just collect the nodes based on that. 
		
		Map<Integer, Set<AGenstarAttribute>> componentId2attributes = new HashMap<>(count);
		for (Node n: graph.getNodeSet()) {
			Integer componentId = n.getAttribute(NODE_ATTRIBUTE_SUBGRAPH_ID);
			if (!componentId2attributes.containsKey(componentId)) {
				componentId2attributes.put(componentId, new HashSet<>());
			}
			componentId2attributes.get(componentId).add(n.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE));
		}
		
		return componentId2attributes.values();
		
	}
	
	/**
	 * returns the potential roots in the subcomponent  that can be used as 
	 * a starting point for exploration. Will be one of the nodes with the lowest connectiviy 
	 * (if the component has only one element, it will be one node with 0 links; if 2 elements,
	 * will be degree 1; else, it will be the lowest possible). 
	 * @return
	 */
	public Set<AGenstarAttribute> getRoots(Collection<AGenstarAttribute> component) {
		
		// the max connectivity found so far
		Integer minDegree = Integer.MAX_VALUE;
		// the nodes found so far for the degrees
		Map<Integer,Set<AGenstarAttribute>> degree2nodes = new TreeMap<>();
		
		for (Node n: graph.getNodeSet()) {
			Integer degree = n.getDegree();
			
			// quick exit
			if (degree==0) 
				return n.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE);
			
			// quick skip
			if (degree > minDegree)
				continue;
			
			// Nota bene: if any performance was arising (unlikely on this type of grpah), we might forget the highest nodes that are useless anyway. Would save a bit of memory. 
			
			if (!degree2nodes.containsKey(degree)) {
				degree2nodes.put(degree, new HashSet<>());
			}
			degree2nodes.get(degree).add(n.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE));
		}
		
		return degree2nodes.entrySet().iterator().next().getValue();
	}
	
	/**
	 * For a given root and a given subcomponent, returns a way to iterate it. 
	 * 
	 * TODO this is not super usefull yet. Should evolve to something like prefering which one to go with.
	 * 
	 * @param component
	 * @param root
	 * @return
	 */
	public List<AGenstarAttribute> getOrderOfExploration(Collection<AGenstarAttribute> component, AGenstarAttribute root) {
		
		Map<AGenstarAttribute,Integer> attribute2rank = new HashMap<>(component.size());
		
		// these nodes were seen already !
		Set<AGenstarAttribute> taboo = new HashSet<>();
		List<AGenstarAttribute> list = new ArrayList<>(component.size());
		
		Set<AGenstarAttribute> toExplore = new HashSet<>();
		
		// bootstrap
		toExplore.add(root);
		attribute2rank.put(root, 0);
		
		while (!toExplore.isEmpty()) {
			
			AGenstarAttribute current = toExplore.iterator().next();
			toExplore.remove(current);
			Integer currentRank = attribute2rank.get(current);
			taboo.add(current);
			list.add(current);
			
			logger.debug("studying node {} {}",currentRank,current);
			
			Iterator<Node> itNeighboors = graph.getNode(current.getAttributeName()).getNeighborNodeIterator();
			while (itNeighboors.hasNext()) {
				Node neighboor = itNeighboors.next();
				AGenstarAttribute nei  = neighboor.getAttribute(NODE_ATTRIBUTE_ATTRIBUTE);
				
				if (taboo.contains(nei)) 
					continue;

				if (attribute2rank.containsKey(nei)) {
					attribute2rank.put(
							nei, 
							Math.max(currentRank+1,  attribute2rank.get(nei))
							);
				} else {
					attribute2rank.put(nei, currentRank+1);
				}
				toExplore.add(nei);
			}
			
		}
		
		logger.debug(attribute2rank);
		
		return list;
		
	}
	
	
	/**
	 * Represents the network as a graphviz / dot representation
	 * @return
	 */
	public String toDotRepresentation() {
		
		// maps the attribute names to dot names
		Map<String,String> attName2dotName = new HashMap<>(graph.getNodeCount());
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("digraph dependancies {\n");

		// graph properties
		sb.append("\trankdir=LR;\n");
		sb.append("\n");
		
		// add nodes 
		for (Node n: graph.getNodeSet()) {
			final String graphvizNodeName = n.getId().replaceAll("[^a-zA-Z0-9]", "_"); 
			attName2dotName.put(n.getId(), graphvizNodeName);
			sb.append("\t").append(graphvizNodeName).append(" [").append("label=\"").append(n.getId()).append("\"];\n");
		}
		sb.append("\n");
		
		sb.append("\tedge [len=2];\n");
		// add edges
		for (Edge e: graph.getEdgeSet()) {
			
			EdgeDependancyType type = (EdgeDependancyType)e.getAttribute(EDGE_ATTRIBUTE_TYPE);
			
			sb.append("\t");
			
			sb.append(attName2dotName.get(e.getNode0().getId()));
			sb.append("->");
			sb.append(attName2dotName.get(e.getNode1().getId()));
			sb.append(" [");
			//sb.append(e.getId());

			switch (type) {
			case GLOBAL_FREQUENCY:
				sb.append("label=\"");
				sb.append(((AFullNDimensionalMatrix<?>)e.getAttribute(EDGE_ATTRIBUTE_MATRIX)).getLabel());
				sb.append("\"");
				sb.append(", style=bold");
				break;
			case REFERENT:
				sb.append("style=dashed");
				break;
			}
			
			if (!e.isDirected()) {
				sb.append(", arrowhead=none");
			} 
			
			sb.append("]");
			sb.append(";\n");
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}
	
	/**
	 * Prints a dot representation of the graph to a temporary file.
	 * @return
	 */
	public File printDotRepresentationToFile() {
		
 	   	try {
 	   		File temp = File.createTempFile("gospl-dependancy-graph-", ".dot");
	 	   	@SuppressWarnings("resource")
			PrintStream ps = new PrintStream(temp);

	 	   	ps.print(this.toDotRepresentation());
	 	   	
	 	   	return temp;
	 	   	
		} catch (IOException e) {
			throw new RuntimeException("error while creating a dependancy file", e);
		}
 	   

	}
	
	/**
	 * attempts to generate a dot representation into temporary file in png
	 * @return
	 */
	public File generateDotRepresentationInPNG(boolean open) {
		File dotFile = this.printDotRepresentationToFile();
		logger.debug("dot file written in {}", dotFile);
		File pngFile = new File(dotFile.getAbsolutePath()+".png");
		try {
			logger.debug("trying to generate PNG into {}", pngFile);
			Process p = Runtime.getRuntime().exec("neato -Tpng "+dotFile.getAbsolutePath()+" -o "+pngFile.getAbsolutePath());
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			throw new RuntimeException("error while attempting to run dot to generate the png representation", e);
		}
		if (!pngFile.exists()) {
			throw new RuntimeException("The png file was not generated as expected. Sorry.");
		}
		if (open) {
			try {
				Desktop.getDesktop().open(pngFile);
			} catch (IOException e) {
				throw new RuntimeException("Error while trying to open the png in an editor");
			}
		}
		return pngFile;
	}
	
}
