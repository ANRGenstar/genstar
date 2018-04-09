package core.metamodel.entity.comparator;

import java.util.Comparator;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class HammingEntityComparator implements Comparator<IEntity<Attribute<? extends IValue>>> {

	private IEntity<Attribute<? extends IValue>> referent;
	private IDComparator defaultComparator = IDComparator.getInstance();

	public HammingEntityComparator(IEntity<Attribute<? extends IValue>> referent) {
		this.referent = referent;
	}

	@Override
	public int compare(IEntity<Attribute<? extends IValue>> o1, IEntity<Attribute<? extends IValue>> o2) {
		int scoreOne = (int) referent.getValues().stream().filter(v -> o1.getValues().contains(v)).count();
		int scoreTwo = (int) referent.getValues().stream().filter(v -> o2.getValues().contains(v)).count();
		return scoreOne > scoreTwo ? -1 : scoreOne < scoreTwo ? 1 : defaultComparator.compare(o1, o2);
	}

}
