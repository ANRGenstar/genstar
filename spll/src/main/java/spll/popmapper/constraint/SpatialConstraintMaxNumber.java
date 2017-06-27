package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.metamodel.geo.AGeoEntity;

public class SpatialConstraintMaxNumber extends ASpatialConstraint{

	protected Map<String, Integer> nestCapacities;
	
	
	//maxVal: global value for the max number of entities per nest
	public SpatialConstraintMaxNumber(Collection<? extends AGeoEntity> nests, Double maxVal) {
		super();
		nestCapacities = computeMaxPerNest(nests, maxVal);
	}
	
	 //keyAttMax: name of the attribute that contains the max number of entities in the nest file
	public SpatialConstraintMaxNumber(Collection<? extends AGeoEntity> nests, String keyAttMax) {
		super();
		nestCapacities = computeMaxPerNest(nests, keyAttMax);
	}
	
	@Override
	public void relaxConstraintOp(Collection<AGeoEntity> nests) {
		for (AGeoEntity n : nests )
			nestCapacities.put(n.getGenstarName(), (int)Math.round(nestCapacities.get(n.getGenstarName()) + increaseStep));
	}


	@Override
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests) {
		List<AGeoEntity> candidates = nests.stream().filter(a -> nestCapacities.get(a.getGenstarName()) > 0).collect(Collectors.toList());
		if (!sortCandidates)
			return candidates;
		return candidates.stream().sorted((n1, n2) -> Integer.compare(-1 * nestCapacities.get(n1.getGenstarName()),
				-1 * nestCapacities.get(n2.getGenstarName()))).collect(Collectors.toList());
	}
	
	@Override
	public boolean updateConstraint(AGeoEntity nest) {
		int capacity = nestCapacities.get(nest.getGenstarName());
		nestCapacities.put(nest.getGenstarName(), capacity - 1);
		if (capacity <= 1) return true;
		return false;
			
	}

	
	protected Map<String, Integer> computeMaxPerNest(Collection<? extends AGeoEntity> nests, String keyAttMax){
		return nests.stream().collect(Collectors.toMap(a -> ((AGeoEntity) a).getGenstarName(), 
							a-> (int)(((AGeoEntity) a).getValueForAttribute(keyAttMax).getNumericalValue().intValue())));
	}
	
	protected Map<String, Integer> computeMaxPerNest(Collection<? extends AGeoEntity> nests, Double maxVal){
		return nests.stream().collect(Collectors.toMap(a -> ((AGeoEntity) a).getGenstarName(), 
						a-> (int)(Math.round(maxVal))));
	}
	
}
