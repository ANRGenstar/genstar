package gospl.distribution.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.pop.DemographicAttribute;
import core.metamodel.value.IValue;

public class CachedSegmentedNDimensionalMatrix<T extends Number> 
		extends CachedNDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T>
		implements ISegmentedNDimensionalMatrix<T> {

	@SuppressWarnings("unused")
	private final ISegmentedNDimensionalMatrix<T> mSeg;
	
	private final Collection<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> cachedSubMatrices;
	
	private final Map<DemographicAttribute<? extends IValue>, 
		Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>>> attribute2involvedmatrices = new HashMap<>();
	
	public CachedSegmentedNDimensionalMatrix(ISegmentedNDimensionalMatrix<T> originalMatrix) {
		super(originalMatrix);
		
		mSeg = originalMatrix;
		
		// create list of cached sub matrices
		cachedSubMatrices = new ArrayList<>(originalMatrix.getMatrices().size());
		for (INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T> m: originalMatrix.getMatrices()) {
			cachedSubMatrices.add(new CachedNDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T>(m));
		}
		
	}

	@Override
	public final Collection<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> getMatrices() {
		return cachedSubMatrices;
	}

	@Override
	public final Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> getMatricesInvolving(DemographicAttribute<? extends IValue> att) {

		Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T>> res = attribute2involvedmatrices.get(att);
		
		if (res == null) {
			res = this.cachedSubMatrices.stream().filter(matrix -> matrix.getDimensions().contains(att)).collect(Collectors.toSet());
			attribute2involvedmatrices.put(att, res);
		}
				
		return res;

	}
	

	@SuppressWarnings("unchecked")
	public long getHits() {
		
		long total = super.getHits();
		
		for (INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T> subM: cachedSubMatrices) {
			total += ((CachedNDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double>)subM).getHits();
		}
		
		return total;
	}
	
	@SuppressWarnings("unchecked")
	public long getMissed() {
		
		long total = super.getMissed();

		for (INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue,T> subM: cachedSubMatrices) {
			total += ((CachedNDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double>)subM).getMissed();
		}
		return total;
	}

}
