package gospl.algo.sr.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;


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
 * According to these hypothesis, we refer to this algorithm as IS for Independent Sampler algorithm
 * 
 * @author kevinchapuis
 *
 */
public class IndependantHypothesisAlgo implements ISyntheticReconstructionAlgo<IDistributionSampler> {

	private Logger logger = LogManager.getLogger();

	@SuppressWarnings("serial")
	@Override
	public ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> inferSRSampler(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> matrix,
			IDistributionSampler sampler) throws IllegalDistributionCreation {
		if(matrix == null || matrix.getMatrix().isEmpty())
			throw new IllegalArgumentException("matrix passed in parameter cannot be null or empty");
		if(!matrix.isSegmented() && matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
			throw new IllegalDistributionCreation("can't create a sampler using only one matrix of GosplMetaDataType#LocalFrequencyTable");

		// Begin the algorithm (and performance utility)
		int theoreticalSize = matrix.getDimensions().stream().mapToInt(d -> d.getValueSpace().getValues().size()).reduce(1, (i1, i2) -> i1 * i2);
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute independant-hypothesis-joint-distribution from conditional distribution\nTheoretical size = "+
				theoreticalSize, logger);
		gspu.setObjectif(theoreticalSize);
		gspu.sysoStempPerformance(0, this);

		// Stop the algorithm and exit the unique matrix if there is only one
		if(!matrix.isSegmented()){
			sampler.setDistribution(GosplNDimensionalMatrixFactory.getFactory()
					.createDistribution(matrix.getMatrix()));
			return sampler;
		}

		// Reject attribute with referent, to only account for referent attribute
		Set<DemographicAttribute<? extends IValue>> targetedDimensions = matrix.getDimensions()
				.stream().filter(att -> att.getReferentAttribute().equals(att))
				.collect(Collectors.toSet());

		// Setup the matrix to estimate 
		AFullNDimensionalMatrix<Double> freqMatrix = new GosplNDimensionalMatrixFactory()
				.createEmptyDistribution(targetedDimensions);

		gspu.sysoStempMessage("Creation of matrix with attributes: "+Arrays.toString(targetedDimensions.toArray()));

		// Extrapolate the whole set of coordinates
		Collection<Map<DemographicAttribute<? extends IValue>, IValue>> coordinates = new ArrayList<>();
		Set<Set<IValue>> tmpCoord = new HashSet<>();
		for(DemographicAttribute<? extends IValue> attribute : targetedDimensions){
			if(tmpCoord.isEmpty()) {
				for(IValue val : attribute.getValueSpace().getValues()) {
					tmpCoord.add(new HashSet<>(Arrays.asList(val)));
				}
			} else {
				Set<Set<IValue>> newTmpCoord = new HashSet<>();
				for(Set<IValue> prevSet : tmpCoord) {
					attribute.getValueSpace().getValues().stream().forEach(val -> newTmpCoord.add(
							Stream.concat(prevSet.stream(), Stream.of(val)).collect(Collectors.toSet())));
				}
				tmpCoord = newTmpCoord;
			}
		}
		for(Set<IValue> coord : tmpCoord) {
			Map<DemographicAttribute<? extends IValue>, IValue> newCoord = new HashMap<>();
			coord.forEach(val -> newCoord.put(matrix.getDimension(val), val));
			coordinates.add(newCoord);
		}
		/*
		for(DemographicAttribute<? extends IValue> attribute : targetedDimensions){
			if(coordinates.isEmpty())
				attribute.getValueSpace().getValues()
					.forEach(val -> coordinates.add(new HashMap<DemographicAttribute<? extends IValue>, IValue>() 
						{{ put(attribute, val); }} ));
			else
				coordinates.stream().forEach(map -> attribute.getValueSpace().getValues()
						.forEach(val -> map.put(attribute, val)));
		}
		*/

		gspu.sysoStempPerformance(1, this);
		gspu.sysoStempMessage("Start writting down collpased distribution of size "+coordinates.size());

		for(Map<DemographicAttribute<? extends IValue>, IValue> coordinate : coordinates){
			AControl<Double> nulVal = freqMatrix.getNulVal();
			ACoordinate<DemographicAttribute<? extends IValue>, IValue> coord = new GosplCoordinate(coordinate);
			AControl<Double> freq = matrix.getVal(coord);
			if(!nulVal.getValue().equals(freq.getValue()))
				freqMatrix.addValue(coord, freq);
			else {
				// HINT: MUST INTEGRATE COORDINATE WITH EMPTY VALUE, e.g. age under 5 & empty occupation
				gspu.sysoStempMessage("Goes into a referent empty correlate: "
						+Arrays.toString(coordinate.values().toArray()));
				ACoordinate<DemographicAttribute<? extends IValue>, IValue	> newCoord = new GosplCoordinate(
						coord.getDimensions().stream().collect(Collectors.toMap(Function.identity(), 
						att -> matrix.getEmptyReferentCorrelate(coord).stream()
									.anyMatch(val -> val.getValueSpace().getAttribute().equals(att)) ?
								att.getValueSpace().getEmptyValue() : coord.getMap().get(att))));
				if(newCoord.equals(coord))
					freqMatrix.addValue(coord, freq);
				else
					freqMatrix.addValue(newCoord, matrix.getVal(newCoord.values()
							.stream().filter(value -> !matrix.getDimension(value).getEmptyValue().equals(value))
							.collect(Collectors.toSet())));
			}
		}
		
		gspu.sysoStempMessage("Distribution has been estimated");
		gspu.sysoStempPerformance(2, this);
		
		// WARNING: cannot justify this normalization, hence find another way to fit 1 sum of probability
		//freqMatrix.normalize();
		/*
		gspu.sysoStempMessage("Collapse matrix have been normalize:\n"
				+ freqMatrix.getDimensions().stream()
					.map(dim -> dim.toString()+" = "+freqMatrix.getVal(dim.getValues()).getValue())
					.reduce("", (s1,s2) -> s1+"\n"+s2));
					*/
		sampler.setDistribution(freqMatrix);
		return sampler;
	}


