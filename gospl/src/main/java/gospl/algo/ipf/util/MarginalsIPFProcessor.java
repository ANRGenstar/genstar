package gospl.algo.ipf.util;

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

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;

public class MarginalsIPFProcessor<T extends Number> implements IMarginalsIPFProcessor<T> {

	private Logger logger = LogManager.getLogger();

	/**
	 * All marginals are calculated using {@link INDimensionalMatrix#getVal(Collection values)}, 
	 * hence comprising different knowledge depending on matrix concrete type: 
	 * <p>
	 * <ul>
	 * <li> {@link AFullNDimensionalMatrix}: gives the real margin
	 * <li> {@link ASegmentedNDimensionalMatrix}: gives accurate margin based on potentially
	 * limited information
	 * </ul>
	 * <p>
	 * After all controls have been retrieve, then we must transpose coordinate into seed compliant
	 * value - attribute requirement through {@link APopulationAttribute#getReferentAttribute()}
	 * <p>
	 * WARNING: let the user define if this method should use {@link Stream#parallel()} capabilities or not
	 * 
	 * @param parallel
	 * @return
	 */
	@Override
	public Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> buildCompliantMarginals(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> control,
			AFullNDimensionalMatrix<T> seed,
			boolean parallel){
		if(!seed.getDimensions().stream().allMatch(dim -> control.getDimensions().contains(dim) 
				|| control.getDimensions().contains(dim.getReferentAttribute())
				|| control.getDimensions().stream().anyMatch(cDim -> cDim.getReferentAttribute().equals(dim))))
			throw new IllegalArgumentException("Cannot build marginals for control and seed that does not match their attributes:\n"
					+ "Seed: "+Arrays.toString(seed.getDimensions().toArray())+"\n"
					+ "Control: "+Arrays.toString(control.getDimensions().toArray()));

		logger.info("Estimates the seed' referent marginals from a control matrix");
		Map<APopulationAttribute, APopulationAttribute> controlToSeedAttribute = new HashMap<>();
		for(APopulationAttribute sAttribute : seed.getDimensions()){
			List<APopulationAttribute> cAttList = control.getDimensions().stream().filter(ca -> sAttribute.equals(ca) 
					|| sAttribute.getReferentAttribute().equals(ca) || ca.getReferentAttribute().equals(sAttribute)
					|| sAttribute.getReferentAttribute().equals(ca.getReferentAttribute())).
					collect(Collectors.toList());
			if(!cAttList.isEmpty())
				for(APopulationAttribute cAtt : cAttList)
					controlToSeedAttribute.put(cAtt, sAttribute);
		}
		logger.debug("Matching attributes are: {}", controlToSeedAttribute.entrySet()
				.stream().map(entry -> "["+entry.getKey().getAttributeName()+" - "
						+entry.getValue().getAttributeName()+"]").collect(Collectors.joining(" ")));
		logger.debug("Unmatched attributes are: {}", Arrays.toString(seed.getDimensions()
				.stream().filter(dim -> !controlToSeedAttribute.values().contains(dim)).toArray()));

		if(controlToSeedAttribute.isEmpty())
			throw new IllegalArgumentException("Seed attributes do not match any attributes in control distribution");

		Map<APopulationAttribute, Map<Set<APopulationValue>, Set<APopulationValue>>> controlToSeedMarginalsDescriptors = new HashMap<>();
		for(APopulationAttribute cAttribute : controlToSeedAttribute.keySet().stream()
				.filter(att -> att.getReferentAttribute().equals(att)).collect(Collectors.toList())){
			Collection<Set<APopulationValue>> cMarginalDescriptors = this.getMarginalDescriptors(cAttribute,
					controlToSeedAttribute.keySet().stream().filter(att -> !att.equals(cAttribute)).collect(Collectors.toSet()), 
					control);
			if(!controlToSeedAttribute.get(cAttribute).equals(cAttribute)){
				APopulationAttribute sAttribute = controlToSeedAttribute.get(cAttribute);
				logger.debug("{} => {}", cAttribute.getAttributeName(), sAttribute.getAttributeName());
				Map<Set<APopulationValue>, Set<APopulationValue>> scmd = new HashMap<>();
				for(Set<APopulationValue> cmd : cMarginalDescriptors){
					Set<APopulationValue> smd = new HashSet<>();
					for(APopulationValue cv : cmd){
						Collection<APopulationValue> mappedSD = null;
						if(!controlToSeedAttribute.keySet().contains(cv.getAttribute())){
							Collection<APopulationValue> ccv = cv.getAttribute().getReferentAttribute()
									.findMappedAttributeValues(cv);
							mappedSD = ccv.stream().flatMap(val -> controlToSeedAttribute.get(val.getAttribute())
									.findMappedAttributeValues(val).stream()).collect(Collectors.toList());
							logger.trace("{} => {} => {}", cv.getInputStringValue(), Arrays.toString(ccv.toArray()), 
									Arrays.toString(mappedSD.toArray()));
						} else {
							mappedSD = controlToSeedAttribute.get(cv.getAttribute())
									.findMappedAttributeValues(cv);
							logger.trace("{} => {}", cv.getInputStringValue(), Arrays.toString(mappedSD.toArray()));
						}
						smd.addAll(mappedSD);
					}
					scmd.put(cmd, smd);
					logger.trace("There is {} Original marginals descriptors and {} seed translated",
							cMarginalDescriptors.size(), scmd.size());
				}
				controlToSeedMarginalsDescriptors.put(sAttribute, scmd);
			} else {
				logger.debug("{} == {}",cAttribute.getAttributeName(), 
						controlToSeedAttribute.get(cAttribute).getAttributeName());
				controlToSeedMarginalsDescriptors.put(controlToSeedAttribute.get(cAttribute), cMarginalDescriptors.stream()
						.collect(Collectors.toMap(Function.identity(), Function.identity())));
			}
			Map<Set<APopulationValue>, Set<APopulationValue>> resultMap = controlToSeedMarginalsDescriptors
					.get(controlToSeedAttribute.get(cAttribute));
			logger.info("{} is granted {} marginals control for attributes: {}", controlToSeedAttribute.get(cAttribute).getAttributeName(),
					resultMap.size(),resultMap.isEmpty() ? "not any other attribute" : 
							Arrays.toString(resultMap.keySet().stream().flatMap(set -> set.stream())
							.map(val -> val.getAttribute().getAttributeName()+" => "
									+controlToSeedAttribute.get(val.getAttribute()).getAttributeName())
							.collect(Collectors.toSet()).toArray()));
			if(resultMap.isEmpty())
				logger.debug("EMPTY MAP");
			if(resultMap.keySet().stream().anyMatch(set -> set.isEmpty()))
					logger.debug("EMPTY KEY - map contains: "+resultMap.entrySet()
						.stream().map(entry -> entry.toString()).collect(Collectors.joining("\n")));
			if(resultMap.values().stream().anyMatch(set -> set.isEmpty()))
				logger.debug("EMPTY VALUE - map contains: "+resultMap.entrySet()
					.stream().map(entry -> entry.toString()).collect(Collectors.joining("\n")));
		}

		// TODO: treat zero cell at seed level !
		Collection<Set<APopulationValue>> zeroCellSeedCoordinate = controlToSeedMarginalsDescriptors.values()
				.stream().flatMap(map -> map.entrySet().stream().filter(entry -> entry.getValue().isEmpty()))
				.map(entry -> entry.getKey()).collect(Collectors.toSet());
		logger.info("Zero cell seed count: {}{}", 
				zeroCellSeedCoordinate.size(),
				zeroCellSeedCoordinate.isEmpty() ? "" : "\nCoordinate with no seed marginals are:\n"+
				zeroCellSeedCoordinate.stream().map(coord -> Arrays.toString(coord.toArray()))
				.collect(Collectors.joining("\n"))
				);
		
		// TODO: treat zero cell at control level !
		Collection<Set<APopulationValue>> zeroCellControlCoordinate = controlToSeedMarginalsDescriptors.values()
				.stream().flatMap(map -> map.entrySet().stream().filter(entry -> entry.getKey().isEmpty()))
				.map(entry -> entry.getValue()).collect(Collectors.toList());
		logger.info("Zero cell control count: {}{}", 
				zeroCellControlCoordinate.size(),
				zeroCellControlCoordinate.isEmpty() ? "" : "\nCoordinate with no control marginals are:\n"+
				zeroCellControlCoordinate.stream().map(coord -> Arrays.toString(coord.toArray()))
				.collect(Collectors.joining("\n"))
				);
		
		logger.debug("Marginals seed descriptors attributes are {}", 
				Arrays.toString(controlToSeedMarginalsDescriptors.keySet().toArray()));
		logger.info("Start to bind seed compliant marginals with control marginals value");
		
		/*
		Stream<APopulationAttribute> smdStream = parallel ? 
				controlToSeedMarginalsDescriptors.keySet().parallelStream() :
			controlToSeedMarginalsDescriptors.keySet().stream();
			*/
			
		/* HINT: Inspection purpose code to replace lambda blurry code */
		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> controlMargin = new HashMap<>();
		for(APopulationAttribute att : controlToSeedMarginalsDescriptors.keySet()){
			Map<Set<APopulationValue>, AControl<T>> map = new HashMap<>();
			for(Set<APopulationValue> valSet : controlToSeedMarginalsDescriptors.get(att).keySet()){
				Set<APopulationValue> mirroredSet = controlToSeedMarginalsDescriptors.get(att)
						.get(valSet);
				AControl<T> cValue = null;
				try {
					cValue = control.getVal(valSet);
				} catch (NullPointerException e) {
					logger.error("Elicit a null pointer exception trying to retrieve control value: {}",
							"att = "+att.getAttributeName()+" | cv = "+Arrays.toString(valSet.toArray())
							+" | sv = "+Arrays.toString(mirroredSet.toArray()));
					System.exit(1);
				}
				map.put(mirroredSet, cValue);
			}
			controlMargin.put(att, map);
		}
		
		/*
		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> controlMargin =
				smdStream.collect(Collectors.toMap(Function.identity(),
						att -> controlToSeedMarginalsDescriptors.get(att).entrySet()
							.stream().collect(Collectors.toMap(entry -> entry.getValue(), 
									entry -> control.getVal(entry.getKey())))));*/
		
		logger.info("Estimated marginals description: \n{}",
				controlMargin.entrySet().stream().map(e -> e.getKey().getAttributeName()
						+" "+e.getValue().size()+" marginals with "+e.getValue().values().stream()
						.mapToDouble(c -> c.getValue().doubleValue()).sum()
						+" total contingency or frequency").collect(Collectors.joining("\n")));
		return controlMargin;
	}


