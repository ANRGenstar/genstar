package gospl.algo.bn;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class FiniteNode<N extends FiniteNode<N>>  extends AbstractNode<N> {

	protected List<String> domain = new LinkedList<>();
	
	public FiniteNode(BayesianNetwork<N> net, String name) {
		super(net, name);
	}


	/**
	 * (internal) Adds a value into the domain of the categorical variable with no verification. 
	 * @param vv
	 */
	private void _addDomain(String vv) {
		domain.add(vv);
	}
	
	/**
	 * Adds a value in the domain if it does not already exists with the same name, 
	 * and adapts the internal size for the content (thus loosing any data there before)
	 * @param vv
	 */
	public void addDomain(String vv) {
		if (domain.contains(vv)) {
			throw new IllegalArgumentException(vv+" is already part of the domain");
		}
		this._addDomain(vv);
		adaptContentSize();
	}
	
	/**
	 * Adds several values in the domain and adapts internal storage .
	 * @param vvs
	 */
	public void addDomain(String ... vvs) {
		
		// check params
		for (String vv : vvs) {
			if (domain.contains(vv)) {
				throw new IllegalArgumentException(vv+" is already part of the domain");
			}
		}
		
		// add values
		for (String vv : vvs) {
			_addDomain(vv);
		}
		
		// adapt cpt size
		adaptContentSize();
	}

	protected abstract void adaptContentSize();
	
	public Collection<String> getDomain() {
		return Collections.unmodifiableList(domain);
	}
	
	public String getValueIndexed(int v) {
		return domain.get(v);
	}
	
	public int getDomainIndex(String value) {
		return domain.indexOf(value);
	}

	public int getDomainSize() {
		return domain.size();
	}
	
}
