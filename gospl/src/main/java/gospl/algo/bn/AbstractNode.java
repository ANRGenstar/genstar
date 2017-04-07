package gospl.algo.bn;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractNode<ParentType extends AbstractNode<?>> {

	protected final String name;
	
	protected final Set<ParentType> parents = new HashSet<>();
	protected final Map<String,ParentType> name2parent = new HashMap<>();
	
	public AbstractNode(String name) {
		this.name = name;
	}

	public void addParent(ParentType parent) {
		if (parents.contains(parent)) {
			throw new IllegalArgumentException("parent "+parent.getName()+" already part of the node's parents");
		}
		parents.add(parent);
		name2parent.put(parent.getName(), parent);
	}
	
	public Set<ParentType> getParents() {
		return Collections.unmodifiableSet(parents);
	}
	
	protected ParentType getParent(String lbl) {
		return name2parent.get(lbl);
	}
	
	public boolean hasParents() {
		return !parents.isEmpty();
	}
	
	public final String getName() {
		return name;
	}
	
}
