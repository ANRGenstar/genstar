package spin.objects;

import java.util.HashSet;
import java.util.Set;

import core.metamodel.pop.APopulationEntity;

/** Node of the SpinNetwork, linked to a GosplEntity (an individual from population generation module)
 * TODO Doublon d'informations sur les lienconnectés avec la liste ici et l'objet spinNetwork.
 * @author Felix, FredA722
 */
public class NetworkNode {
	
	// Entity associated 
	private APopulationEntity entity;
	
	// Non initalisé volontairement. Liste lié a celle de SpinNetwork
	private Set<NetworkLink> links;
	
	// Id du node. 
	private String id;
	
	
	/** Constructeur sans paramètre.
	 * 
	 */
	private NetworkNode(){
	}
	
	/** Constructeur de networkNode prenant une entité
	 * 
	 * @param entite
	 */
	public NetworkNode(APopulationEntity entite, String id){
		this();
		entity = entite;
		this.id = id;
	}
	
	/** Fait un lien entre la hash du spinNetwork et celle la.
	 * 
	 * @param links
	 */
	public void defineLinkHash(HashSet<NetworkLink> links){
		this.links = links;
	}
	
	/** Ajout d'un link dans la list linké avec celle du spinG.
	 * 
	 * @param link
	 */
	public void addLink(NetworkLink link){
		links.add(link);
	}
	
	/** Obtenir l'entité associée
	 * 
	 * @return
	 */
	public APopulationEntity getEntity() {
		return entity;
	}
	
	public String getId(){
		return id;
	}
	
	public Set<NetworkLink> getLinks(){
		return links;
	}
	
	public void removeLink(NetworkLink l){
		links.remove(l);
	}
	
	public boolean hasLink(NetworkLink l){
		return links.contains(l);
	}
	
	public Set<NetworkNode> getNeighbours(){
		Set<NetworkNode> neighb = new HashSet<NetworkNode>();
		for(NetworkLink l : links){
			if(this.equals(l.getFrom()))
				neighb.add(l.getTo());
			else
				neighb.add(l.getFrom());
		}
		return neighb;
	}
	
}

