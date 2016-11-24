package spll.popmatcher;

import core.io.geo.IGSGeofile;
import core.io.geo.ShapeFile;
import core.metamodel.IPopulation;

public class SpllUniformPopulationMatcher implements ISpllPopulationMatcher {

	@SuppressWarnings("rawtypes")
	private IPopulation population;
	private ShapeFile match;
	private IGSGeofile localisation;

	public SpllUniformPopulationMatcher(@SuppressWarnings("rawtypes") IPopulation population, ShapeFile match, IGSGeofile localisation) {
		this.population = population;
		this.match = match;
		this.localisation = localisation;
	}
	
	
	@Override
	public IPopulation localisePopulation() {
		// TODO Auto-generated method stub
		
		// TODO: how to match attribute feature and attribute individual, both are IEntity
		
		// TODO: for each feature, randomly spread individual with a fitness (reproduce in each cells the feature distribution)
		
		return population;
	}

}
