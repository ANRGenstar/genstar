package gospl.metamodel.attribut;

/**
 * An attribute with a discrete set of potential values (categorial)
 * 
 * @author gospl-team
 *
 */
public interface IDiscreteAttribute<ContentType extends Object> 
					extends IAttribute {

	/**
	 * The number of discrete value this attribute can take
	 * 
	 * @return
	 */
	public int getValueSize();
	
}
