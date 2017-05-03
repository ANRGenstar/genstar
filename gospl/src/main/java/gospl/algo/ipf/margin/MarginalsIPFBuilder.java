package gospl.algo.ipf.margin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.GSPerformanceUtil;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;

public class MarginalsIPFBuilder<T extends Number> implements IMarginalsIPFBuilder<T> {

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
	public Collection<AMargin<T>> buildCompliantMarginals(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> control,
			AFullNDimensionalMatrix<T> seed,
			boolean parallel){
		
		// check that the dimensions are correct and inform the user of potential issues
		{
		StringBuffer sbErrors = new StringBuffer();
		for (APopulationAttribute dimControl: control.getDimensions()) {
			if (!seed.getDimensions().contains(dimControl) 
					&& !control.getDimensions().contains(dimControl.getReferentAttribute())
					&& !seed.getDimensions().stream().anyMatch(dimSeed -> dimSeed.getReferentAttribute().equals(dimControl))
					) {
				sbErrors.append("control does not contains seed dimension ").append(dimControl).append(" (you might add a referent attribute?);\n");
			} 
		}
		if (sbErrors.length() > 0) 
			throw new IllegalArgumentException(
					"Cannot build marginals for control and seed that does not match their attributes: "
					+sbErrors.toString()
					);
		}
		
		// ... no error, let's go ahead
		
		// now let's build the marginals
		logger.info("Estimates seed's referent marginals from control matrix {}", 
				control.getDimensions().stream().map(att -> att.getAttributeName()
						.substring(0, att.getAttributeName().length() < 5 ? att.getAttributeName().length() : 5))
				.collect(Collectors.joining(" x ")));
		
		Map<APopulationAttribute, APopulationAttribute> controlToSeedAttribute = new HashMap<>();
		for(APopulationAttribute sAttribute : seed.getDimensions()){
			List<APopulationAttribute> cAttList = control.getDimensions().stream()
					.filter(ca -> ca.isLinked(sAttribute))
					.collect(Collectors.toList());
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

		// let's create the marginals based on this information
		Collection<AMargin<T>> marginals = new ArrayList<>();

		List<APopulationAttribute> targetedAttributes = controlToSeedAttribute.keySet().stream()
				.filter(att -> att.getReferentAttribute().equals(att)).collect(Collectors.toList());
		GSPerformanceUtil gspu = new GSPerformanceUtil("Trying to build marginals for attribute set "
				+Arrays.toString(targetedAttributes.toArray()), logger, Level.TRACE);
		gspu.sysoStempPerformance(0, this);
		for(APopulationAttribute cAttribute : targetedAttributes){
			Collection<Set<APopulationValue>> cMarginalDescriptors = this.getMarginalDescriptors(cAttribute,
					controlToSeedAttribute.keySet().stream().filter(att -> !att.equals(cAttribute)).collect(Collectors.toSet()), 
					control);
			
			logger.debug("Attribute "+cAttribute.getAttributeName()+" marginal descriptors are composed of "
					+cMarginalDescriptors.size()+" set of values with "+cMarginalDescriptors.stream().flatMap(set -> set.stream())
					.collect(Collectors.toSet()).size()+" different values being used");
			
			AMargin<T> mrg = null;
			if(controlToSeedAttribute.get(cAttribute).equals(cAttribute)){
				SimpleMargin<T> margin = new SimpleMargin<>(cAttribute, controlToSeedAttribute.get(cAttribute));
				cMarginalDescriptors.parallelStream().forEach(md -> margin.addMargin(md, control.getVal(md)));
				mrg = margin;
				marginals.add(margin);
			} else {
				APopulationAttribute sAttribute = controlToSeedAttribute.get(cAttribute);
				ComplexMargin<T> margin = new ComplexMargin<>(cAttribute, sAttribute);
				cMarginalDescriptors.parallelStream().forEach(md -> margin.addMarginal(md, control.getVal(md), 
						this.tranposeMarginalDescriptor(md, control, seed)));
				mrg = margin;
				marginals.add(margin);
			}
			
			gspu.sysoStempPerformance(1, this);
			double totalMRG = mrg.marginalControl.values().stream().mapToDouble(c -> c.getValue().doubleValue()).sum();
			logger.info("Created marginals (size = {}): cd = {} | sd = {} | sum_of_c = {}", mrg.size() == 0 ? "empty" : mrg.size(),
					mrg.getControlDimension(), mrg.getSeedDimension(), totalMRG); 
			
			if(mrg.size() != 0 && Math.abs(totalMRG - 1d) > 0.01){
				// oops, the margins are higher than 1.
				// let's reweight (???? is it even relevant here ???)
				for (AControl<T> c: mrg.marginalControl.values()) {
					AControl<Double> cD = (AControl<Double>)c; // anyway I don't see how marginals would be integer... 
					cD.multiply(1./totalMRG);
				}
				// recompute again
				totalMRG = mrg.marginalControl.values().stream().mapToDouble(c -> c.getValue().doubleValue()).sum();
				logger.info("Created marginals (size = {}): cd = {} | sd = {} | sum_of_c = {}", mrg.size() == 0 ? "empty" : mrg.size(),
						mrg.getControlDimension(), mrg.getSeedDimension(), totalMRG); 
				
			}
			
			if(mrg.size() != 0 && Math.abs(totalMRG - 1d) > 0.01){
				String msg = "Detailed marginals: "+mrg.getClass().getCanonicalName()+" \n "+mrg.marginalControl.entrySet()
						.stream().map(entry -> Arrays.toString(entry.getKey().toArray())
								+" = "+entry.getValue()).collect(Collectors.joining("\n"));
				logger.error(msg);
				throw new RuntimeException("wrong marginals total "+totalMRG+" ("+seed.getGenesisAsList()+")"+": "+msg);
			}
		}

		return marginals;
	}


	/**
	 * Return the transposed marginal descriptor coordinate from control to seed matrix
	 * 
	 * @param cMarginalDescriptor
	 * @param seed 
	 * @param control 
	 * @param control
	 * @param seed
	 * @return
	 */
	private Set<APopulationValue> tranposeMarginalDescriptor(Set<APopulationValue> cMarginalDescriptor,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> control, AFullNDimensionalMatrix<T> seed) {

		Set<APopulationValue> smd = new HashSet<>();
		Set<APopulationAttribute> refSAtt = seed.getDimensions().stream()
				.filter(att -> !att.isRecordAttribute() && !att.getReferentAttribute().equals(att))
				.map(att -> att.getReferentAttribute()).collect(Collectors.toSet());
		for(APopulationValue cv : cMarginalDescriptor){
			Collection<APopulationValue> mappedSD = new HashSet<>();
			APopulationAttribute refCAtt = cv.getAttribute().getReferentAttribute();
			if(refCAtt.equals(cv.getAttribute())){
				// Attribute's value is the same in control and seed
				if(seed.getDimensions().contains(cv.getAttribute())){
					mappedSD.add(cv);
				} 
				// Attribute's value is a referent attribute of seed one
				if(refSAtt.contains(cv.getAttribute())){
					APopulationAttribute seedAttribute = seed.getDimensions().stream().filter(att -> 
					att.getReferentAttribute().equals(cv.getAttribute())).findAny().get();
					// WARNING: this method can returns same values with another control value @code{cv}
					mappedSD.addAll(seedAttribute.findMappedAttributeValues(cv));
				}
			} else {
				// Seed has a referent attribute for this attribute's value
				if(seed.getDimensions().contains(refCAtt)){
					Collection<APopulationValue> sv = cv.getAttribute().findMappedAttributeValues(cv);
					if(!seed.getAspects().containsAll(sv))
						throw new RuntimeException("matrix "+seed.getLabel()+" should contain values "
								+Arrays.toString(sv.toArray())+" but does not");
					mappedSD.addAll(sv);
				}
				// seed and control attribute have a common referent attribute
				if(refSAtt.contains(refCAtt)){
					Collection<APopulationValue> sv = new HashSet<>();
					APopulationAttribute sa = seed.getDimensions().stream()
							.filter(att -> att.getReferentAttribute().equals(refCAtt))
							.findAny().get();
					for(APopulationValue refValue : cv.getAttribute().findMappedAttributeValues(cv))
						sv.addAll(sa.findMappedAttributeValues(refValue));
					if(!seed.getAspects().containsAll(sv))
						throw new RuntimeException("Trying to match value "+cv+" with one or more value of attribute "
								+sa.getAttributeName()+" ("+Arrays.toString(sv.toArray())+") through common referent attribute "
								+refCAtt.getAttributeName());
					mappedSD.addAll(sv);

				}
			}
			smd.addAll(mappedSD);
		}
		return smd;
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
		Set<Set<APopulationValue>> outputMarginalDescriptors =  marginalDescriptors.stream()
				.flatMap(set -> control.getCoordinates(set).stream()
						.filter(coord -> coord.getDimensions().contains(targetedAttribute))
						.map(coord -> coord.values().stream().filter(val -> !val.getAttribute().equals(targetedAttribute))
								.collect(Collectors.toSet()))).collect(Collectors.toSet());
		// Exclude overlapping marginal descriptors in segmented matrix: e.g. md1 = {age} & md2 = {age, gender}
		final Set<Set<APopulationAttribute>> mdAttributes = outputMarginalDescriptors.stream()
				.map(set -> set.stream().map(a -> a.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		Set<Set<APopulationAttribute>> mdArchitype = mdAttributes.stream().filter(archi -> mdAttributes.stream()
				.noneMatch(mdAtt -> mdAtt.containsAll(archi) && mdAtt.size() > archi.size()))
			.collect(Collectors.toSet());
		return outputMarginalDescriptors.stream().filter(set -> mdArchitype
				.stream().anyMatch(architype -> architype.stream().allMatch(att -> set.stream().anyMatch(val -> att.getValues().contains(val)))))
			.collect(Collectors.toList());
	}
}
