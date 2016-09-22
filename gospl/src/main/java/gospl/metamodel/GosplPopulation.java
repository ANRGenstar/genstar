package gospl.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

public class GosplPopulation implements IPopulation {

	private final Collection<IEntity> population;
	
	/**
	 * Default inner type collection is {@link Set}
	 * 
	 */
	public GosplPopulation() {
		population = new HashSet<>();
	}
	
	/**
	 * Place the concrete type of collection you want this population be. If the propose
	 * collection is not empty, then default inner collection type is choose.
	 * 
	 * @see GosplPopulation()
	 * 
	 * @param population
	 */
	public GosplPopulation(Collection<IEntity> population){
		if(!population.isEmpty())
			this.population = new HashSet<>();
		else
			this.population = population;
	}
	
	@Override
	public int size() {
		return population.size();
	}

	@Override
	public boolean isEmpty() {
		return population.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return population.contains(o);
	}

	@Override
	public Iterator<IEntity> iterator() {
		return population.iterator();
	}

	@Override
	public Object[] toArray() {
		return population.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return population.toArray(a);
	}

	@Override
	public boolean add(IEntity e) {
		return population.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return population.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return population.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends IEntity> c) {
		return population.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return population.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return population.retainAll(c);
	}

	@Override
	public void clear() {
		population.clear();
	}
	
// ------------------------------------ POP ACCESSORS ------------------------------------ //
	
	public Set<IAttribute> getPopulationAttributes(){
		return population.parallelStream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
	}
	
// --------------------------------- POP REPORT METHODS --------------------------------- //
	
	@Override
	public String csvReport(CharSequence csvSep) {
		Set<IAttribute> attributes = this.getPopulationAttributes();
		String report = attributes.stream().map(att -> att.getName() + csvSep + "contingent" + csvSep + "pourcentage").collect(Collectors.joining(csvSep))+"\n";
		List<String> lines = new ArrayList<>();
		for(int i = 0; i < attributes.stream().mapToInt(att -> att.getValues().size()).max().getAsInt()+1; i++)
			lines.add("");
		for(IAttribute attribute : attributes){
			int lineNumber = 0;
			Set<IValue> vals = new HashSet<>(attribute.getValues());
			vals.add(attribute.getEmptyValue());
			for(IValue value : vals){
				long valCount = this.population
						.stream().filter(e -> e.getValues()
								.stream().anyMatch(ea -> ea.equals(value))).count();
				double valProp =  Math.round(Math.round(((valCount * 1d / this.population.size()) * 10000))) / 100d;
				if(lines.get(lineNumber).isEmpty())
					lines.set(lineNumber, value.getStringValue() + csvSep + valCount + csvSep + valProp);
				else
					lines.set(lineNumber, lines.get(lineNumber) + csvSep + value.getInputStringValue() + csvSep + valCount + csvSep + valProp);
				lineNumber++;
			}
			for(int i = lineNumber; i < lines.size(); i++)
				if(lines.get(i).isEmpty())
					lines.set(i, lines.get(i) + csvSep + "" + csvSep + "");
				else
					lines.set(i, lines.get(i) + csvSep + "" + csvSep + "" + csvSep + "");
		}
		report += String.join("\n", lines);
		return report;
	}

}
