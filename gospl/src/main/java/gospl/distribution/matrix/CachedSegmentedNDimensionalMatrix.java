package gospl.distribution.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;

public class CachedSegmentedNDimensionalMatrix<T extends Number> 
		extends CachedNDimensionalMatrix<APopulationAttribute, APopulationValue, T>
		implements ISegmentedNDimensionalMatrix<T> {

	private final ISegmentedNDimensionalMatrix<T> mSeg;
	
	private final Collection<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> cachedSubMatrices;
	
	private final Map<APopulationAttribute,Set<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>>> attribute2involvedmatrices = new HashMap<>();
	
	public CachedSegmentedNDimensionalMatrix(ISegmentedNDimensionalMatrix<T> originalMatrix) {
		super(originalMatrix);
		
		mSeg = originalMatrix;
		
		// create list of cached sub matrices
		cachedSubMatrices = new ArrayList<>(originalMatrix.getMatrices().size());
		for (INDimensionalMatrix<APopulationAttribute, APopulationValue,T> m: originalMatrix.getMatrices()) {
			cachedSubMatrices.add(new CachedNDimensionalMatrix<APopulationAttribute, APopulationValue, T>(m));
		}
		
	}

	@Override
	public final Collection<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatrices() {
		return cachedSubMatrices;
	}

	@Override
	public final Set<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> getMatricesInvolving(APopulationAttribute att) {

		Set<INDimensionalMatrix<APopulationAttribute, APopulationValue,T>> res = attribute2involvedmatrices.get(att);
		
		if (res == null) {
			res = this.cachedSubMatrices.stream().filter(matrix -> matrix.getDimensions().contains(att)).collect(Collectors.toSet());
			attribute2involvedmatrices.put(att, res);
		}
				
		return res;

	}
	

	public long getHits() {
		
		long total = super.getHits();
		
		for (INDimensionalMatrix<APopulationAttribute, APopulationValue,T> subM: cachedSubMatrices) {
			total += ((CachedNDimensionalMatrix<APopulationAttribute, APopulationValue, Double>)subM).getHits();
		}
		
		return total;
	}
	
	public long getMissed() {
		
		long total = super.getMissed();

		for (INDimensionalMatrix<APopulationAttribute, APopulationValue,T> subM: cachedSubMatrices) {
			total += ((CachedNDimensionalMatrix<APopulationAttribute, APopulationValue, Double>)subM).getMissed();
		}
		return total;
	}

}
