package gospl.algo.sr.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.GSUtilAttribute;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
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
				theoreticalSize, logger, Level.DEBUG);
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
		Collection<Map<DemographicAttribute<? extends IValue>, IValue>> coordinates = GSUtilAttribute.getValuesCombination(targetedDimensions);

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

		sampler.setDistribution(freqMatrix);
		return sampler;
	}

}
