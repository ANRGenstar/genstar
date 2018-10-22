package core.metamodel.entity.comparator;

import java.util.Map;
import java.util.Set;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.util.AttributeVectorMatcher;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Compare agent using a third referent: the closest agent to referent will be sorted first !
 * <p>
 * WARNING: not consistant with equality
 * 
 * @author kevinchapuis
 *
 */
public class HammingEntityComparator extends ImplicitEntityComparator {

	private AttributeVectorMatcher vector;

	public HammingEntityComparator() { }

	/**
	 * Hamming distance will be computed using a referent entity (one value for each attribute) 
	 * 
	 * @param entity
	 */
	public HammingEntityComparator(IEntity<Attribute<? extends IValue>> entity) {
		this.vector = new AttributeVectorMatcher(entity);
	}
	
	/**
	 * Hamming distance will be computed with several value being possible for each attribute
	 * 
	 * @param vector
	 */
	public HammingEntityComparator(Map<IAttribute<? extends IValue>, Set<IValue>> vector) {
		this.vector = new AttributeVectorMatcher(vector);
	}
	
	/**
	 * Equivalent to {link @HammingEntityComparator(Map<IAttribute<? extends IValue>, Set<IValue>> vector)} but not
	 * without consistency
	 * 
	 * @param vector
	 */
	public HammingEntityComparator(IValue... vector) {
		this.vector = new AttributeVectorMatcher();
		this.vector.addMatchToVector(vector);
	}
	
	// ------------------------------------------------------------ //

	@Override
	public int compare(IEntity<? extends IAttribute<? extends IValue>> e1, IEntity<? extends IAttribute<? extends IValue>> e2) {
		int scoreOne = 0;
		int scoreTwo = 0;
		if(vector != null) {
			scoreOne = vector.getHammingDistance(e1);
			scoreTwo = vector.getHammingDistance(e2);
		}
		return scoreOne > scoreTwo ? -1 : scoreOne < scoreTwo ? 1 : super.compare(e1, e2);
	}

}
