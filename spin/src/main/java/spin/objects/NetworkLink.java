package spin.objects;

/** Objet pour un network
 * Combien des informations sur le type de lien entre des nodes
 *
 */
public class NetworkLink{
	private NetworkNode from;
	private NetworkNode to;
	private boolean oriented=false;
	
	public NetworkLink(NetworkNode f, NetworkNode t){
		this.from=f;
		this.to=t;
	}
	
	public NetworkLink(NetworkNode f, NetworkNode t, boolean oriented){
		this(f,t);
		this.oriented=oriented;
	}
	
	public NetworkNode getFrom(){
		return from;
	}
	
	public NetworkNode getTo(){
		return to;
	}
	
	public boolean equals(Object o){
		if(!o.getClass().equals(this.getClass()))
			return false;
		if(oriented){
			return(this.from.equals(((NetworkLink)o).getFrom())&&this.to.equals(((NetworkLink)o).getTo()));
		}
		else{
			return((this.from.equals(((NetworkLink)o).getFrom())&&this.to.equals(((NetworkLink)o).getTo()))
					||(this.from.equals(((NetworkLink)o).getTo())&&this.to.equals(((NetworkLink)o).getFrom())));
		}
	}

}
