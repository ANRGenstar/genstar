package spin.objects;

import core.metamodel.IAttribute;
import core.metamodel.IValue;

/** La valeur d'attribut pour les attributs de la classe networkLien
 * par exemple, la valeur de l'attribut FROM sera noeud 1
 * ou encore attrivut POIDS aura une valeur 10
 */
public class NetworkLinkValue implements IValue {

//	public int weight;
	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + weight;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		NetworkLinkValue other = (NetworkLinkValue) obj;
//		if (weight != other.weight)
//			return false;
//		return true;
//	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInputStringValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttribute<? extends IValue> getAttribute() {
		// TODO Auto-generated method stub
		return null;
	}
	
	



}