	/**
	 * Return the marginal descriptors coordinate: a collection of all attribute's value combination (a set of value).
	 * This set of value should be compliant with provided n-dimensional matrix
	 * 
	 * @param referent
	 * @return
	 */
	private Collection<Set<APopulationValue>> getMarginalDescriptors(APopulationAttribute targetedAttribute,
			Collection<APopulationAttribute> sideAttributes,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> control){
		if(!control.getDimensions().containsAll(Stream.concat(Stream.of(targetedAttribute), sideAttributes.stream())
				.collect(Collectors.toSet())))
			throw new IllegalArgumentException("Targeted attributes must be compliant with n-dimensional matrix passed as parameter");
		// Setup output
		Collection<Set<APopulationValue>> marginalDescriptors = new ArrayList<>();
		// Init. the output collection with any attribute
		APopulationAttribute firstAtt = sideAttributes.iterator().next();
		for(APopulationValue value : firstAtt.getValues())
			marginalDescriptors.add(Stream.of(value).collect(Collectors.toSet()));
		sideAttributes.remove(firstAtt);
		// Then iterate over all other attributes
		for(APopulationAttribute att : sideAttributes){
			List<Set<APopulationValue>> tmpDescriptors = new ArrayList<>();
			for(Set<APopulationValue> descriptors : marginalDescriptors){
				tmpDescriptors.addAll(att.getValues().stream()
						.map(val -> Stream.concat(descriptors.stream(), Stream.of(val)).collect(Collectors.toSet()))
						.collect(Collectors.toList())); 
			}
			marginalDescriptors = tmpDescriptors;
		}
		// Translate into control compliant coordinate set of value
		return marginalDescriptors.stream()
				.flatMap(set -> control.getCoordinates(set).stream()
						.filter(coord -> coord.getDimensions().contains(targetedAttribute))
						.map(coord -> coord.values().stream().filter(val -> !val.getAttribute().equals(targetedAttribute))
								.collect(Collectors.toSet()))).collect(Collectors.toList());
	}
}
