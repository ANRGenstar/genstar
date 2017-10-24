package gospl.algo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.pop.attribute.DemographicAttributeFactory;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UtilGenerator;

public class GosplAlgoUtilTest {

	private Logger log = LogManager.getLogger();
	
	private ISyntheticGosplPopGenerator generator;
	private Set<DemographicAttribute<? extends IValue>> attributes;
	
	private IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population = null;

	public GosplAlgoUtilTest(Set<DemographicAttribute<? extends IValue>> attributes, 
			ISyntheticGosplPopGenerator generator){
		this.attributes = attributes;
		this.generator = generator;
	}
	
	public GosplAlgoUtilTest(Set<DemographicAttribute<? extends IValue>> attributes){
		this(attributes, new UtilGenerator(attributes));
	}
	
	public GosplAlgoUtilTest() throws GSIllegalRangedData{
		this.attributes = new HashSet<>();
		this.attributes.add(DemographicAttributeFactory.getFactory().createAttribute(
				"Genre", GSEnumDataType.Nominal, Arrays.asList("Homme", "Femme")));
		this.attributes.add(DemographicAttributeFactory.getFactory().createAttribute("Age", GSEnumDataType.Range, 
				Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus")));
		this.attributes.add(DemographicAttributeFactory.getFactory().createAttribute(
				"Couple", GSEnumDataType.Boolean, Arrays.asList("oui", "non")));
		this.attributes.add(DemographicAttributeFactory.getFactory().createAttribute(
				"Education", GSEnumDataType.Order, Arrays.asList("pre-bac", "bac", "licence", "master et plus")));
		this.attributes.add(DemographicAttributeFactory.getFactory().createAttribute("Activité", GSEnumDataType.Nominal, 
				Arrays.asList("inactif", "chomage", "employé", "fonctionnaire", "indépendant", "retraité")));
		this.generator = new UtilGenerator(attributes);
	}
	
	/**
	 * Create a population with random component and given attributes
	 * 
	 * @param size
	 * @return 
	 * @return
	 */
	public IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> buildPopulation(int size){
		this.population = generator.generate(size);
		return this.population;
	}
	
	// ---------------------------------------------------- //


	/**
	 * Get a contingency based on a created population using {@link #getPopulation(int)}
	 * 
	 * @param size
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> getContingency(){
		if(this.population == null)
			throw new NullPointerException("No population have been generated - see #buildPopulation");
		return new GosplNDimensionalMatrixFactory().createContingency(this.population);
	}

	/**
	 * Get a frequency based on a created population using {@link #getPopulation(int)}
	 * 
	 * @param size
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> getFrequency(){
		if(this.population == null)
			throw new NullPointerException("No population have been generated - see #buildPopulation");
		return new GosplNDimensionalMatrixFactory().createDistribution(this.population);
	}

	/**
	 * Get a segmented frequency based on several created population using {@link #getPopulation(int)}
	 * 
	 * @param segmentSize
	 * @return
	 * @throws IllegalDistributionCreation
	 */
	public ASegmentedNDimensionalMatrix<Double> getSegmentedFrequency(int segmentSize) 
			throws IllegalDistributionCreation {
		if(this.population == null)
			this.buildPopulation(segmentSize);
		log.debug("Try to build segmented matrix with {} dimensions", this.attributes.size());
		Map<DemographicAttribute<? extends IValue>, Double> attributesProb = this.attributes.stream().collect(
				Collectors.toMap(Function.identity(), att -> 0.5));

		Collection<Set<DemographicAttribute<? extends IValue>>> segmentedAttribute = new HashSet<>();
		while(!segmentedAttribute.stream().flatMap(set -> set.stream())
				.collect(Collectors.toSet()).containsAll(this.attributes)){
			Set<DemographicAttribute<? extends IValue>> atts = new HashSet<>();
			for(DemographicAttribute<? extends IValue> attribute : attributesProb.keySet()){
				if(GenstarRandom.getInstance().nextDouble() < attributesProb.get(attribute)){
					atts.add(attribute);
					attributesProb.put(attribute, attributesProb.get(attribute) * 0.5); 
				} else {
					attributesProb.put(attribute, Math.tanh(attributesProb.get(attribute) + 0.5));
				}
			}
			if(atts.size() < 2)
				continue;
			log.debug("Build a new full inner matrix with {} attributes", 
					atts.stream().map(a -> a.getAttributeName()).collect(Collectors.joining(", ")));
			segmentedAttribute.add(atts);
		}
		log.debug("Build the segmented matrix with {} inner full matrix", segmentedAttribute.size());
		GosplNDimensionalMatrixFactory factory = new GosplNDimensionalMatrixFactory();
		return factory.createDistributionFromDistributions(segmentedAttribute.stream()
				.map(sa -> factory.createDistribution(sa, this.population))
				.collect(Collectors.toSet()));
	}

}
