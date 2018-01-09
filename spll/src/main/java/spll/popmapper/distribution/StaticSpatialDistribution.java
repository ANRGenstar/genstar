package spll.popmapper.distribution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.popmapper.distribution.function.ISpatialEntityFunction;

/**
 * Static Spatial Distribution that relies on spatial entity attribute to asses probability. For exemple,
 * probability could be computed based on the area of spatial entity.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class StaticSpatialDistribution<N extends Number, E extends ADemoEntity> implements ISpatialDistribution<E> {
	
	private Map<String,N> distributionTot;
	
	public StaticSpatialDistribution(List<? extends AGeoEntity<? extends IValue>> candidates, ISpatialEntityFunction<N> function) {
		distributionTot = new LinkedHashMap<>();
		for (AGeoEntity<? extends IValue> cand : candidates) {
			distributionTot.put(cand.getGenstarName(), function.apply(cand));
		}
		
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(E entity, List<? extends AGeoEntity<? extends IValue>> candidates) {
		List<N> distribution = candidates.size() ==  distributionTot.size() ? new ArrayList<>(distributionTot.values()) : candidates.stream().map( a -> distributionTot.get(a.getGenstarName())).collect(Collectors.toList());
		return RouletteWheelSelectionFactory.getRouletteWheel(distribution, candidates)
			.drawObject();
	}



}
