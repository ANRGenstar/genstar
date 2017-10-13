package spll;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.metamodel.IPopulation;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;
import core.metamodel.pop.DemographicAttribute;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.APopulationValue;
import spll.util.SpllUtil;

public class SpllPopulation implements IPopulation<ADemoEntity, DemographicAttribute, APopulationValue> {

	private Collection<SpllEntity> population;
	private IGSGeofile<? extends AGeoEntity> geoFile; 

	private Set<DemographicAttribute> attributes;
	
	public SpllPopulation(IPopulation<ADemoEntity, DemographicAttribute, APopulationValue> population,
			IGSGeofile<? extends AGeoEntity> geoFile) {
		this.population = population.stream().map(entity -> new SpllEntity(entity))
				.collect(Collectors.toSet());
		this.attributes = population.getPopulationAttributes();
		this.geoFile = geoFile;
	}

	/**
	 * Gives the specific coordinate system this population
	 * have been localized with
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCrs(){
		return SpllUtil.getCRSfromWKT(geoFile.getWKTCoordinateReferentSystem());
	}
	
	/**
	 * Gives the geography this population is localized in
	 * 
	 * @return
	 */
	public IGSGeofile<? extends AGeoEntity> getGeography() {
		return geoFile;
	}
	
	/**
	 * Return spll entities
	 * 
	 * @return
	 */
	public Collection<SpllEntity> getSpllPopulation(){
		return population;
	}
	
	@Override
	public Set<DemographicAttribute> getPopulationAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	// ------------------------------------------- //
	// ----------- COLLECTION CONTRACT ----------- //
	// ------------------------------------------- //
	
	
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
	public Iterator<ADemoEntity> iterator() {
		return new ArrayList<ADemoEntity>(this).iterator();
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
	public boolean add(ADemoEntity e) {
		return population.add(new SpllEntity(e));
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
	public boolean addAll(Collection<? extends ADemoEntity> c) {
		return population.addAll(c.stream().map(e -> new SpllEntity(e))
				.collect(Collectors.toList()));
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

}
