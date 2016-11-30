package gospl.algo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.util.GSPerformanceUtil;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.IHierarchicalSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;
import gospl.metamodel.GSSurveyType;


/**
 * Infer a complete distribution based on a n-dimension matrix (either partial or complete) and setup a sampler 
 * based on it. 
 * <p>
 * The algorithme make several assumptions:
 * <p><ul>
 * <li> All variables of two dimension with no relation in the {@link INDimensionalMatrix} are supposed to be independent
 * <li> When several dimensions refer to only one main dimension: Aggregated dimensions are disband following 
 * the same principle. For ex., the value "75 and more" is broke down into several equals category like 
 * "75-79", "80-84", "85-89", etc.
 * <li> When several dimensions refer to only one main dimension: unmentioned variable refer to as empty variable. For ex.,
 * people under the age of 15' are usually not consider in job category, so in this algorithm they will be attached with
 * variable 'empty' for dimension 'job'
 * </ul><p>
 * 
 * @author kevinchapuis
 *
 */
public class IndependantHypothesisAlgo implements IDistributionInferenceAlgo<IDistributionSampler> {

	private Logger logger = LogManager.getLogger();
	
	public IndependantHypothesisAlgo() {

	}

	@Override
	public ISampler<ACoordinate<ASurveyAttribute, AValue>> inferDistributionSampler(
			INDimensionalMatrix<ASurveyAttribute, AValue, Double> matrix,
			IDistributionSampler sampler) throws IllegalDistributionCreation {
		if(matrix == null || matrix.getMatrix().isEmpty())
			throw new IllegalArgumentException("matrix passed in parameter cannot be null or empty");
		if(!matrix.isSegmented() && matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
			throw new IllegalDistributionCreation("can't create a sampler using only one matrix of GosplMetaDataType#LocalFrequencyTable");

		// Begin the algorithm (and performance utility)
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute independant-hypothesis-joint-distribution from conditional distribution\nTheoretical size = "+
				matrix.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2), logger);
		gspu.getStempPerformance(0);

		// Stop the algorithm and exit the unique matrix if there is only one
		if(!matrix.isSegmented()){
			sampler.setDistribution((AFullNDimensionalMatrix<Double>) matrix);
			return sampler;
		}


		/////////////////////////////////////
		// 1st STEP: identify the various inner matrices
		/////////////////////////////////////

		// Cast matrix to access inner full matrices
		ASegmentedNDimensionalMatrix<Double> segmentedMatrix = (ASegmentedNDimensionalMatrix<Double>) matrix;

		// Init sample distribution simple expression
		Map<Set<AValue>, Double> sampleDistribution = new HashMap<>();

		// Init collection to store processed attributes & matrix
		Set<ASurveyAttribute> allocatedAttributes = new HashSet<>();
		Set<AFullNDimensionalMatrix<Double>> unallocatedMatrices = new HashSet<>(segmentedMatrix.getMatrices());

		/////////////////////////////////////
		// 2nd STEP: disaggregate attributes & start processing associated matrices
		/////////////////////////////////////

		// First identify referent & aggregated attributes
		Set<ASurveyAttribute> aggAtts = segmentedMatrix.getDimensions()
				.stream().filter(d -> !d.isRecordAttribute() && !d.getReferentAttribute().equals(d))
				.collect(Collectors.toSet());
		Set<ASurveyAttribute> refAtts = aggAtts.stream().map(att -> att.getReferentAttribute())
				.collect(Collectors.toSet());
		// Then disintegrated attribute -> to aggregated attributes relationships
		Map<ASurveyAttribute, Set<ASurveyAttribute>> aggAttributeMap = refAtts
				.stream().collect(Collectors.toMap(refDim -> refDim, refDim -> segmentedMatrix.getDimensions()
						.stream().filter(aggDim -> aggDim.getReferentAttribute().equals(refDim) 
								&& !aggDim.equals(refDim)).collect(Collectors.toSet())));
		
		// And disintegrated value -> to aggregated values relationships:
		Map<AValue, Set<AValue>> aggToRefValues = new HashMap<>();
		for(ASurveyAttribute asa : aggAtts){
			Map<AValue, Set<AValue>> tmpMap = new HashMap<>();
			Set<AValue> values = asa.getValues();
			for(AValue val : values)
				tmpMap.put(val, val.getAttribute().findMappedAttributeValues(val));
			Set<AValue> matchedValue = tmpMap.values().stream().flatMap(set -> set.stream()).collect(Collectors.toSet());
			Set<AValue> refValue = asa.getReferentAttribute().getValues();
			Set<AValue> unmatchedValue = refValue.stream().filter(val -> !matchedValue.contains(val)).collect(Collectors.toSet());
			tmpMap.put(asa.getEmptyValue(), unmatchedValue);
			aggToRefValues.putAll(tmpMap);
		}

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
			Map<Set<AValue>, Double> updatedSampleDistribution = new HashMap<>();
			Map<Set<AValue>, Double> untargetedIndiv = new HashMap<>(sampleDistribution);
			
			// Corresponding disintegrated control total of aggregated values
			double oControl = sampleDistribution.entrySet()
					.parallelStream().filter(indiv -> mat.getDimensions()
							.stream().filter(ad -> aggAtts.contains(ad)).map(ad -> ad.getValues()).allMatch(aVals -> aVals
									.stream().anyMatch(av -> aggToRefValues.get(av)
											.stream().anyMatch(dv -> indiv.getKey().contains(dv)))))
					.mapToDouble(indiv -> indiv.getValue()).sum();
			
