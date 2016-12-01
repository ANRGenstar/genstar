package spin.objects;

import java.util.HashSet;
import java.util.Set;

import core.io.survey.entity.AGenstarEntity;

/** Node of the SpinNetwork, linked to a GosplEntity (an individual from population generation module)
 * 
 * @author Felix, FredA722
 *
 * @param <E>
 */
public class NetworkNode {
	// Entity associated 
	private AGenstarEntity entity;
	
	// Connected node
	private Set<NetworkLink> links;
	
	/** Constructeur de networkNode prenant une entité
	 * 
	 * @param entite
	 */
	
	public NetworkNode(AGenstarEntity entite){
		entity = entite;
		links = new HashSet<NetworkLink>();
	}
	
	public void addLink(NetworkLink link){
		links.add(link);
	}
	
	public AGenstarEntity getEntity() {
		return entity;
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

