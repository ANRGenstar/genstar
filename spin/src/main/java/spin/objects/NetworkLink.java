package spin.objects;

/** Objet pour un network
 * Contient des informations sur le type de lien entre des nodes
 *
 */
public class NetworkLink{
	private NetworkNode from;
	private NetworkNode to;
	private boolean oriented = false;
	private String id;
	
	public NetworkLink(NetworkNode f, NetworkNode t, String id){
		this.from=f;
		this.to= t;
		this.id = id;
	}
	
	public NetworkLink(NetworkNode f, NetworkNode t, boolean oriented, String id){
		this(f,t,id);
		this.oriented = oriented;
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
