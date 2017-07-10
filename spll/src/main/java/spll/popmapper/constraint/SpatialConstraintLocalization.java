package spll.popmapper.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;

public class SpatialConstraintLocalization extends ASpatialConstraint {

	Geometry bounds;
	protected IGSGeofile<? extends AGeoEntity> referenceFile;
	
	public SpatialConstraintLocalization(Geometry bounds) {
		super();
		this.bounds = bounds;
	}

	@Override
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests) {
		if (bounds == null) return nests;
		//System.out.println("nests: " + nests.size());
		List<AGeoEntity> cands = null;
		if (referenceFile != null) {
			cands = new ArrayList<AGeoEntity>(referenceFile.getGeoEntityWithin(bounds));
			if (nests != null)cands.removeIf(a -> !nests.stream().anyMatch(b -> b.getGenstarName().equals(b.getGenstarName())));
		} else {
			cands = nests.stream().filter(a -> a.getGeometry().intersects(bounds)).collect(Collectors.toList());
		}
		if (sortCandidates) 
			return cands.stream().sorted((n1, n2) -> Double.compare(bounds.getCentroid()
					.distance(n1.getGeometry()),bounds.getCentroid().distance(n2.getGeometry())))
					.collect(Collectors.toList());
		return cands;
	}

	@Override
	public boolean updateConstraint(AGeoEntity nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(Collection<AGeoEntity> nests) {
		if (bounds != null) 
			bounds = bounds.buffer(increaseStep);
		else 
			currentValue = maxIncrease;
	}
	
	// ---------------------- //
	
	public Geometry getBounds() {
		return bounds;
	}

	public void setBounds(Geometry bounds) {
		this.bounds = bounds;
		currentValue = 0.0;
		constraintLimitReach = false;
	}


	public IGSGeofile<? extends AGeoEntity> getReferenceFile() {
		return referenceFile;
	}

	public void setReferenceFile(IGSGeofile<? extends AGeoEntity> referenceFile) {
		this.referenceFile = referenceFile;
	}


}
