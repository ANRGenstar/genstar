package spin.objects;

import java.util.Set;

import core.metamodel.IAttribute;

/** Les attributs d'un lien. a savoir les noeuds FROM et TO, LE POIDS, l'orientation
 * 
 * @author Felix
 *
 */
public class NetworkLinkAttribut implements IAttribute<NetworkLinkValue>{

	@Override
	public String getAttributeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NetworkLinkValue> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setValues(Set<NetworkLinkValue> values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NetworkLinkValue getEmptyValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEmptyValue(NetworkLinkValue emptyValue) {
		// TODO Auto-generated method stub
		
	}

}
