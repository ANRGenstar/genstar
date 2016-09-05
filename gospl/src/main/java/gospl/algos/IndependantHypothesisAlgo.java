package gospl.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.GosplAliasSampler;
import gospl.algos.sampler.GosplBasicSampler;
import gospl.algos.sampler.GosplBinarySampler;
import gospl.algos.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.survey.GosplMetaDataType;
import io.util.GSPerformanceUtil;

/**
 * TODO: explain
 * 
 * TODO: find a way to choose the sampling algorithm, e.g. {@link GosplAliasSampler}, {@link GosplBasicSampler} or {@link GosplBinarySampler}
 * HINT: choose a builder that take a {@link IDistributionInferenceAlgo} and an empty {@link ISampler} to fill with
 * 
 * @author kevinchapuis
 *
 */
public class IndependantHypothesisAlgo implements IDistributionInferenceAlgo<IAttribute, IValue> {

	private boolean DEBUG_SYSO;

	public IndependantHypothesisAlgo(boolean DEBUG_SYSO) {
		this.DEBUG_SYSO = DEBUG_SYSO;
	}

	@Override
	public ISampler<ACoordinate<IAttribute, IValue>> inferDistributionSampler(
			INDimensionalMatrix<IAttribute, IValue, Double> matrix) throws IllegalDistributionCreation, GosplSamplerException {
		if(matrix == null || matrix.getMatrix().isEmpty())
			throw new IllegalArgumentException("matrix passed in parameter cannot be null or empty");
		if(!matrix.isSegmented() && matrix.getMetaDataType().equals(GosplMetaDataType.LocalFrequencyTable))
			throw new IllegalDistributionCreation("can't create a sampler using only one matrix of GosplMetaDataType#LocalFrequencyTable");

		// Begin the algorithm (and performance utility)
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute independant-hypothesis-joint-distribution from conditional distribution\nTheoretical size = "+
				matrix.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2), DEBUG_SYSO);
		gspu.getStempPerformance(0);

		// Stop the algorithm and exit the unique matrix if there is only one
		if(!matrix.isSegmented())
			return new GosplBasicSampler(matrix);


		/////////////////////////////////////
		// 1st STEP: identify the various inner matrices
		/////////////////////////////////////

		// Cast matrix to access inner full matrices
		ASegmentedNDimensionalMatrix<Double> segmentedMatrix = (ASegmentedNDimensionalMatrix<Double>) matrix;

		// Init sample distribution simple expression
		Map<Set<IValue>, Double> sampleDistribution = new HashMap<>();

		// Init collection to store processed attributes & matrix
		Set<IAttribute> allocatedAttributes = new HashSet<>();
		Set<AFullNDimensionalMatrix<Double>> unallocatedMatrices = new HashSet<>(segmentedMatrix.getMatrices());

		/////////////////////////////////////
		// 2nd STEP: disaggregate attributes & start processing associated matrices
		/////////////////////////////////////

		// First identify referent & aggregated attributes:
		Set<IAttribute> refAtts = segmentedMatrix.getDimensions()
				.stream().filter(d -> !d.isRecordAttribute() && !d.getReferentAttribute().equals(d))
				.map(d -> d.getReferentAttribute()).collect(Collectors.toSet());
		Map<IAttribute, Set<IAttribute>> aggAttributeMap = refAtts
				.stream().collect(Collectors.toMap(refDim -> refDim, refDim -> segmentedMatrix.getDimensions()
						.stream().filter(aggDim -> aggDim.getReferentAttribute().equals(refDim)).collect(Collectors.toSet())));

		// Iterate over matrices that have referent attributes and no aggregated attributes
		List<AFullNDimensionalMatrix<Double>> refMatrices = segmentedMatrix.getMatrices().stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(dim -> aggAttributeMap.keySet().contains(dim) 
						&& !aggAttributeMap.values().stream().flatMap(set -> set.stream()).anyMatch(aggDim -> dim.equals(aggDim))))
				.sorted((mat1, mat2) -> (int) mat2.getDimensions().stream().filter(dim2 -> aggAttributeMap.keySet().contains(dim2)).count()
						- (int) mat1.getDimensions().stream().filter(dim1 -> aggAttributeMap.keySet().contains(dim1)).count())
				.collect(Collectors.toList());
		for(AFullNDimensionalMatrix<Double> mat : refMatrices){
			sampleDistribution = updateGosplProbaMap(sampleDistribution, mat, gspu);
			unallocatedMatrices.remove(mat);
			allocatedAttributes.addAll(mat.getDimensions());
		}
		
		// Iterate over matrices that have aggregated attributes
		List<AFullNDimensionalMatrix<Double>> aggMatrices = unallocatedMatrices.stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(dim -> aggAttributeMap.values()
						.stream().flatMap(set -> set.stream()).anyMatch(aggDim -> dim.equals(aggDim))))
				.sorted((mat1, mat2) -> (int) mat2.getDimensions().stream().filter(dim2 -> aggAttributeMap.keySet().contains(dim2)).count()
						- (int) mat1.getDimensions().stream().filter(dim1 -> aggAttributeMap.keySet().contains(dim1)).count())
				.collect(Collectors.toList());
		for(AFullNDimensionalMatrix<Double> mat : aggMatrices){
			// Store mapped relationship between aggregated (key) and disaggregated (values) values
			Set<IAttribute> aggregAttSet = matrix.getDimensions()
					.stream().filter(a -> !a.getReferentAttribute().equals(a))
					.collect(Collectors.toSet());
			Map<IValue, Set<IValue>> aggToRefValues = new HashMap<>();
			// For known matching
			for(IValue val : aggregAttSet.stream().flatMap(att -> att.getValues().stream()).collect(Collectors.toSet()))
				aggToRefValues.put(val, val.getAttribute().getReferentAttribute().getValues()
						.stream().filter(disVal -> val.getAttribute().findMatchingAttributeValue(disVal).equals(val))
						.collect(Collectors.toSet()));
			// For values that have no aggregate counterpart
			for(IAttribute att : aggregAttSet)
				aggToRefValues.put(att.getEmptyValue(), att.getReferentAttribute().getValues()
					.stream().filter(val -> aggToRefValues.values().stream().allMatch(vals -> !vals.contains(val)))
				.collect(Collectors.toSet()));
			// Iterate over the old sampleDistribution to had new disaggregate values
			for(Set<IValue> indiv : sampleDistribution.keySet()){
				Set<IValue> hookVal = Stream.concat(indiv.stream().filter(val -> mat.getAspects().contains(val)), 
						aggToRefValues.keySet().stream().filter(k -> aggToRefValues.get(k)
								.stream().anyMatch(val -> indiv.contains(val))))
						.collect(Collectors.toSet());
				Map<ACoordinate<IAttribute, IValue>, AControl<Double>> coordsHooked = mat.getMatrix().entrySet()
						.parallelStream().filter(e -> e.getKey().containsAll(hookVal))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				for(ACoordinate<IAttribute, IValue> HCoord : coordsHooked.keySet()){
					
				}
			}
		}

		////////////////////////////////////
		// 4th STEP: proceed the remaining matrices
		////////////////////////////////////

		for(AFullNDimensionalMatrix<Double> mat : unallocatedMatrices){
			gspu.sysoStempPerformance(0d, this);

			// If "hookAtt" is empty fill the proxy distribution with conditional probability of this joint distribution
			sampleDistribution = updateGosplProbaMap(sampleDistribution, mat, gspu);
			allocatedAttributes.addAll(matrix.getDimensions());

			gspu.resetStempProp();
			gspu.sysoStempPerformance(1, this);
		}

		long wrongPr = sampleDistribution.keySet().parallelStream().filter(c -> c.size() != segmentedMatrix.getDimensions().size()).count();
		double avrSize =  sampleDistribution.keySet().parallelStream().mapToInt(c -> c.size()).average().getAsDouble();
		if(wrongPr != 0)
			throw new IllegalDistributionCreation("Some sample indiv ("+( Math.round(Math.round(wrongPr * 1d / sampleDistribution.size() * 100)))+"%) have not all attributs (average attributs nb = "+avrSize+")");
		
		LinkedHashMap<ACoordinate<IAttribute, IValue>, Double> sdMap = sampleDistribution.entrySet()
				.parallelStream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(e -> safeCoordinateCreation(e.getKey(), gspu), e -> e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));
		return new GosplBasicSampler(sdMap);
	}

	
	
	// ------------------------------ inner utility methods ------------------------------ //

	
	// Fake methode in order to create coordinate in lambda java 8 stream operation
	private ACoordinate<IAttribute, IValue> safeCoordinateCreation(Set<IValue> coordinate, GSPerformanceUtil gspu){
		ACoordinate<IAttribute, IValue> coord = null;
		try {
			coord = new GosplCoordinate(coordinate);
		} catch (MatrixCoordinateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return coord;
	}

	private Map<Set<IValue>, Double> updateGosplProbaMap(Map<Set<IValue>, Double> sampleDistribution, 
			AFullNDimensionalMatrix<Double> matrix, GSPerformanceUtil gspu){
		Map<Set<IValue>, Double> updatedSampleDistribution = new HashMap<>();
		if(sampleDistribution.isEmpty()){
			updatedSampleDistribution.putAll(matrix.getMatrix().entrySet()
					.parallelStream().collect(Collectors.toMap(e -> new HashSet<>(e.getKey().values()), e -> e.getValue().getValue())));
		} else {
			int j = 1;
			Set<IAttribute> allocatedAttributes = sampleDistribution.keySet()
					.parallelStream().flatMap(set -> set.stream()).map(a -> a.getAttribute()).collect(Collectors.toSet());
			Set<IAttribute> hookAtt = matrix.getDimensions()
					.stream().filter(att -> allocatedAttributes.contains(att)).collect(Collectors.toSet());
			for(Set<IValue> indiv : sampleDistribution.keySet()){
				Set<IValue> hookVal = indiv.stream().filter(val -> hookAtt.contains(val.getAttribute()))
						.collect(Collectors.toSet());
				Map<ACoordinate<IAttribute, IValue>, AControl<Double>> coordsHooked = matrix.getMatrix().entrySet()
						.parallelStream().filter(e -> e.getKey().containsAll(hookVal))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				double summedProba = coordsHooked.values()
						.stream().reduce(matrix.getNulVal(), (v1, v2) -> v1.add(v2)).getValue();
				for(ACoordinate<IAttribute, IValue> newIndivVal : coordsHooked.keySet()){
					Set<IValue> newIndiv = Stream.concat(indiv.stream(), newIndivVal.values().stream()).collect(Collectors.toSet());
					double newProba = sampleDistribution.get(indiv) * coordsHooked.get(newIndivVal).getValue() / summedProba;
					if(newProba > 0d)
						updatedSampleDistribution.put(newIndiv, newProba);
				}
				if(j++ % (sampleDistribution.size() / 10) == 0)
					gspu.sysoStempPerformance(j * 1d / sampleDistribution.size(), this);
			}
		}
		return updatedSampleDistribution;
	}

}
