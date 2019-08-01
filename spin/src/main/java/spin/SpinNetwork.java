package spin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.ClusteringCoefficient;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.traverse.RandomWalkIterator;

import core.metamodel.entity.ADemoEntity;
import core.util.random.GenstarRandom;
import spin.interfaces.INetProperties;



/** Network compose de noeud et de lien
 * 
 */
public class SpinNetwork implements INetProperties<ADemoEntity> {
	
	public Graph<ADemoEntity,DefaultEdge> network;
	boolean directed;
	
	// Map d'acces rapide;
	// FIXME : is it useless ? 
			// On peut récupérer les entite par un getAttribute sur les nodes
	// FIXME : set de 
	// public Map<DefaultVertex, ADemoEntity> kvNodeEntityFastList;
	// public Map<ADemoEntity, Node> kvEntityNodeFastList;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		this(true);
	}
	
	public SpinNetwork(boolean _directed){
		if(_directed)
			network = new DefaultDirectedGraph<>(DefaultEdge.class);
		else
			network = new DefaultUndirectedGraph<>(DefaultEdge.class);
		directed = _directed;
		
		//kvNodeEntityFastList = new HashMap<Node, ADemoEntity>();
		
		//kvEntityNodeFastList = new HashMap<ADemoEntity, Node>();
	}	
	
	/**
	 * Have the inner jgrapht network
	 * 
	 * @return
	 */
	public Graph<ADemoEntity,DefaultEdge> getNetwork() {
		return network;
	}
	
	/**
	 * Put a new Node in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param nodeId the id of the Node to add
	 * @param entite the population entity to which the Node is associated 
	 */
	public void putNode(String nodeId, ADemoEntity entite) {
	
		network.addVertex(entite);
		
		/*
		Node node = network.addNode(nodeId);
		
		node.addAttribute("entity", entite);
	
		kvNodeEntityFastList.put(node, entite);
		kvEntityNodeFastList.put(entite, node);
		*/
	}

	/** Ajout de link aux listes de link des noeuds
	 * 
	 * @param link
	 */
	public void putLink(String linkId, ADemoEntity e1, ADemoEntity e2){
		network.addEdge(e1, e2, new DefaultEdge());
		
		//network.addEdge(linkId, kvEntityNodeFastList.get(e1), kvEntityNodeFastList.get(e2), directed);
	}	
	
	/** Remove a node from a graph
	 * 
	 * @param node the node we want to remove
	 */
	public void removeNode(ADemoEntity entite) {
		network.removeVertex(entite);
	}
	
	/** Remove a link from the graph
	 * 
	 * @param link the ling we want to remove
	 */
	public void removeLink(DefaultEdge link) {
		network.removeEdge(link);
	}
	
	/** Obtenir les noeuds du reseau
	 * 
	 * @return
	 */
	public Set<ADemoEntity> getNodes() {
		return network.vertexSet();
	}
		
	/** Obtenir la liste de liens
	 * 
	 * @return
	 */
	public Set<DefaultEdge> getLinks(){
		return network.edgeSet();
	}
	
	/*
	public ADemoEntity getDemoEntityNode(Node n) {
		return kvNodeEntityFastList.get(n);
	}
	*/
	
	
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
		List<ADemoEntity> nodes = new ArrayList<>(getNodes());
		if(nodes.size() < sampleSize) {
			System.out.println("ERROR : sample size cannot be bigger than the size of the original graph");
			System.exit(0);
		}
		
		// Sample graph
		Graph sampleGraph = null;
		
		while(sampleGraph==null || sampleGraph.vertexSet().size() != sampleSize) {
			ADemoEntity randomStart = nodes.remove(GenstarRandom.getInstance().nextInt(nodes.size()));
			RandomWalkIterator<ADemoEntity, DefaultEdge> rwi = new RandomWalkIterator<>(
					network, randomStart, false, sampleSize, GenstarRandom.getInstance());
			sampleGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
			while(rwi.hasNext()) {
				sampleGraph.addVertex(rwi.next());
			}
		}
		
		for (DefaultEdge edge : network.edgeSet()) {
			ADemoEntity source = network.getEdgeSource(edge);
			ADemoEntity target = network.getEdgeTarget(edge);
			if(network.containsVertex(source) && network.containsVertex(target)) {
				sampleGraph.addEdge(source, target);
			}
		}
		
		return sampleGraph;
		
		/*
		 * TODO : do I made the whole algo in 10 lines ??????
		 * 
		// Map associating a weight to each node of the graph
		Map<ADemoEntity,Double> weights = getNodes().stream().collect(Collectors.toMap(Function.identity(), n -> 1.0));
		
		// List of nodes added to the sample graph
		List<ADemoEntity> sampleNodes = new ArrayList<>();
		
		// Map associating sample nodes to original nodes
		Map<String,String> sampleToOriginalMap = new HashMap<String, String>();
		Map<String,String> originalToSampleMap = new HashMap<String, String>();
		
		// Starting point of the random walk
		ADemoEntity start = nodes.get(GenstarRandom.getInstance().nextInt(nodes.size()));
		
		// Adding the starting point to the sample graph
		//int sampleNodeId = nodes.size();
		Object snid = sampleGraph.addVertex();
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
		*/
	}
	
	public int selectNextNode(List<ADemoEntity> nodes, Map<ADemoEntity,Double> weights) {
		int index = 0;
		Random rand = new Random();
		
		double[] cumulatedWeights = new double[nodes.size()];
		int i = 0;
		for(ADemoEntity n : nodes) {
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
		return getAPL(randomWalkSample(sampleSize));
	}

	@Override
	public double getAPL() {
		return getAPL(network);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private double getAPL(Graph graph) {
		double APL = 0;
		int nbPaths = 0;
		
		DijkstraShortestPath dijkstra = new DijkstraShortestPath(graph);
		for(Object n1 : graph.vertexSet()) {
			for(Object n2 : graph.vertexSet()) {
				if(!n2.equals(n1)) {
					APL += dijkstra.getPath(n1, n2).getLength();
					nbPaths ++;
				}
			}
		}
		
		APL /= nbPaths;
		return APL;
	}

	@Override
	public double getClustering(ADemoEntity entity) {
		return new ClusteringCoefficient<>(network).getGlobalClusteringCoefficient();
	}

	@Override
	public Set<ADemoEntity> getNeighboor(ADemoEntity entity) {
		return new NeighborCache<>(network).neighborsOf(entity); 
	}

	@Override
	public double getDensity() {
		if(directed) 
			return network.vertexSet().size()*2/(network.edgeSet().size()*(network.edgeSet().size()-1));
		return network.vertexSet().size()/(network.edgeSet().size()*(network.edgeSet().size()-1));
	}
	
	@Override
	public String toString(){
		String res = "Nodes: "+network.vertexSet().size()+"\n" ;//+ network.getNodeSet() ;
		res = res + "\nEdges: "+network.edgeSet().size()+"\n" ;//+ network.getEdgeSet();
		return res;
	}
	
	public boolean isDirected() {
		return directed;
	}
	
	public void setDirected (boolean direct) {
		directed = direct;
	}
}
