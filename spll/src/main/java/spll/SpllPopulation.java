package spll;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.metamodel.IPopulation;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spll.util.SpllUtil;

public class SpllPopulation implements IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> {

	private IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population;
	private IGSGeofile<? extends AGeoEntity> geoFile; 

	public SpllPopulation(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			IGSGeofile<? extends AGeoEntity> geoFile) {
		this.population = population; 
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
	
	@Override
	public Set<APopulationAttribute> getPopulationAttributes() {
		return population.getPopulationAttributes();
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
	public Iterator<APopulationEntity> iterator() {
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
	public boolean add(APopulationEntity e) {
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
	public boolean addAll(Collection<? extends APopulationEntity> c) {
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

}
