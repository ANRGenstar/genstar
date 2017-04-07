package spin.objects;

import static org.graphstream.algorithm.Toolkit.clusteringCoefficient;
import static org.graphstream.algorithm.Toolkit.density;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

import core.metamodel.pop.APopulationEntity;
import spin.interfaces.INetProperties;



/** Network compose de noeud et de lien
 * 
 */
public class SpinNetwork implements INetProperties{
	
	public Graph network;
	
	// Map d'acces rapide;
	public Map<Node, APopulationEntity> kvNodeEntityFastList;
	public Map<APopulationEntity, Node> kvEntityNodeFastList;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		network = new DefaultGraph("network");
		kvNodeEntityFastList = new HashMap<Node, APopulationEntity>();
		kvEntityNodeFastList = new HashMap<APopulationEntity, Node>();
	}
	
	/**
	 * Put a new Node in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param nodeId the id of the Node to add
	 * @param entite the population entity to which the Node is associated 
	 */
	public void putNode(String nodeId, APopulationEntity entite) {
		network.addNode(nodeId);
		
		Node node = network.getNode(nodeId);
		
		node.addAttribute("entity", entite);
	
		kvNodeEntityFastList.put(node, node.getAttribute("entity"));
		kvEntityNodeFastList.put(node.getAttribute("entity"), node);
	}

	/** Ajout de link aux listes de link des noeuds
	 * 
	 * @param link
	 */
	public void putLink(String linkId, Node n1, Node n2){
		network.addEdge(linkId, n1, n2);
		// TODO [stage (?)] utiliser String plutot que Node pour identifier n1 et n2
	}
	
	/** Remove a node from a graph
	 * 
	 * @param node the node we want to remove
	 */
	public void removeNode(Node node) {
		network.removeNode(node);
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

	@Override
	public double getAPL() {
		double APL = 0;
		int nbPaths = 0;
		
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
		dijkstra.init(network);
		for(Node n1 : network.getEachNode()) {
			dijkstra.setSource(n1);
			dijkstra.compute();
			for(Node n2 : network.getEachNode()) {
				APL += dijkstra.getPathLength(n2);
				nbPaths ++;
			}
		}
		dijkstra.clear();
		
		APL /= nbPaths;
		return APL;
	}

	@Override
	public double getClustering(APopulationEntity entite) {
		Node node = kvEntityNodeFastList.get(entite);
		return clusteringCoefficient(node);
	}

	@Override
	public Set<APopulationEntity> getNeighboor(APopulationEntity entite) {
		Node node = kvEntityNodeFastList.get(entite);
		Set<APopulationEntity> neighbors = new HashSet<APopulationEntity>();
		for(Node n : network.getEachNode()) {
			if(!n.equals(node) && n.hasEdgeBetween(node)) {
				APopulationEntity e = n.getAttribute("entity");
				neighbors.add(e);
			}
		}
		return neighbors;
	}

	@Override
	public double getDensity() {
		return density(network);
	}
}
