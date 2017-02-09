package spin.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.pop.APopulationEntity;
import spin.tools.Tools;



/** Network composé de noeud et de lien
 * 
 */
public class SpinNetwork {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	// let set<networkLink> est commun a ceux donné aux noeuds
	private Map<NetworkNode, Set<NetworkLink>> network;
	private Map<NetworkNode, APopulationEntity> kvNodeEntityFastList;
	private Map<APopulationEntity, NetworkNode> kvEntityNodeFastList;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		network = new HashMap<NetworkNode, Set<NetworkLink>>();
		kvNodeEntityFastList = new HashMap<NetworkNode, APopulationEntity>();
		kvEntityNodeFastList = new HashMap<APopulationEntity, NetworkNode>();
	}
	
	/**
	 * Put a new NetworkNode in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param node the NetworkNode to add
	 */
	public void putNode(NetworkNode node) {
		HashSet<NetworkLink> links = new HashSet<NetworkLink>();
		network.put(node, links);
		node.defineLinkHash(links);
	}

	/** Ajout de link aux listes de link des noeuds
	 * 
	 * @param link
	 */
	public void putLink(NetworkLink link){
		Tools.addElementInHashArray(network, link.getFrom(), link);
		Tools.addElementInHashArray(network, link.getTo(), link);
	}
	
	/** Obtenir les noeuds du réseau
	 * 
	 * @return
	 */
	public Set<NetworkNode> getNodes() {
		return network.keySet();
	}
	
	/** Obtenir la liste de liens
	 * 
	 * @return
	 */
	public Set<NetworkLink> getLinks(){
		HashSet<NetworkNode> nodes = new HashSet<>(this.getNodes());
		Set<NetworkLink> links  = new HashSet<>();
		
//		links = 
//				network.values().stream()
//				.flatMap(f -> f.stream())
//				.distinct()
//				.sorted()
//				.collect(Collectors.toSet());
				
		for (NetworkNode n : nodes){
			for (NetworkLink l : n.getLinks()){
				if (!links.contains(l)){
					links.add(l);
				}
			}
		}
		return links;
	}
}