	public ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> inferSRSamplerWithReferentProcess(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> matrix,
			IDistributionSampler sampler) throws IllegalDistributionCreation {
		if(matrix == null || matrix.getMatrix().isEmpty())
			throw new IllegalArgumentException("matrix passed in parameter cannot be null or empty");
		if(!matrix.isSegmented() && matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
			throw new IllegalDistributionCreation("can't create a sampler using only one matrix of GosplMetaDataType#LocalFrequencyTable");

		// Begin the algorithm (and performance utility)
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute independant-hypothesis-joint-distribution from conditional distribution\nTheoretical size = "+
				matrix.getDimensions().stream().mapToInt(d -> d.getValueSpace().getValues().size()).reduce(1, (i1, i2) -> i1 * i2), logger);
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
		Map<Set<IValue>, Double> sampleDistribution = new HashMap<>();

		// Init collection to store processed attributes & matrix
		Set<DemographicAttribute<? extends IValue>> allocatedAttributes = new HashSet<>();
		Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double>> unallocatedMatrices = new HashSet<>(segmentedMatrix.getMatrices());

		/////////////////////////////////////
		// 2nd STEP: disaggregate attributes & start processing associated matrices
		/////////////////////////////////////

		// TODO: move this step to the getValue method in segmented matrix
		// More elegant and generic

		// First identify referent & aggregated attributes
		Set<DemographicAttribute<? extends IValue>> aggAtts = segmentedMatrix.getDimensions()
				.stream().filter(d -> !d.getReferentAttribute().equals(d))
				.collect(Collectors.toSet());
		Set<DemographicAttribute<? extends IValue>> refAtts = aggAtts.stream().map(att -> att.getReferentAttribute())
				.collect(Collectors.toSet());
		// Then disintegrated attribute -> to aggregated attributes relationships
		Map<DemographicAttribute<? extends IValue>, Set<DemographicAttribute<? extends IValue>>> aggAttributeMap = refAtts
				.stream().collect(Collectors.toMap(refDim -> refDim, refDim -> segmentedMatrix.getDimensions()
						.stream().filter(aggDim -> aggDim.getReferentAttribute().equals(refDim) 
								&& !aggDim.equals(refDim)).collect(Collectors.toSet())));

		// And disintegrated value -> to aggregated values relationships:
		Map<IValue, Collection<? extends IValue>> aggToRefValues = new HashMap<>();
		for(DemographicAttribute<? extends IValue> asa : aggAtts){
			Map<IValue, Collection<? extends IValue>> tmpMap = new HashMap<>();
			IValueSpace<? extends IValue> values = asa.getValueSpace();
			for(IValue val : values.getValues())
				tmpMap.put(val, segmentedMatrix.getDimension(val).findMappedAttributeValues(val));
			Set<IValue> matchedValue = tmpMap.values().stream().flatMap(set -> set.stream()).collect(Collectors.toSet());
			IValueSpace<? extends IValue> refValue = asa.getReferentAttribute().getValueSpace();
			Set<IValue> unmatchedValue = refValue.getValues().stream().filter(val -> !matchedValue.contains(val)).collect(Collectors.toSet());
			tmpMap.put(asa.getEmptyValue(), unmatchedValue);
			aggToRefValues.putAll(tmpMap);
		}

		// Iterate over matrices that have referent attributes and no aggregated attributes
		List<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double>> refMatrices = segmentedMatrix.getMatrices().stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(dim -> aggAttributeMap.keySet().contains(dim) 
						&& !aggAttributeMap.values().stream().flatMap(set -> set.stream()).anyMatch(aggDim -> dim.equals(aggDim))))
				.sorted((mat1, mat2) -> (int) mat2.getDimensions().stream().filter(dim2 -> aggAttributeMap.keySet().contains(dim2)).count()
						- (int) mat1.getDimensions().stream().filter(dim1 -> aggAttributeMap.keySet().contains(dim1)).count())
				.collect(Collectors.toList());
		for(INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> mat : refMatrices){
			sampleDistribution = updateGosplProbaMap(sampleDistribution, mat, gspu);
			unallocatedMatrices.remove(mat);
			allocatedAttributes.addAll(mat.getDimensions());
		}

