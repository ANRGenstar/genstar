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
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import gospl.algo.generator.ISyntheticGosplPopGenerator;
import gospl.algo.generator.UtilGenerator;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;

public class GosplAlgoUtilTest {

	private Logger log = LogManager.getLogger();
	
	private GosplAttributeFactory gaf = new GosplAttributeFactory();
	private ISyntheticGosplPopGenerator generator;
	private Set<APopulationAttribute> attributes;
	
	private IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population = null;

	public GosplAlgoUtilTest(Set<APopulationAttribute> attributes, 
			ISyntheticGosplPopGenerator generator){
		this.attributes = attributes;
		this.generator = generator;
	}
	
	public GosplAlgoUtilTest(Set<APopulationAttribute> attributes){
		this(attributes, new UtilGenerator(attributes));
	}
	
	public GosplAlgoUtilTest() throws GSIllegalRangedData{
		this.attributes = new HashSet<>();
		this.attributes.add(gaf.createAttribute("Genre", GSEnumDataType.String, 
				Arrays.asList("Homme", "Femme"), GSEnumAttributeType.unique));
		this.attributes.add(gaf.createAttribute("Age", GSEnumDataType.Integer, 
				Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus"), GSEnumAttributeType.range));
		this.attributes.add(gaf.createAttribute("Couple", GSEnumDataType.Boolean, 
				Arrays.asList("oui", "non"), GSEnumAttributeType.unique));
		this.attributes.add(gaf.createAttribute("Education", GSEnumDataType.String, 
				Arrays.asList("pre-bac", "bac", "licence", "master et plus"), GSEnumAttributeType.unique));
		this.attributes.add(gaf.createAttribute("Activité", GSEnumDataType.String, 
				Arrays.asList("inactif", "chomage", "employé", "fonctionnaire", "indépendant", "retraité"), GSEnumAttributeType.unique));
		this.generator = new UtilGenerator(attributes);
	}
	
	/**
	 * Create a population with random component and given attributes
	 * 
	 * @param size
	 * @return 
	 * @return
	 */
	public IPopulation<APopulationEntity,APopulationAttribute,APopulationValue> buildPopulation(int size){
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
		Map<APopulationAttribute, Double> attributesProb = this.attributes.stream().collect(
				Collectors.toMap(Function.identity(), att -> new Double(0.5)));

		Collection<Set<APopulationAttribute>> segmentedAttribute = new HashSet<>();
		while(!segmentedAttribute.stream().flatMap(set -> set.stream())
				.collect(Collectors.toSet()).containsAll(this.attributes)){
			Set<APopulationAttribute> atts = new HashSet<>();
			for(APopulationAttribute attribute : attributesProb.keySet()){
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
