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

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

/**
 * Make it possible to determine the signature of margin (how many dimensions, attributes, and theoretical margins)
 * as well as the control T value associated
 * 
 * @see Margin
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
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
	 * value - attribute requirement through {@link DemographicAttribute#getReferentAttribute()}
	 * <p>
	 * WARNING: let the user define if this method should use {@link Stream#parallel()} capabilities or not
	 * 
	 * @param parallel
	 * @return
	 */
	@Override
	public Collection<Margin<T>> buildCompliantMarginals(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T> control,
			AFullNDimensionalMatrix<T> seed,
			boolean parallel){
		
		// check that the dimensions are correct and inform the user of potential issues
		{
		StringBuffer sbErrors = new StringBuffer();
		for (DemographicAttribute<? extends IValue> dimControl: control.getDimensions()) {
			if (!seed.getDimensions().contains(dimControl) 
					&& !control.getDimensions().contains(dimControl.getReferentAttribute())
					&& !seed.getDimensions().stream().anyMatch(dimSeed -> dimSeed.getReferentAttribute().equals(dimControl))
					) {
				sbErrors.append("control does not contains seed dimension ")
					.append(dimControl).append(" (you might add a referent attribute?);\n");
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
		
		// control to seed attribute - relationship can be from referent-to-referent, referent-to-referee, referee-to-referent
		Map<DemographicAttribute<? extends IValue>, DemographicAttribute<? extends IValue>> controlToSeedAttribute = new HashMap<>();
		for(DemographicAttribute<? extends IValue> sAttribute : seed.getDimensions()){
			List<DemographicAttribute<? extends IValue>> cAttList = control.getDimensions().stream()
					.filter(ca -> ca.isLinked(sAttribute))
					.collect(Collectors.toList());
			if(!cAttList.isEmpty())
				for(DemographicAttribute<? extends IValue> cAtt : cAttList)
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
		Collection<Margin<T>> marginals = new ArrayList<>();
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Trying to build marginals for attribute set "
				+Arrays.toString(controlToSeedAttribute.keySet().toArray()), logger, Level.TRACE);
		gspu.sysoStempPerformance(0, this);
		
		// For each dimension of the control matrix connected to seed matrix
		// we will setup a margin with control attributes
		for(DemographicAttribute<? extends IValue> cAttribute : controlToSeedAttribute.keySet()){
			// The set of marginal descriptor, i.e. all combination of values, one per related attribute
			Collection<MarginDescriptor> marginDescriptors = this.getMarginalDescriptors(cAttribute, 
					controlToSeedAttribute.keySet().stream().filter(att -> !att.equals(cAttribute))
						.collect(Collectors.toSet()), 
					control, seed);
			
			logger.debug("Attribute \'"+cAttribute.getAttributeName()+"\' marginal descriptors: "
					+marginDescriptors.size()+" margin(s), "
					+marginDescriptors.stream().flatMap(md -> md.getSeed().stream()).collect(Collectors.toSet()).size()
					+" seed values over ");
			
			Margin<T> mrg = new Margin<>(cAttribute, controlToSeedAttribute.get(cAttribute));
			for(MarginDescriptor marge : marginDescriptors)
				mrg.addMargin(marge, control.getVal(marge.getControl()));
			marginals.add(mrg);
			
			gspu.sysoStempPerformance(1, this);
			double totalMRG = mrg.marginalControl.values().stream().mapToDouble(c -> c.getValue().doubleValue()).sum();
			logger.info("Created marginals (size = {}): cd = {} | sd = {} | sum_of_c = {}", mrg.size() == 0 ? "empty" : mrg.size(),
					mrg.getControlDimension(), mrg.getSeedDimension(), totalMRG); 
		}

		return marginals;
	}


	/**
	 * Return the marginal descriptors coordinate: a collection of all attribute's value combination (a set of value).
	 * This set of value should be compliant with provided n-dimensional matrix
	 * 
	 * @param referent
	 * @return
	 */
	private Collection<MarginDescriptor> getMarginalDescriptors(DemographicAttribute<? extends IValue> targetedAttribute,
			Set<DemographicAttribute<? extends IValue>> sideAttributes, // side attributes just means other targeted attributes
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, T> control,
			AFullNDimensionalMatrix<T> seed){
		if(!control.getDimensions().containsAll(Stream.concat(Stream.of(targetedAttribute), sideAttributes.stream())
				.collect(Collectors.toSet())))
			throw new IllegalArgumentException("Targeted attributes must be compliant with n-dimensional matrix passed as parameter");
		// Setup output - marginal descriptor describe all combination of value that can be related to target attribute
		Collection<MarginDescriptor> marginalDescriptors = new ArrayList<>();
		
		// Start by extracting control marginal
		// ------
		
		// Exclude mapped attribute for which we have referent attribute
		Set<DemographicAttribute<? extends IValue>> sAtt = sideAttributes.stream().filter(a -> a.getReferentAttribute().equals(a)
				|| (a.getReferentAttribute().equals(a) && !sideAttributes.contains(a.getReferentAttribute())))
			.collect(Collectors.toSet());
		
		// Init. the output collection with any attribute
		List<Set<IValue>> controlMarginals = new ArrayList<>();
		DemographicAttribute<? extends IValue> firstAtt = sideAttributes.iterator().next();
		for(IValue value : firstAtt.getValueSpace().getValues())
			controlMarginals.add(Stream.of(value).collect(Collectors.toSet()));
		sideAttributes.remove(firstAtt);
		// Then iterate over all other attributes
		for(DemographicAttribute<? extends IValue> att : sideAttributes){
			List<Set<IValue>> tmpDescriptors = new ArrayList<>();
			for(Set<IValue> descriptors : controlMarginals){
				tmpDescriptors.addAll(att.getValueSpace().getValues().stream()
						.map(val -> Stream.concat(descriptors.stream(), Stream.of(val)).collect(Collectors.toSet()))
						.collect(Collectors.toList())); 
			}
			controlMarginals = tmpDescriptors;
		}
		
		/*
		if(control.isSegmented()) {
			// State equality between referent and mapped attribute
			Set<DemographicAttribute<? extends IValue>> equiToTarget = control.getDimensions().stream()
					.filter(att -> att.getReferentAttribute().equals(targetedAttribute))
					.collect(Collectors.toSet());

			// Translate into control compliant coordinate set of value
			marginalDescriptors =  marginalDescriptors.stream()
					.flatMap(set -> control.getCoordinates(set).stream()
							.filter(coord -> coord.getDimensions().stream().anyMatch(dim -> equiToTarget.contains(dim)))
							.map(coord -> coord.values().stream()
									.filter(val -> !equiToTarget.contains(val.getValueSpace().getAttribute()))
									.collect(Collectors.toSet()))).collect(Collectors.toSet());
		}
		
		// Exclude overlapping marginal descriptors in segmented matrix: e.g. md1 = {age} & md2 = {age, gender}
		final Set<Set<DemographicAttribute<? extends IValue>>> mdAttributes = new HashSet<>();
		for(Set<IValue> marginalDescriptorSet : marginalDescriptors) {
			Set<DemographicAttribute<? extends IValue>> mAttribute = marginalDescriptorSet.stream()
					.map(aspect -> control.getDimension(aspect)).collect(Collectors.toSet());
			mdAttributes.add(mAttribute);
		}
		Set<Set<DemographicAttribute<? extends IValue>>> mdArchitype = mdAttributes.stream().filter(archi -> mdAttributes.stream()
				.noneMatch(mdAtt -> mdAtt.containsAll(archi) && mdAtt.size() > archi.size()))
			.collect(Collectors.toSet());
		return marginalDescriptors.stream().filter(set -> mdArchitype
				.stream().anyMatch(architype -> architype.stream().allMatch(att -> set.stream()
						.anyMatch(val -> att.getValueSpace().getValues().contains(val)))))
			.collect(Collectors.toList());
			*/
		return marginalDescriptors;
	}
	
}
