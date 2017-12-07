package gospl.algo.ipf.margin;

import java.util.Collections;
import java.util.Set;

import core.metamodel.value.IValue;

public class MarginDescriptor {

	private Set<IValue> seed;
	private Set<IValue> control;
	
	public MarginDescriptor() { }
	
	public MarginDescriptor(Set<IValue> seed, Set<IValue> control) {
		this.seed = seed;
		this.control = control;
	}
	
	public Set<IValue> getSeed() {
		return Collections.unmodifiableSet(seed);
	}
	
	public MarginDescriptor setSeed(Set<IValue> seed) {
		this.seed = seed;
		return this;
	}
	
	public Set<IValue> getControl() {
		return Collections.unmodifiableSet(control);
	}
	
	public MarginDescriptor setControl(Set<IValue> control) {
		this.control = control;
		return this;
	}
	
}
