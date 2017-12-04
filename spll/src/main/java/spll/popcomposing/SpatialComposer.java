package spll.popcomposing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import core.metamodel.IMultitypePopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.GosplMultitypePopulation;
import spll.SpllEntity;
import spll.SpllPopulation;
import spll.popmapper.constraint.ISpatialConstraint;

public class SpatialComposer implements ISpatialComposer<SpllEntity> {

	protected List<ISpatialConstraint> constraints = new LinkedList<ISpatialConstraint>();
	
	protected SpllPopulation populationOfParentCandidates;
	protected String parentType;
	
	protected SpllPopulation populationOfChildrenCandidates;
	protected String childrenType;
	
	protected Integer countEmptyParents = null;
	protected Integer countOrphanChildren = null;
	
	protected GosplMultitypePopulation<SpllEntity> resultPopulation = null;
	
	public SpatialComposer() {
		
		// start with an empty population
		resultPopulation = new GosplMultitypePopulation<>();
	}

	@Override
	public boolean addConstraint(ISpatialConstraint constraint) {
		return constraints.add(constraint);
	}

	@Override
	public void setConstraints(List<ISpatialConstraint> constraints) {
		this.constraints = constraints;
	}

	@Override
	public List<ISpatialConstraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	@Override
	public void setPopulationOfParentCandidates(SpllPopulation populationOfParentCandidates, String parentType) {
		
		assert parentType != null;
		
		this.parentType = parentType;
		this.populationOfParentCandidates = populationOfParentCandidates;
		
		// TODO cast o_O
		this.resultPopulation.addAll(parentType, populationOfParentCandidates);
		
		updateCountOfEmptyParentCandidates();
	}

	@Override
	public SpllPopulation getPopulationOfParentCandidates() {
		return populationOfParentCandidates;
	}

	@Override
	public void setPopulationOfChildrenCandidates(SpllPopulation populationOfChildrenCandidates, String childrenType) {
		this.childrenType = childrenType;
		this.populationOfChildrenCandidates = populationOfChildrenCandidates;
		
		// TODO cast o_O
		this.resultPopulation.addAll(childrenType, populationOfChildrenCandidates);

		updateCountOfOrphanChildrenCandidates();
	}

	@Override
	public SpllPopulation getPopulationOfChildrenCandidates() {
		return this.populationOfChildrenCandidates;
	}

	@Override
	public void matchParentsAndChildren() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCountEmptyParents() {
		if (countEmptyParents == null)
			throw new NullPointerException("the count of empty parents is null, maybe because there is no population of parents");
		return countEmptyParents;
	}

	@Override
	public int getCountOrphanChildren() {
		if (countOrphanChildren == null)
			throw new NullPointerException("the count of orphan children is null, maybe because there is no population of children");
		return countOrphanChildren;
	}

	@Override
	public IMultitypePopulation<SpllEntity, DemographicAttribute<? extends IValue>> getMatchedPopulation() {
		return resultPopulation;
	}
	
	@Override
	public void clearMatchedPopulation() {
		resultPopulation.clear();
		updateCountsOfOrphanEntities();
	}
	

	protected void updateCountsOfOrphanEntities() {
		updateCountOfEmptyParentCandidates();
		updateCountOfOrphanChildrenCandidates();
	}
	
	protected void updateCountOfEmptyParentCandidates() {
		
		if (this.populationOfParentCandidates == null) {
			this.countEmptyParents = null;
		} else {
			int c = 0;
			Iterator<SpllEntity> it = resultPopulation.iterateSubPopulation(this.parentType);
			while (it.hasNext()) {
				SpllEntity e = it.next();
				if (!e.hasChildren())
					c++;
			}
			this.countEmptyParents = c;
		}
		System.out.println("count of empty parents is now: "+countEmptyParents);
	}
	

	protected void updateCountOfOrphanChildrenCandidates() {
		
		if (this.populationOfChildrenCandidates == null) {
			this.countOrphanChildren = null;
		} else {
			int c = 0;
			Iterator<SpllEntity> it = resultPopulation.iterateSubPopulation(this.childrenType);
			while (it.hasNext()) {
				SpllEntity e = it.next();
				if (!e.hasParent())
					c++;
			}
			this.countOrphanChildren = c;
		}
		System.out.println("count of orphan parents is now: "+countOrphanChildren);

	}
	

	

}
