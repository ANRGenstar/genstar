package gospl.algo.co.metamodel.solution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;
import gospl.GosplPopulationInDatabase;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.validation.GosplIndicatorFactory;

/**
 * A Synthetic population that provides a ready to access exploration feature in term of fitness and neighbors 
 * 
 * TODO : move fitness computation from layer 0 to multi-layered fitness : requires to create a new type of optimization algorithm (or extend the current 3)
 * 
 * @author kevinchapuis
 *
 */
public class MultiLayerSPSolution implements ISyntheticPopulationSolution {

	final private IPopulation<ADemoEntity, Attribute<? extends IValue>> population;
	final private Map<Integer, Double> layeredFitness;
	final private int layer;
	final private boolean subPopulationConstant;
	
	private boolean dataBasedPopulation;
	
	public MultiLayerSPSolution(IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			int layer, boolean subPopulationConstant, boolean dataBasedPopulation) {
		if(dataBasedPopulation)
			this.population = new GosplPopulationInDatabase(population);
		else
			this.population = population;
		this.dataBasedPopulation = dataBasedPopulation;
		this.layer = layer;
		this.subPopulationConstant = subPopulationConstant;
		this.layeredFitness = new HashMap<>();
	}
	
	public MultiLayerSPSolution(Collection<ADemoEntity> population, int layer, boolean subPopulationConstant, 
			boolean dataBasedPopulation) {
		if(dataBasedPopulation) {
			this.population = new GosplPopulationInDatabase();
			this.population.addAll(population);
		} else
			this.population = new GosplPopulation(population);
		this.dataBasedPopulation = dataBasedPopulation;
		this.layer = layer;
		this.subPopulationConstant = subPopulationConstant;
		this.layeredFitness = new HashMap<>();
	}
	
	// ----------------------- NEIGHBOR ----------------------- //
	
	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch) {
		return this.getNeighbors(neighborSearch,1);
	}

	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch,
			int k_neighbors) {
		return neighborSearch.getPredicates().stream()
				.map(u -> new MultiLayerSPSolution(
						neighborSearch.getNeighbor(this.population, u, k_neighbors, true),
						this.layer, this.subPopulationConstant, this.dataBasedPopulation))
				.collect(Collectors.toCollection(ArrayList::new)); 
	}

	@Override
	public <U> MultiLayerSPSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch) {
		return getRandomNeighbor(neighborSearch, 1);
	}

	@Override
	public <U> MultiLayerSPSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch,
			int k_neighbors) {
		return new MultiLayerSPSolution(
				neighborSearch.getNeighbor(this.population,
						GenstarRandomUtils.oneOf(neighborSearch.getPredicates()), k_neighbors, true),
				this.layer, this.subPopulationConstant, this.dataBasedPopulation);
	}

	// ----------------------- FITNESS & SOLUTION ----------------------- //
	
	@Override
	public Double getFitness(Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {
		return this.getFitness(0, objectives);
	}
	
	/**
	 * Return the fitness for a given layer
	 * @param layer
	 * @param objectives
	 * @return
	 */
	public Double getFitness(int layer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {
		if(layeredFitness.containsKey(layer)){
			return layeredFitness.get(layer);
		} else {
			final GSPerformanceUtil gspu = new GSPerformanceUtil("== Fitness Computation ==", Level.TRACE);
			double t = System.currentTimeMillis();
			
			// TODO test compatibility
			
			Set<Attribute<? extends IValue>> objAtt = objectives.stream()
					.flatMap(obj -> obj.getDimensions().stream())
					.collect(Collectors.toSet());
			
			IPopulation<ADemoEntity, Attribute<? extends IValue>> popLayer = this.getSolution(layer);
			objAtt = objAtt.stream().filter(att -> popLayer.getPopulationAttributes().stream()
					.anyMatch(popAtt -> att.isLinked(popAtt))).collect(Collectors.toSet());
			
			gspu.sysoStempMessage("Based on the distribution of "
					+objAtt.stream().map(Attribute::getAttributeName).collect(Collectors.joining(", "))
					+" attributes");
			
			if(objAtt.isEmpty()) {
				throw new IllegalArgumentException("Population attribute set does not match objectives attributes: "
						+ "\nMarginals: "+objectives.stream().flatMap(obj -> obj.getDimensions().stream())
							.map(Attribute::getAttributeName).collect(Collectors.joining("; "))
						+ "\nPopulation: "+popLayer.getPopulationAttributes().stream()
							.map(Attribute::getAttributeName).collect(Collectors.joining("; ")));
			}
			
			AFullNDimensionalMatrix<Integer> popMatrix = GosplNDimensionalMatrixFactory
					.getFactory().createContingency(objAtt,popLayer);
			
			gspu.sysoStempMessage("Build population contingency ("+popMatrix.getVal().getValue()
					+") for attributes: "+popMatrix.getDimensions().stream()
						.map(Attribute::getAttributeName).collect(Collectors.joining(", "))
					+" ("+String.valueOf((System.currentTimeMillis()-t)/1000)+"s)");
			
			for(IValue val : objAtt.stream().map(att -> att.getValueSpace().getValues().stream().findFirst().get())
					.collect(Collectors.toList())) {
				gspu.sysoStempMessage("Exemple comparison on value "+val.getStringValue()+": "
						+ "POP="+popMatrix.getVal(val,true)+" | MARGINAL="+objectives.iterator().next().getVal(val, true));
			}
			
			double t1 = System.currentTimeMillis();
			double fitness = objectives.stream().mapToDouble(obj -> GosplIndicatorFactory.getFactory()
					.getIntegerTAE(obj, popMatrix)).sum();
			gspu.sysoStempMessage("Compute fitness for given contingency: "+fitness+" ("+String.valueOf((System.currentTimeMillis()-t1)/1000)+"s)");
			
			layeredFitness.put(layer, fitness);
			return fitness;
		}
	}
	
	/**
	 * Return the fitness for all given layers
	 * @param objectives
	 * @return
	 */
	public Map<Integer,Double> getFitness(Map<Integer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>>> objectives) {
		return objectives.entrySet().stream().collect(Collectors.toMap(
				Entry::getKey, 
				entry -> getFitness(entry.getKey(), entry.getValue()))
				);
	}

	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSolution() {
		return population;
	}
	
	// Private utilities
	
	/*
	 * Return the nth layer population : 0 for layer with no child and max_arg(layer) the one with no parent
	 * WARNING : nasty cast
	 */
	private IPopulation<ADemoEntity, Attribute<? extends IValue>> getSolution(int layer) {
		if(layer==this.layer) {return this.getSolution();}
		IPopulation<ADemoEntity, Attribute<? extends IValue>> popLayer = population;
		if (layer < this.layer) {
			for (int l = this.layer; l > layer; l--) {
				popLayer = new GosplPopulation(popLayer.stream().flatMap(entity -> entity.getChildren().stream())
						.map(entity -> (ADemoEntity)entity)
						.collect(Collectors.toSet()));
				if(popLayer.isEmpty()) { throw new IllegalArgumentException("There is no "+l+" layer in the current population"); }
			}
		} else {
			for (int l = this.layer; l < layer; l++) {
				popLayer = new GosplPopulation(popLayer.stream().map(entity -> entity.getParent())
						.map(entity -> (ADemoEntity)entity)
						.collect(Collectors.toSet()));
				if(popLayer.isEmpty()) { throw new IllegalArgumentException("There is no "+l+" layer in the current population"); }
			}
		}
		return popLayer;
	}

}
