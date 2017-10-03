package core.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;

public class GenstarDictionary implements Map<IAttribute, List<String>> {

	private Map<IAttribute, List<String>> dictionary = new HashMap<>();

	@Override
	public int size() {
		return dictionary.size();
	}

	@Override
	public boolean isEmpty() {
		return dictionary.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return dictionary.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return dictionary.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return dictionary.get(key);
	}

	@Override
	public List<String> put(IAttribute key, List<String> value) {
		return dictionary.put(key, value);
	}

	@Override
	public List<String> remove(Object key) {
		return dictionary.remove(key);
	}

	@Override
	public void putAll(Map<? extends IAttribute, ? extends List<String>> m) {
		dictionary.putAll(m);
	}

	@Override
	public void clear() {
		dictionary.clear();
	}

	@Override
	public Set<IAttribute> keySet() {
		return dictionary.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return dictionary.values();
	}

	@Override
	public Set<java.util.Map.Entry<IAttribute, List<String>>> entrySet() {
		return dictionary.entrySet();
	}
	
}