		// Iterate over matrices that have aggregated attributes
		List<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double>> aggMatrices = unallocatedMatrices.stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(dim -> aggAttributeMap.values()
						.stream().flatMap(set -> set.stream()).anyMatch(aggDim -> dim.equals(aggDim))))
				.sorted((mat1, mat2) -> (int) mat2.getDimensions().stream().filter(dim2 -> aggAttributeMap.keySet().contains(dim2)).count()
						- (int) mat1.getDimensions().stream().filter(dim1 -> aggAttributeMap.keySet().contains(dim1)).count())
				.collect(Collectors.toList());

		for(INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> mat : aggMatrices){
			Map<Set<IValue>, Double> updatedSampleDistribution = new HashMap<>();
			Map<Set<IValue>, Double> untargetedIndiv = new HashMap<>(sampleDistribution);

			// Corresponding disintegrated control total of aggregated values
			double oControl = sampleDistribution.entrySet()
					.parallelStream().filter(indiv -> mat.getDimensions()
							.stream().filter(ad -> aggAtts.contains(ad)).map(ad -> ad.getValueSpace()).allMatch(aVals -> aVals.getValues()
									.stream().anyMatch(av -> aggToRefValues.get(av)
											.stream().anyMatch(dv -> indiv.getKey().contains(dv)))))
					.mapToDouble(indiv -> indiv.getValue()).sum();

			// Iterate over the old sampleDistribution to had new disaggregate values
			for(ACoordinate<DemographicAttribute<? extends IValue>, IValue> aggCoord : mat.getMatrix().keySet()){

				// Identify all individual in the distribution that have disintegrated information about aggregated data
				Map<Set<IValue>, Double> targetedIndiv = sampleDistribution.entrySet()
						.stream().filter(indiv -> aggCoord.getDimensions()
								.stream().filter(d -> allocatedAttributes.contains(d)).map(d -> aggCoord.getMap().get(d))
								.allMatch(v -> indiv.getKey().contains(v))
								&& aggToRefValues.entrySet()
								.stream().filter(e -> aggCoord.contains(e.getKey())).map(e -> e.getValue())
								.allMatch(vals -> vals.stream().anyMatch(v -> indiv.getKey().contains(v))))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

				// Retain new values from aggregated coordinate
				Set<IValue> newVals = aggCoord.getMap().entrySet()
						.stream().filter(e -> !aggAtts.contains(e.getKey()) 
								&& !allocatedAttributes.contains(e.getKey()))
						.map(e -> e.getValue()).collect(Collectors.toSet());

				// Identify targeted probability: sum of proba for tageted disintegrated values and proba for aggregated values
				double mControl = targetedIndiv.values().stream().reduce(0d, (d1, d2) -> d1 + d2);
				double aControl = mat.getMatrix().get(aggCoord).getValue();
				for(Set<IValue> indiv : targetedIndiv.keySet()){
					updatedSampleDistribution.put(Stream.concat(indiv.stream(), newVals.stream()).collect(Collectors.toSet()), 
							targetedIndiv.get(indiv) / mControl * aControl * oControl);

					untargetedIndiv.remove(indiv);
				}
			}

			// Iterate over non updated individual to add new attribute empty value (no info in aggregated data)
			Set<IValue> newVals = mat.getDimensions()
					.stream().filter(d -> !allocatedAttributes.contains(d) && !aggAtts.contains(d))
					.map(d -> d.getEmptyValue()).collect(Collectors.toSet());
			for(Set<IValue> indiv : untargetedIndiv.keySet())
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

		for(INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> mat : unallocatedMatrices){
			gspu.sysoStempPerformance(0d, this);

			// If "hookAtt" is empty fill the proxy distribution with conditional probability of this joint distribution
			sampleDistribution = updateGosplProbaMap(sampleDistribution, mat, gspu);
			allocatedAttributes.addAll(matrix.getDimensions());

			gspu.sysoStempPerformance(1, this);
		}

		sampler.setDistribution(new GosplNDimensionalMatrixFactory().createDistribution(matrix.getDimensions(), sampleDistribution));
		return sampler;
	}


	// ------------------------------ inner utility methods ------------------------------ //


	private Map<Set<IValue>, Double> updateGosplProbaMap(Map<Set<IValue>, Double> sampleDistribution, 
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> matrix, GSPerformanceUtil gspu){
		Map<Set<IValue>, Double> updatedSampleDistribution = new HashMap<>();
		if(sampleDistribution.isEmpty()){
			updatedSampleDistribution.putAll(matrix.getMatrix().entrySet()
					.parallelStream().collect(Collectors.toMap(e -> new HashSet<>(e.getKey().values()), e -> e.getValue().getValue())));
		} else {
			int j = 1;
			Set<DemographicAttribute<? extends IValue>> allocatedAttributes = sampleDistribution.keySet()
					.parallelStream().flatMap(set -> set.stream()).map(a -> matrix.getDimension(a)).collect(Collectors.toSet());
			Set<DemographicAttribute<? extends IValue>> hookAtt = matrix.getDimensions()
					.stream().filter(att -> allocatedAttributes.contains(att)).collect(Collectors.toSet());
			for(Set<IValue> indiv : sampleDistribution.keySet()){
				Set<IValue> hookVal = indiv.stream().filter(val -> hookAtt.contains(val.getValueSpace().getAttribute()))
						.collect(Collectors.toSet());
				Map<ACoordinate<DemographicAttribute<? extends IValue>, IValue>, AControl<Double>> coordsHooked = matrix.getMatrix().entrySet()
						.parallelStream().filter(e -> e.getKey().containsAll(hookVal))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				double summedProba = coordsHooked.values()
						.stream().reduce(matrix.getNulVal(), (v1, v2) -> v1.add(v2)).getValue();
				for(ACoordinate<DemographicAttribute<? extends IValue>, IValue> newIndivVal : coordsHooked.keySet()){
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
