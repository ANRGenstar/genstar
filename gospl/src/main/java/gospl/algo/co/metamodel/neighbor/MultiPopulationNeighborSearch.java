package gospl.algo.co.metamodel.neighbor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandomUtils;
import gospl.GosplMultitypePopulation;
import gospl.GosplPopulation;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.validation.GosplIndicatorFactory;

public class MultiPopulationNeighborSearch implements IPopulationNeighborSearch<GosplMultitypePopulation<ADemoEntity>, 
	INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> {

	private IPopulationNeighborSearch<GosplPopulation, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> search;
	
	private Map<Integer, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> predicates;
	private Map<Integer, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives;
	
	private int maxSizeGrowthFactor = 3;

	private GosplMultitypePopulation<ADemoEntity> sample;
		
	public MultiPopulationNeighborSearch() {
		this.search = new PopulationRandomNeighborSearch<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>>();
	}
	
	public MultiPopulationNeighborSearch(IPopulationNeighborSearch<GosplPopulation, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> search) {
		this.search = search;
	}
	
	@Override
	public Map<ADemoEntity, ADemoEntity> getPairwisedEntities(GosplMultitypePopulation<ADemoEntity> population, 
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> predicate, int size) {
		return this.getPairwisedEntities(population, predicate, size, false);
	}

	@Override
	public Map<ADemoEntity, ADemoEntity> getPairwisedEntities(GosplMultitypePopulation<ADemoEntity> population, 
			int size, boolean childSizeConsistant) {
		return this.getPairwisedEntities(population, GenstarRandomUtils.oneOf(this.getPredicates()), size, childSizeConsistant);
	}
	
	@Override
	public Map<ADemoEntity, ADemoEntity> getPairwisedEntities(GosplMultitypePopulation<ADemoEntity> population, 
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> predicate, int size, boolean childSizeConsistant) {
		return this.getPairwisedEntities(population, predicate, Collections.max(sample.getEntityLevel()), size, childSizeConsistant);
	}

	/**
	 * Get Pairwised (top layer) entities according to a predicate applied at given layer
	 * 
	 * @param population
	 * @param predicate
	 * @param layer
	 * @param size
	 * @param childSizeConsistant
	 * @return
	 */
	public Map<ADemoEntity, ADemoEntity> getPairwisedEntities(GosplMultitypePopulation<ADemoEntity> population,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> predicate, int layer, int size, boolean childSizeConsistant) {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil(this.getClass().getCanonicalName()+"#getPairwisedEntities", Level.TRACE);
	
		GosplPopulation layeredSample = new GosplPopulation(population.getSubPopulation(layer));
		GosplPopulation layeredToDraw = new GosplPopulation(this.sample.getSubPopulation(layer));
		search.setSample(layeredToDraw);
		
		if(layeredToDraw.stream().anyMatch(e -> e.getEntityId()==null||e.getEntityId().isEmpty())) {
			throw new IllegalStateException("Entities to swap with from original sample should have ID's");
		}
		
		Map<ADemoEntity,ADemoEntity> pairs = search.getPairwisedEntities(layeredSample, predicate, size, childSizeConsistant);
		
		gspu.sysoStempPerformance("Propose swaps ("+pairs.size()+"):\n".concat(pairs.entrySet().stream().limit(10)
				.map(Entry::toString).collect(Collectors.joining("\n"))),this.getClass());
		
		List<Integer> subEs = population.getEntityLevel().stream()
				.filter(lvl -> lvl < layer).collect(Collectors.toList());
		// Do we have to look for sub-entities ?
		// Normally, the sub-search engine should have take care of sub-entities consistency: i.e. proposed pair of
		// add/remove entities have the same number of sub-entities (or closest found)
		if(subEs.size()>1) { throw new UnsupportedOperationException("Cannot yet operate combinatorial optimization "
				+ "on synthetic population with more than 2 layeres");}
		
		List<Integer> supEs = population.getEntityLevel().stream()
				.filter(lvl -> lvl > layer).collect(Collectors.toList());
		if(supEs.size()>1) { throw new UnsupportedOperationException("Cannot yet operate combinatorial optimization "
				+ "on synthetic population with entities encapsulated in more than 1 group (e.g. individuals > households > classes");}
		// Do we have to lock layered entities to be within a super-entity ? (i.e. swap all individual of a household, not just part of it)
		if(supEs.size()==1) {
			Set<ADemoEntity> cUpsReadyToSwap = new HashSet<>();
			Set<ADemoEntity> pUpsReadyToSwap = new HashSet<>();
			
			gspu.sysoStempMessage("Trying to build upper level entities from child drawn");
			
			int sizeFactor = 1;
			do {
				
				Map<ADemoEntity, ADemoEntity> layeredSwaps = sizeFactor++ == 1 ?  pairs :
					search.getPairwisedEntities(layeredSample, predicate, size*sizeFactor++, childSizeConsistant);
				
				// Get a map of PARENT :: CHILDS_IN_LAYERED_SWAPS
				Map<ADemoEntity,Set<ADemoEntity>> currentUpToSwap = layeredSwaps.keySet().stream().collect(
						Collectors.groupingBy(e -> (ADemoEntity) e.getParent(), Collectors.toSet()));
				Map<ADemoEntity,Set<ADemoEntity>> proposedUpToSwap = layeredSwaps.values().stream().collect(
						Collectors.groupingBy(e -> (ADemoEntity) e.getParent(), Collectors.toSet()));
				
				cUpsReadyToSwap.addAll(
						currentUpToSwap.keySet().stream()
							.filter(key -> currentUpToSwap.get(key).containsAll(key.getChildren())
									&& !pairs.containsKey(key))
							.collect(Collectors.toSet()));
				pUpsReadyToSwap.addAll(
						proposedUpToSwap.keySet().stream()
							.filter(key -> proposedUpToSwap.get(key).containsAll(key.getChildren())
									&& !pairs.containsValue(key))
							.collect(Collectors.toSet()));
				
				int sizeToAdd = size - pairs.size();
				if( cUpsReadyToSwap.isEmpty() || pUpsReadyToSwap.isEmpty() ) { continue; }
				else {
					sizeToAdd = IntStream.of(sizeToAdd, (cUpsReadyToSwap.size() > pUpsReadyToSwap.size() ? 
								pUpsReadyToSwap.size() : cUpsReadyToSwap.size()) )
							.min().getAsInt();
					for(ADemoEntity current : cUpsReadyToSwap.stream().limit(sizeToAdd).collect(Collectors.toSet())) {
						ADemoEntity proposed = GenstarRandomUtils.oneOf(pUpsReadyToSwap);
						pairs.put(current, proposed);
						pUpsReadyToSwap.remove(proposed);
					}
				}

			} while (pairs.size() >=  size || sizeFactor == maxSizeGrowthFactor);
			
		}
		return pairs;
	}

	@Override
	public Collection<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> getPredicates() {
		return Collections.unmodifiableCollection(predicates.values());
	}

	@Override
	public void setPredicates(Collection<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> predicates) {
		if(predicates.isEmpty()) {throw new IllegalArgumentException("Predicat cannot be empty");}
		if(this.predicates == null) { this.predicates = new HashMap<>(); }
		Iterator<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> iterPredicates = predicates.iterator();
		int layer = 0;
		while(iterPredicates.hasNext()) { this.predicates.put(layer++, iterPredicates.next()); }
	}

	@Override
	public void updatePredicates(GosplMultitypePopulation<ADemoEntity> population) {
		for(Integer layer : new ArrayList<>(objectives.keySet())) {
			Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> obj = 
					new HashSet<>(Arrays.asList(objectives.get(layer)));
			this.setPredicates(Collections.singleton(GosplIndicatorFactory
					.getFactory().getAbsoluteErrors(population.getSubPopulation(layer), predicates.get(layer), obj)));
		}
	}

	@Override
	public void setSample(GosplMultitypePopulation<ADemoEntity> sample) {
		this.sample = sample;
	}
	
	/**
	 * Add control marginals to propose neighbored population that complies with attributes value to compensate for  </p>
	 * i.e. negative contingencies are people's attributes to look for, while positive are attribute to discard
	 * 
	 * @param objectif
	 */
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectif, int layer) {
		if(this.objectives==null) { this.objectives = new HashMap<>(); }
		this.objectives.put(layer, objectif);
		if(this.predicates==null) { this.predicates = new HashMap<>(); }
		this.predicates.put(layer, GosplNDimensionalMatrixFactory.getFactory()
				.createEmtpyContingencies(this.objectives.get(layer).getDimensions(), true));
	}

}
