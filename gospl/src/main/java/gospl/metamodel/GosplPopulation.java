package gospl.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.io.survey.entity.ASurveyEntity;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import core.metamodel.IPopulation;

public class GosplPopulation implements IPopulation<ASurveyEntity, ASurveyAttribute, ASurveyValue> {

	CoordinateReferenceSystem crs = null;
	
	private final Collection<ASurveyEntity> population;
	
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
	public GosplPopulation(Collection<ASurveyEntity> population){
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
	public Iterator<ASurveyEntity> iterator() {
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
	public boolean add(ASurveyEntity e) {
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
	public boolean addAll(Collection<? extends ASurveyEntity> c) {
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
	
	public Set<ASurveyAttribute> getPopulationAttributes(){
		return population.parallelStream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
	}
	
// --------------------------------- POP REPORT METHODS --------------------------------- //
	
	@Override
	public String csvReport(CharSequence csvSep) {
		Set<ASurveyAttribute> attributes = this.getPopulationAttributes();
		String report = attributes.stream().map(att -> att.getAttributeName() + csvSep + "contingent" + csvSep + "pourcentage").collect(Collectors.joining(csvSep))+"\n";
		List<String> lines = new ArrayList<>();
		for(int i = 0; i < attributes.stream().mapToInt(att -> att.getValues().size()).max().getAsInt()+1; i++)
			lines.add("");
		System.out.println("Headers done !");
		for(ASurveyAttribute attribute : attributes){
			int lineNumber = 0;
			Set<ASurveyValue> vals = new HashSet<>(attribute.getValues());
			vals.add(attribute.getEmptyValue());
			for(ASurveyValue value : vals){
				long valCount = this.population.parallelStream()
						.filter(e -> e.getValueForAttribute(attribute).equals(value)).count();
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
			System.out.println("Attribute "+attribute+" done !");
		}
		System.out.println("Start joining lines to one another");
		report += String.join("\n", lines);
		return report;
	}

	public CoordinateReferenceSystem getCrs() {
		return crs;
	}

	public void setCrs(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	
}
