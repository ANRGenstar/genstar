package core.metamodel.attribute.emergent.filter;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.value.IValue;

/**
 * Factory of basic filter: namely {@link EChildFilter#OneOf}, {@link EChildFilter#OneOfEach}
 * and {@link EChildFilter#All}
 * 
 * @author kevinchapuis
 *
 */
public class EntityChildFilterFactory {

	public enum EChildFilter {
		TheOne(new EntityMatchFilter()),
		OneOf(new EntityOneOfMatchFilter()), 
		OneOfEach(new EntityOneOfEachMatchFilter()),
		All(new EntityAllMatchFilter());
		
		private final IEntityChildFilter filter;
		private EChildFilter(IEntityChildFilter filter) {this.filter=filter;}
		public IEntityChildFilter getFilter() {return filter;}
	}
	
	private static final EntityChildFilterFactory FACTORY = new EntityChildFilterFactory();
	
	private EntityChildFilterFactory() {}
	
	public static EntityChildFilterFactory getFactory() {
		return FACTORY;
	}
	
	/**
	 * Get the filter passed in argument with default entity comparator
	 * 
	 * @param filter
	 * @return
	 */
	public IEntityChildFilter getFilter(EChildFilter filter) {
		return filter.getFilter();
	}
	
	/**
	 * Get the filter passed in argument with a particular entity comparator
	 * 
	 * @param filter
	 * @param comparator
	 * @return
	 */
	public IEntityChildFilter getFilter(EChildFilter filter, ImplicitEntityComparator comparator) {
		switch (filter) {
		case TheOne:
			IEntityChildFilter output = new EntityMatchFilter();
			output.setComparator(comparator);
			return output;
		case OneOf:
			return new EntityOneOfMatchFilter(comparator);
		case OneOfEach:
			return new EntityOneOfEachMatchFilter(comparator);
		case All:
			return new EntityAllMatchFilter(comparator);
		default:
			throw new IllegalArgumentException("Unregistered filter type : "+filter);
		}
	}
	
	// ---------------------- ENTITY MATCH ------------------------ //
	
	/**
	 * Get the filter that retain only one entity based on Hamming distance with referent
	 * 
	 * @param referent
	 * @return
	 */
	public EntityMatchFilter getEntityMatchFilter(IEntity<Attribute<? extends IValue>> referent) {
		return new EntityMatchFilter(new HammingEntityComparator(referent));
	}
	
	/**
	 * 
	 * @param referent
	 * @return
	 */
	public EntityMatchFilter getEntityMatchFilter(IValue... matches) {
		return new EntityMatchFilter(new HammingEntityComparator(matches));
	}
		
}
