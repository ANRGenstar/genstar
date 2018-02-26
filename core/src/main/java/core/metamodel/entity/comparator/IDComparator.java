package core.metamodel.entity.comparator;

import java.util.Comparator;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class IDComparator implements Comparator<IEntity<? extends IAttribute<? extends IValue>>> {

	public static final String SELF = "DEFAULT COMPARATOR";
	
	static private IDComparator INSTANCE = new IDComparator();
	
	private IDComparator() {}
	
	protected static IDComparator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public int compare(IEntity<? extends IAttribute<? extends IValue>> o1,
			IEntity<? extends IAttribute<? extends IValue>> o2) {
		return o1.getEntityId().compareTo(o2.getEntityId());
	}

}
