package core.metamodel.attribute.emergent.filter;

import java.util.Comparator;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EntityChildFilterFactory {

	public enum EChildFilter {
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
	
	public IEntityChildFilter getFilter(EChildFilter filter,
			Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator) {
		switch (filter) {
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
	
}
