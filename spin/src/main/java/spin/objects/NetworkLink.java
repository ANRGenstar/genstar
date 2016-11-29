package spin.objects;

import java.util.Collection;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.IEntity;

/** Objet pour un network
 * 
 *
 */
public class NetworkLink implements IEntity<NetworkLinkAttribut, NetworkLinkValue> {

//	NetworkNode<>
	
	@Override
	public Collection<NetworkLinkAttribut> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkLinkValue getValueForAttribute(NetworkLinkAttribut attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<NetworkLinkValue> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public IEntity<?, ?> getNest() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setLocation(Point location) {
		// TODO Auto-generated method stub
		
	}

	
	public void setNest(IEntity<?, ?> entity) {
		// TODO Auto-generated method stub
		
	}

	
	public NetworkLinkValue getValueForAttribute(String property) {
		// TODO Auto-generated method stub
		return null;
	}


}
