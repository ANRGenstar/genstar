package spin;

import static org.graphstream.algorithm.Toolkit.clusteringCoefficient;
import static org.graphstream.algorithm.Toolkit.density;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

import core.metamodel.entity.ADemoEntity;
import spin.interfaces.INetProperties;



/** Network compose de noeud et de lien
 * 
 */
public class SpinNetwork implements INetProperties{
	
	public Graph network;
	boolean directed;
	
	// Map d'acces rapide;
	// FIXME : is it useless ? 
			// On peut récupérer les entite par un getAttribute sur les nodes
	// FIXME : set de 
	public Map<Node, ADemoEntity> kvNodeEntityFastList;
	public Map<ADemoEntity, Node> kvEntityNodeFastList;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		this(true);
	}
	
	public SpinNetwork(boolean _directed){
		network = new DefaultGraph("network");
		directed = _directed;
		
		kvNodeEntityFastList = new HashMap<Node, ADemoEntity>();
		
		kvEntityNodeFastList = new HashMap<ADemoEntity, Node>();
	}	
	
	public Graph getNetwork() {
		return network;
	}
	
	/**
	 * Put a new Node in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param nodeId the id of the Node to add
	 * @param entite the population entity to which the Node is associated 
	 */
	public void putNode(String nodeId, ADemoEntity entite) {
	
		Node node = network.addNode(nodeId);
		
		node.addAttribute("entity", entite);
	
		kvNodeEntityFastList.put(node, entite);
		kvEntityNodeFastList.put(entite, node);
	}

	/** Ajout de link aux listes de link des noeuds
	 * 
	 * @param link
	 */
	public void putLink(String linkId, ADemoEntity e1, ADemoEntity e2){
		network.addEdge(linkId, kvEntityNodeFastList.get(e1), kvEntityNodeFastList.get(e2),directed);
	}

	/** Ajout de link aux listes de link des noeuds
	 * 
	 * @param link
	 */
	public void putLink(String linkId, Node n1, Node n2){
		network.addEdge(linkId, n1, n2, directed);
	}	
	
	/** Remove a node from a graph
	 * 
	 * @param node the node we want to remove
	 */
	public void removeNode(ADemoEntity entite) {
		network.removeNode(kvEntityNodeFastList.get(entite));
	}
	
	/** Remove a link from the graph
	 * 
	 * @param link the ling we want to remove
	 */
	public void removeLink(Edge link) {
		network.removeEdge(link);
	}
	
	/** Obtenir les noeuds du reseau
	 * 
	 * @return
	 */
	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<Node>();
		for(Node n : network.getEachNode()) {
			nodes.add(n);
		}
		return nodes;
	}
	
	/** Obtenir la liste de liens
	 * 
	 * @return
	 */
	public Set<Edge> getLinks(){
		Set<Edge> links = new HashSet<Edge>();
		for(Edge l : network.getEachEdge()) {
			links.add(l);
		}
		return links;
	}
	
	// TODO [stage] Utiliser des méthodes de sampling pour alléger le calcul de l'APL
	
	// TODO [stage] Random Walk ne fonctionne pas sur les graphes SF. Chercher une solution au probl�me
	/** Creates a sample graph from an existing graph using the Random Walk Sampling Method
	 * 
	 * @param sampleSize the number of nodes we want in our sample graph
	 * @return sampleGraph the sample Graph
	 */
	public Graph randomWalkSample(int sampleSize) {
		System.out.println("Debut de la generation du sample graph");
		
		// List of nodes from the original graph
		List<Node> nodes = new ArrayList<>(getNodes());
		if(nodes.size() < sampleSize) {
			System.out.println("ERROR : sample size cannot be bigger than the size of the original graph");
			System.exit(0);
		}
		
		// Map associating a weight to each node of the graph
		Map<Node,Double> weights = new HashMap<Node,Double>();
		for(Node n : getNodes()) {
			weights.put(n, 1.0);
		}
		
		// Random generator
		Random rand = new Random();
		
		// Sample graph
		Graph sampleGraph = new DefaultGraph("sample");
		
		// List of nodes added to the sample graph
		List<Node> sampleNodes = new ArrayList<>();
		
		// Map associating sample nodes to original nodes
		Map<String,String> sampleToOriginalMap = new HashMap<String, String>();
		Map<String,String> originalToSampleMap = new HashMap<String, String>();
		
		// Starting point of the random walk
		Node start = nodes.get(rand.nextInt(nodes.size()));
		
		// Adding the starting point to the sample graph
		int sampleNodeId = nodes.size();
		sampleGraph.addNode(String.valueOf(sampleNodeId));
		sampleNodes.add(start);
		sampleToOriginalMap.put(String.valueOf(sampleNodeId), start.getId());
		originalToSampleMap.put(start.getId(), String.valueOf(sampleNodeId));
		sampleNodeId++;
		int nbNodes = sampleSize-1;
		
		// Set the current step in the walk and update its weight
		Node currentNode = start;
		weights.replace(currentNode, weights.get(currentNode)/2);
		
		// Next step in the walk
		Node nextNode;
		
		// Counter used to measure the number of steps the program takes to complete. If the
		// program takes too many steps without completing, it must be stuck and needs to be reset
		// with a different starting point
		int nbSteps = 0;
		
		// Adding the right number of nodes to the sample graph
		while(nbNodes>0) {
			// Check if the program is stuck and change the starting point if necessary
			if(nbSteps>5*nodes.size()) {
				System.out.println("Reset");
				// Clear sampleGraph, sampleNodes and the maps
				sampleGraph.clear();
				sampleNodes.clear();
				sampleToOriginalMap.clear();
				originalToSampleMap.clear();
				
				// Reset the weight map
				for(Node n : getNodes()) {
					if(weights.get(n)!=1.0) {
						weights.replace(n, 1.0);
					}
				}
				
				// Select new starting point
				start = nodes.get(rand.nextInt(nodes.size()));
				
				// Reset program : add new starting point to the sample graph and reset
				// sampleNodeId, nbNodes and nbSteps
				sampleNodeId = nodes.size();
				sampleGraph.addNode(String.valueOf(sampleNodeId));
				sampleNodes.add(start);
				sampleToOriginalMap.put(String.valueOf(sampleNodeId), start.getId());
				originalToSampleMap.put(start.getId(), String.valueOf(sampleNodeId));
				sampleNodeId++;
				nbNodes = sampleSize-1;
				currentNode = start;
				weights.replace(currentNode, weights.get(currentNode)/2);
				nbSteps = 0;
			}
			
			// List the neighbors of the current node
			List<ADemoEntity> neighborsEntity = new ArrayList<>(getNeighboor(currentNode.getAttribute("entity")));
			List<Node> neighborsNode = new ArrayList<Node>();
			for(ADemoEntity e : neighborsEntity) {
				neighborsNode.add(kvEntityNodeFastList.get(e));
			}
			
			// Choose one of those neighbors as the next step in the walk
			int index = selectNextNode(neighborsNode, weights);
			nextNode = neighborsNode.get(index);
			
			// If nextNode doesn't already belong to the sample graph, we add it and continue the walk
			if(!sampleNodes.contains(nextNode)) {
				sampleGraph.addNode(String.valueOf(sampleNodeId));
				sampleNodes.add(nextNode);
				sampleToOriginalMap.put(String.valueOf(sampleNodeId), nextNode.getId());
				originalToSampleMap.put(nextNode.getId(), String.valueOf(sampleNodeId));
				sampleNodeId++;
				
				currentNode = nextNode;
				weights.replace(currentNode, weights.get(currentNode)/2);
				
				nbNodes--;
			}
			nbSteps++;
		}
		
		// Linking the nodes in the sample graph
		int sampleLinkId = network.getEdgeCount();
		for(Node n1 : sampleGraph.getEachNode()) {
			Node N1 = network.getNode(sampleToOriginalMap.get(n1.getId()));
			for(Node n2 : sampleGraph.getEachNode()) {
				if(!n2.equals(n1)) {
					Node N2 = network.getNode(sampleToOriginalMap.get(n2.getId()));
					if(N1.hasEdgeBetween(N2) && !n1.hasEdgeBetween(n2)) {
						sampleGraph.addEdge(String.valueOf(sampleLinkId), n1, n2);
						sampleLinkId++;
					}
				}
			}
		}
		
		System.out.println("Fin de generation du sample graph");
		return sampleGraph;
	}
	
	public int selectNextNode(List<Node> nodes, Map<Node,Double> weights) {
		int index = 0;
		Random rand = new Random();
		
		double[] cumulatedWeights = new double[nodes.size()];
		int i = 0;
		for(Node n : nodes) {
			if(i == 0) {
				cumulatedWeights[i] = weights.get(n);
				i++;
			} else {
				cumulatedWeights[i] = weights.get(n) + cumulatedWeights[i-1];
				i++;
			}
		}
		
		double p = rand.nextDouble()*cumulatedWeights[nodes.size()-1];
		for(int j=0 ; j<nodes.size()-1 ; j++) {
			if(p>=cumulatedWeights[j] && p<cumulatedWeights[j+1]) {
				index = j+1;
			}
		}
		
		return index;
	}
	
	public double getSampleAPL(int sampleSize) {
		Graph sampleGraph = randomWalkSample(sampleSize);
		
		double APL = 0;
		int nbPaths = 0;
		
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", null);
		dijkstra.init(sampleGraph);
		for(Node n1 : sampleGraph.getEachNode()) {
			dijkstra.setSource(n1);
			dijkstra.compute();
			for(Node n2 : sampleGraph.getEachNode()) {
				if(!n2.equals(n1)) {
					APL += dijkstra.getPathLength(n2);
					nbPaths ++;
				}
			}
		}
		dijkstra.clear();
		
		APL /= nbPaths;
		return APL;
	}

	@Override
	public double getAPL() {
		double APL = 0;
		int nbPaths = 0;
		
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", null);
		dijkstra.init(network);
		for(Node n1 : network.getEachNode()) {
			dijkstra.setSource(n1);
			dijkstra.compute();
			for(Node n2 : network.getEachNode()) {
				if(!n2.equals(n1)) {
					APL += dijkstra.getPathLength(n2);
					nbPaths ++;
				}
			}
		}
		dijkstra.clear();
		
		APL /= nbPaths;
		return APL;
	}

	@Override
	public double getClustering(ADemoEntity entite) {
		Node node = kvEntityNodeFastList.get(entite);
		return clusteringCoefficient(node);
	}

	@Override
	public Set<ADemoEntity> getNeighboor(ADemoEntity entite) {
		Node node = kvEntityNodeFastList.get(entite);
		Set<ADemoEntity> neighbors = new HashSet<ADemoEntity>();
		for(Node n : network.getEachNode()) {
			if(!n.equals(node) && n.hasEdgeBetween(node)) {
				ADemoEntity e = n.getAttribute("entity");
				neighbors.add(e);
			}
		}
		return neighbors;
	}

	@Override
	public double getDensity() {
		return density(network);
	}
	
	@Override
	public String toString(){
		String res = "Nodes: "+network.getNodeCount()+"\n" ;//+ network.getNodeSet() ;
		res = res + "\nEdges: "+network.getEdgeCount()+"\n" ;//+ network.getEdgeSet();
		return res;
	}
	
	public boolean isDirected() {
		return directed;
	}
	
	public void setDirected (boolean direct) {
		directed = direct;
	}
}
