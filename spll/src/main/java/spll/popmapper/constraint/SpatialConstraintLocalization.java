package spll.popmapper.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;

public class SpatialConstraintLocalization extends ASpatialConstraint {

	Geometry bounds;
	protected IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> referenceFile;
	
	public SpatialConstraintLocalization(Geometry bounds) {
		super();
		this.bounds = bounds;
	}

	@Override
	public List<AGeoEntity<? extends IValue>> getCandidates(List<AGeoEntity<? extends IValue>> nests) {
		if (bounds == null) return nests;
		
		//System.out.println("nests: " + nests.size());
		List<AGeoEntity<? extends IValue>> cands = null;
		if (referenceFile != null) {
			cands = new ArrayList<>(referenceFile.getGeoEntityWithin(bounds));
			if (nests != null) {
				Collection<String> nestNames = new ArrayList<>();
				for (AGeoEntity<? extends IValue> nest : nests) {
					nestNames.add(nest.getGenstarName());
				}
				cands.removeIf(a -> !nestNames.contains(a.getGenstarName()));
			}
		} else {
			cands = nests.stream().filter(a -> a.getGeometry().intersects(bounds)).collect(Collectors.toList());
		}
		/*if (sortCandidates) 
			return cands.stream().sorted((n1, n2) -> Double.compare(bounds.getCentroid()
					.distance(n1.getGeometry()),bounds.getCentroid().distance(n2.getGeometry())))
					.collect(Collectors.toList());*/
		return cands;
	}

	@Override
	public boolean updateConstraint(AGeoEntity<? extends IValue> nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(Collection<AGeoEntity<? extends IValue>> nests) {
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


	public IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> getReferenceFile() {
		return referenceFile;
	}

	public void setReferenceFile(IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> referenceFile) {
		this.referenceFile = referenceFile;
	}


}
