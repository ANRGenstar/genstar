package gospl.distribution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gospl.algos.sampler.GosplBasicSampler;
import gospl.algos.sampler.ISampler;
import gospl.distribution.control.AControl;
import gospl.distribution.control.ControlFrequency;
import gospl.distribution.coordinate.ACoordinate;
import gospl.distribution.coordinate.GosplCoordinate;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.MatrixCoordinateException;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import io.data.GSDataParser;
import io.data.GSDataType;
import io.util.GSPerformanceUtil;

public class GosplConditionalDistribution extends ASegmentedNDimensionalMatrix<Double> {

	private static boolean DEBUG_SYSO = true;

	public GosplConditionalDistribution(Set<AFullNDimensionalMatrix<Double>> jointDistributionSet) throws IllegalDistributionCreation {
		super(jointDistributionSet);
	}

	/* ------------------ Setters ------------------ //

	@Override
	public boolean addValue(ACoordinate<IAttribute, IValue> coordinates, AControl<? extends Number> value) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinates.getDimensions())).collect(Collectors.toSet());
		return jds.iterator().next().addValue(coordinates, value);
	}

	@Override
	public boolean setValue(ACoordinate<IAttribute, IValue> coordinates, AControl<? extends Number> value) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinates.getDimensions())).collect(Collectors.toSet());
		if(jds.size() != 1)
			return false;
		return jds.iterator().next().setValue(coordinates, value);
	}

	@Override
	public boolean removeValue(ACoordinate<IAttribute, IValue> coordinate) {
		Set<AFullNDimensionalMatrix<Double>> jds = jointDistributionSet
				.stream().filter(jd -> jd.getDimensions().equals(coordinate.getDimensions())).collect(Collectors.toSet());
		if(jds.size() != 1)
			return false;
		return jds.iterator().next().removeValue(coordinate);
	}
	*/
	
// ------------------ Side contract ------------------ //  
	
	@Override
	public AControl<Double> getNulVal() {
		return new ControlFrequency(0d);
	}

	@Override
	public AControl<Double> getIdentityProductVal() {
		return new ControlFrequency(1d);
	}

// -------------------- Utilities -------------------- //

	@Override
	public boolean isCoordinateCompliant(ACoordinate<IAttribute, IValue> coordinate) {
		return jointDistributionSet.stream().anyMatch(jd -> jd.isCoordinateCompliant(coordinate));
	}

	@Override
	public AControl<Double> parseVal(GSDataParser parser, String val) {
		if(parser.getValueType(val).equals(GSDataType.String) || parser.getValueType(val).equals(GSDataType.Boolean))
			return getNulVal();
		return new ControlFrequency(Double.valueOf(val));
	}

// ------------------- Back office ------------------- //

	/*
	 * TODO: move this into a builder, i.e. matrix should be estimated, next sampling method choose and finally draw made
	 */
	/*
	private ISampler<ACoordinate<IAttribute, IValue>> createSampler() 
			throws IllegalDistributionCreation {
		int theoreticalSpaceSize = this.getDimensions().stream()
				.mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2);
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute independant-hypothesis-joint-distribution from conditional distribution\nTheoretical size = "+
				theoreticalSpaceSize, DEBUG_SYSO);
		Map<Set<IValue>, Double> sampleDistribution = new HashMap<>();
		// Store the attributes that have been allocated
		Set<IAttribute> allocatedAttribut = new HashSet<>();
		// Begin the algorithm
		gspu.getStempPerformance(0);
		for(AFullNDimensionalMatrix<Double> jd : jointDistributionSet.stream()
				.sorted((jd1, jd2) -> jd2.size() - jd1.size()).collect(Collectors.toList())){
			// Collect attribute in the schema for which a probability have already been calculated
			Set<IAttribute> hookAtt = jd.getDimensions()
					.stream().filter(att -> allocatedAttribut.contains(att)).collect(Collectors.toSet());

			gspu.sysoStempPerformance(0d, this);
			// If "hookAtt" is empty fill the proxy distribution with conditional probability of this joint distribution
			if(sampleDistribution.isEmpty()){
				sampleDistribution.putAll(jd.getMatrix().entrySet()
						.parallelStream().collect(Collectors.toMap(e -> new HashSet<>(e.getKey().values()), e -> e.getValue().getValue())));

				gspu.sysoStempPerformance(1d, this);
				System.out.println(sampleDistribution.keySet().parallelStream().mapToInt(c -> c.size()).average().getAsDouble()+" attributs ("+
						Arrays.toString(jd.getDimensions().stream().map(d -> d.getName()).toArray())+")");

			// Else, take into account known conditional probabilities in order to add new attributes
			} else {
				int j = 1;
				Map<Set<IValue>, Double> newSampleDistribution = new HashMap<>();
				for(Set<IValue> indiv : sampleDistribution.keySet()){
					Set<IValue> hookVal = indiv.stream().filter(val -> hookAtt.contains(val.getAttribute())).collect(Collectors.toSet());
					Map<ACoordinate<IAttribute, IValue>, AControl<Double>> coordsHooked = jd.getMatrix().entrySet()
							.parallelStream().filter(e -> e.getKey().containsAll(hookVal))
							.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
					double summedProba = coordsHooked.values().stream().reduce(getNulVal(), (v1, v2) -> v1.add(v2)).getValue();
					for(ACoordinate<IAttribute, IValue> newIndivVal : coordsHooked.keySet()){
						Set<IValue> newIndiv = new HashSet<>(indiv);
						newIndiv.addAll(newIndivVal.values());
						double newProba = sampleDistribution.get(indiv) * coordsHooked.get(newIndivVal).getValue() / summedProba;
						if(newProba > 0d)
							newSampleDistribution.put(newIndiv, newProba);
					}
					if(j++ % (sampleDistribution.size() / 10) == 0)
						gspu.sysoStempPerformance(j * 1d / sampleDistribution.size(), this);
				}
				System.out.println("-------------------------\nFrom "+sampleDistribution.keySet().parallelStream().mapToInt(c -> c.size()).average().getAsDouble()+
						"To "+newSampleDistribution.keySet().parallelStream().mapToInt(c -> c.size()).average().getAsDouble()+" attributs ("+
						Arrays.toString(jd.getDimensions().stream().map(d -> d.getName()).toArray())+")"
						+ "\n-------------------------");
				sampleDistribution = newSampleDistribution;
			}
			allocatedAttribut.addAll(jd.getDimensions());
			gspu.resetStempProp();
			gspu.sysoStempPerformance(1, this);
		}
		long wrongPr = sampleDistribution.keySet().parallelStream().filter(c -> c.size() != getDimensions().size()).count();
		double avrSize =  sampleDistribution.keySet().parallelStream().mapToInt(c -> c.size()).average().getAsDouble();
		gspu.resetStempProp();
		if(wrongPr != 0)
			throw new IllegalDistributionCreation("Some sample indiv ("+( Math.round(Math.round(wrongPr * 1d / sampleDistribution.size() * 100)))+"%) have not all attributs (average attributs nb = "+avrSize+")");
		Map<ACoordinate<IAttribute, IValue>, Double> sdMap = sampleDistribution.entrySet()
				.parallelStream().collect(Collectors.toMap(e -> safeCoordinateCreation(e.getKey(), gspu), e -> e.getValue()));
		return new GosplBasicSampler(new GosplJointDistribution(sdMap));
	}
	*/
	
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

}