			// Iterate over the old sampleDistribution to had new disaggregate values
			for(ACoordinate<ASurveyAttribute, AValue> aggCoord : mat.getMatrix().keySet()){
				
				// Identify all individual in the distribution that have disintegrated information about aggregated data
				Map<Set<AValue>, Double> targetedIndiv = sampleDistribution.entrySet()
						.stream().filter(indiv -> aggCoord.getDimensions()
								.stream().filter(d -> allocatedAttributes.contains(d)).map(d -> aggCoord.getMap().get(d))
								.allMatch(v -> indiv.getKey().contains(v))
								&& aggToRefValues.entrySet()
								.stream().filter(e -> aggCoord.contains(e.getKey())).map(e -> e.getValue())
								.allMatch(vals -> vals.stream().anyMatch(v -> indiv.getKey().contains(v))))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
								
				// Retain new values from aggregated coordinate
				Set<AValue> newVals = aggCoord.getMap().entrySet()
						.stream().filter(e -> !aggAtts.contains(e.getKey()) 
								&& !allocatedAttributes.contains(e.getKey()))
						.map(e -> e.getValue()).collect(Collectors.toSet());
				
				// Identify targeted probability: sum of proba for tageted disintegrated values and proba for aggregated values
				double mControl = targetedIndiv.values().stream().reduce(0d, (d1, d2) -> d1 + d2);
				double aControl = mat.getMatrix().get(aggCoord).getValue();
				for(Set<AValue> indiv : targetedIndiv.keySet()){
					updatedSampleDistribution.put(Stream.concat(indiv.stream(), newVals.stream()).collect(Collectors.toSet()), 
							targetedIndiv.get(indiv) / mControl * aControl * oControl);

					untargetedIndiv.remove(indiv);
				}
			}
			
			// Iterate over non updated individual to add new attribute empty value (no info in aggregated data)
			Set<AValue> newVals = mat.getDimensions()
					.stream().filter(d -> !allocatedAttributes.contains(d) && !aggAtts.contains(d))
					.map(d -> d.getEmptyValue()).collect(Collectors.toSet());
			for(Set<AValue> indiv : untargetedIndiv.keySet())
				updatedSampleDistribution.put(Stream.concat(indiv.stream(), newVals.stream()).collect(Collectors.toSet()), 
						sampleDistribution.get(indiv));
			
			// Update allocated attributes and sampleDistribution
			allocatedAttributes.addAll(mat.getDimensions()
					.stream().filter(a -> !aggAtts.contains(a)).collect(Collectors.toSet()));
			sampleDistribution = updatedSampleDistribution;
			unallocatedMatrices.remove(mat);
		}

		////////////////////////////////////
		// 3rd STEP: proceed the remaining matrices
		////////////////////////////////////

		for(AFullNDimensionalMatrix<Double> mat : unallocatedMatrices){
			gspu.sysoStempPerformance(0d, this);

			// If "hookAtt" is empty fill the proxy distribution with conditional probability of this joint distribution
			sampleDistribution = updateGosplProbaMap(sampleDistribution, mat, gspu);
			allocatedAttributes.addAll(matrix.getDimensions());

			gspu.sysoStempPerformance(1, this);
		}
	
		sampler.setDistribution(new GosplBasicDistribution(sampleDistribution));
		return sampler;
	}

	
	
	// ------------------------------ inner utility methods ------------------------------ //

	
	private Map<Set<AValue>, Double> updateGosplProbaMap(Map<Set<AValue>, Double> sampleDistribution, 
			AFullNDimensionalMatrix<Double> matrix, GSPerformanceUtil gspu){
		Map<Set<AValue>, Double> updatedSampleDistribution = new HashMap<>();
		if(sampleDistribution.isEmpty()){
			updatedSampleDistribution.putAll(matrix.getMatrix().entrySet()
					.parallelStream().collect(Collectors.toMap(e -> new HashSet<>(e.getKey().values()), e -> e.getValue().getValue())));
		} else {
			int j = 1;
			Set<ASurveyAttribute> allocatedAttributes = sampleDistribution.keySet()
					.parallelStream().flatMap(set -> set.stream()).map(a -> a.getAttribute()).collect(Collectors.toSet());
			Set<ASurveyAttribute> hookAtt = matrix.getDimensions()
					.stream().filter(att -> allocatedAttributes.contains(att)).collect(Collectors.toSet());
			for(Set<AValue> indiv : sampleDistribution.keySet()){
				Set<AValue> hookVal = indiv.stream().filter(val -> hookAtt.contains(val.getAttribute()))
						.collect(Collectors.toSet());
				Map<ACoordinate<ASurveyAttribute, AValue>, AControl<Double>> coordsHooked = matrix.getMatrix().entrySet()
						.parallelStream().filter(e -> e.getKey().containsAll(hookVal))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				double summedProba = coordsHooked.values()
						.stream().reduce(matrix.getNulVal(), (v1, v2) -> v1.add(v2)).getValue();
				for(ACoordinate<ASurveyAttribute, AValue> newIndivVal : coordsHooked.keySet()){
					Set<AValue> newIndiv = Stream.concat(indiv.stream(), newIndivVal.values().stream()).collect(Collectors.toSet());
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
