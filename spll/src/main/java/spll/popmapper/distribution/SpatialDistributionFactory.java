package spll.popmapper.distribution;

import java.util.Collection;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;
import spll.popmapper.constraint.SpatialConstraintMaxNumber;
import spll.popmapper.distribution.function.AreaFunction;
import spll.popmapper.distribution.function.CapacityFunction;
import spll.popmapper.distribution.function.DistanceFunction;
import spll.popmapper.distribution.function.GravityFunction;
import spll.popmapper.distribution.function.ISpatialComplexFunction;
import spll.popmapper.distribution.function.ISpatialEntityFunction;

/**
 * Build distribution to asses spatial entity probability to be bind with synthetic population entity.
 * This factory if purely praticle, as it is very easy to build new distribution: you only need to choose
 * basic or complex distribution (or even build your own by implementing {@link ISpatialDistribution}) and
 * build it using cutsom function, like for example by implementing {@link ISpatialComplexFunction} 
 * @author kevinchapuis
 *
 */
public class SpatialDistributionFactory {

	private static SpatialDistributionFactory sdf = new SpatialDistributionFactory();
	
	private SpatialDistributionFactory() {}
	
	public static SpatialDistributionFactory getInstance() {
		return sdf;
	}
	
	/**
	 * General factory method to create distribution based on a function that transposed spatial entity into a number.
	 * Provided example includes, area based distribution {@link #getAreaBasedDistribution()} and capacity based distribution
	 * {@link #getCapacityBasedDistribution(SpatialConstraintMaxNumber)}
	 * 
	 * @param function
	 * @return
	 */
	public <N extends Number, E extends ADemoEntity> ISpatialDistribution<E> getDistribution(ISpatialEntityFunction<N> function){
		return new BasicSpatialDistribution<N, E>(function);
	}
	
	/**
	 * All provided spatial entities have the same probability - uniform distribution
	 * @return
	 */
	public <E extends ADemoEntity> ISpatialDistribution<E> getUniformDistribution(){
		return new BasicSpatialDistribution<>(new ISpatialEntityFunction<Integer>() {
			@Override public Integer apply(AGeoEntity<? extends IValue> t) {return 1;}
			@Override public void updateFunctionState(AGeoEntity<? extends IValue> entity) {}
		} );
	}
	
	/**
	 * Probability is computed as a linear function of spatial entity area. That is,
	 * the bigger the are is, the bigger will be the probability to be located in.
	 * 
	 * @return
	 */
	public <E extends ADemoEntity> ISpatialDistribution<E> getAreaBasedDistribution(){
		return new BasicSpatialDistribution<>(new AreaFunction());
	}
	
	/**
	 * Probability is computed as a linear function of spatial entity capacity. This capacity
	 * is provided by {@code scNumber} argument and can be dynamically updated
	 * @param scNumber
	 * @return
	 */
	public <E extends ADemoEntity> ISpatialDistribution<E> getCapacityBasedDistribution(SpatialConstraintMaxNumber scNumber){
		return new BasicSpatialDistribution<>(new CapacityFunction(scNumber));
	}
	
	// ----------------------------------------------------------------------------------- //
	
	/**
	 * General factory method to create distribution based on biFunction implementation that transposed 
	 * @param function
	 * @return
	 */
	public <N extends Number> ISpatialDistribution<SpllEntity> getDistribution(ISpatialComplexFunction<N> function){
		return new ComplexSpatialDistribution<N>(function);
	}
	
	/**
	 * Probability is computed as a linear function of distance between spatial and population entities
	 * @return
	 */
	public ISpatialDistribution<SpllEntity> getDistanceBasedDistribution(){
		return new ComplexSpatialDistribution<>(new DistanceFunction());
	}
	
	/**
	 * Gravity model that compute 
	 * 
	 * @param candidates
	 * @param entities
	 * @return
	 */
	public ISpatialDistribution<SpllEntity> getGravityModelDistribution(
			Collection<? extends AGeoEntity<? extends IValue>> candidates, 
			SpllEntity... entities){
		return new ComplexSpatialDistribution<>(new GravityFunction(candidates, entities));
	}
	
	/**
	 * 
	 * 
	 * @param candidates
	 * @param buffer
	 * @param entities
	 * @return
	 */
	public ISpatialDistribution<SpllEntity> getGravityModelDistribution(
			Collection<? extends AGeoEntity<? extends IValue>> candidates, 
			double buffer, SpllEntity... entities){
		return new ComplexSpatialDistribution<>(new GravityFunction(candidates, buffer, entities));
	}
	
}
