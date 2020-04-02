package gospl.sampler.multilayer.co;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.AMultiLayerOptimizationAlgorithm;
import gospl.algo.co.metamodel.solution.MultiLayerSPSolution;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.co.MicroDataSampler;

/**
 * Draw multi layered entities according to layer marginals
 * 
 * TODO : replace uniform draw from sample to weighted based draws
 * 
 * @author kevinchapuis
 *
 * @param <A>
 */
public class GosplBiLayerOptimizationSampler<A extends AMultiLayerOptimizationAlgorithm> implements ICOMultiLayerSampler {
	
	private Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> parentObjectives;
	private Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> childObjectives;
	
	private int parentSizeConstraint;
	private int childSizeConstraint;
	
	private final A algorithm;
	private final MicroDataSampler childSampler;
	private final MicroDataSampler parentSampler;
	
	public GosplBiLayerOptimizationSampler(A algorithm) {
		this.algorithm = algorithm;
		this.childSampler = new MicroDataSampler();
		this.parentSampler = new MicroDataSampler();
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample, boolean withWeigths) {
		this.algorithm.setSample(sample);
	}
	
	@Override
	public void setSample(Map<Integer, IPopulation<ADemoEntity, Attribute<? extends IValue>>> samples, boolean withWeights, int layer) {
		this.checkLayer(layer);
		
		if(samples.containsKey(0)) { this.childSampler.setSample(samples.get(0), withWeights); }
		if(samples.containsKey(1)) { this.parentSampler.setSample(samples.get(1), withWeights); }
		
		this.algorithm.setSample(samples.get(layer));
		this.algorithm.setSampledLayer(layer);
		
	}

	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		if (childObjectives == null && parentObjectives == null) {
			this.addObjectives(objectives, 0);
		} else if (childObjectives != null &&
					childObjectives.stream().flatMap(ob -> ob.getDimensions().stream())
					.anyMatch(dim -> objectives.getDimensions().contains(dim))) {
			this.addObjectives(objectives,0);
		} else if(parentObjectives != null &&
					parentObjectives.stream().flatMap(ob -> ob.getDimensions().stream())
					.anyMatch(dim -> objectives.getDimensions().contains(dim))) {
			this.addObjectives(objectives, 1);
		}
		throw new IllegalArgumentException("Try to setup an objectif for "+GosplBiLayerOptimizationSampler.class.getCanonicalName()
				+" sampler but cannot fit it to child or parent layer");
	}
	
	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives, int layer) {
		this.checkLayer(layer);
		this.algorithm.addObjectives(layer, objectives);
		if(layer==0) { if (childObjectives == null) { this.childObjectives = new HashSet<>(); } 
			this.childObjectives.add(objectives);
			this.childSizeConstraint = objectives.getVal().getValue();
		}
		if(layer==1) { if (parentObjectives == null) {this.parentObjectives = new HashSet<>();} 
			this.parentObjectives.add(objectives);
			this.parentSizeConstraint = objectives.getVal().getValue();
		}
	}

	@Override
	public ADemoEntity draw() {
		return this.algorithm.getSampledLayer()==0 ? this.childSampler.draw() : this.parentSampler.draw();
	}
	
	@Override
	public ADemoEntity drawFromLayer(int layer) {
		this.checkLayer(layer);
		return layer==0 ? this.childSampler.draw() : this.parentSampler.draw();
	}
	
	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		Collection<ADemoEntity> startingSolution;
		if(this.algorithm.getSampledLayer()==0) {
			numberOfDraw = childSizeConstraint==0 ? numberOfDraw : (numberOfDraw < childSizeConstraint ? numberOfDraw : childSizeConstraint);
			startingSolution = this.childSampler.draw(numberOfDraw);
		} else {
			if(parentSizeConstraint==0 && childSizeConstraint > 0) { startingSolution = this.parentSampler.drawWithChildrenNumber(numberOfDraw); }
			else if(parentSizeConstraint>0) {startingSolution = this.parentSampler.draw(numberOfDraw>parentSizeConstraint?parentSizeConstraint:numberOfDraw); }
			else {startingSolution = this.parentSampler.draw(numberOfDraw);}
		}
		return this.algorithm.run(new MultiLayerSPSolution(startingSolution, algorithm.getSampledLayer(), true, false)).getSolution();
	}

	@Override
	public Collection<ADemoEntity> drawFromLayer(int layer, int numberOfDraw) {
		this.checkLayer(layer);
		Collection<ADemoEntity> statingSolution = layer==0 ? this.childSampler.draw(numberOfDraw) : this.parentSampler.draw(numberOfDraw);
		return this.algorithm.run(new MultiLayerSPSolution(statingSolution, algorithm.getSampledLayer(), true, false)).getSolution();
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean checkLayer(int layer) {
		if(layer==0 || layer==1) {return true;} 
		throw new IllegalArgumentException("GosplBiLayerSampler accepts 0 (child) or 1 (parent) layer but not "+layer);
	}

}
