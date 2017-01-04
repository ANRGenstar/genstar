package gospl.algo;

import java.util.Arrays;
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
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.generator.UtilGenerator;

public class GosplAlgoUtilTest {

	private Logger log = LogManager.getLogger();
	
	private GosplAttributeFactory gaf = new GosplAttributeFactory();
	private ISyntheticGosplPopGenerator generator;
	private Set<APopulationAttribute> attributes;

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

	public GosplAlgoUtilTest(Set<APopulationAttribute> attributes){
		this.attributes = attributes;
		this.generator = new UtilGenerator(attributes);
	}
	
	// ---------------------------------------------------- //

	/**
	 * Create a population with random component and given attributes
	 * 
	 * @param size
	 * @return
	 */
	public IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> getPopulation(int size){
		return generator.generate(size);
	}

	/**
	 * Get a contingency based on a created population using {@link #getPopulation(int)}
	 * 
	 * @param size
	 * @return
	 */
	public AFullNDimensionalMatrix<Integer> getContingency(int size){
		return new GosplDistributionFactory().createContingency(generator.generate(size));
	}

	/**
	 * Get a frequency based on a created population using {@link #getPopulation(int)}
	 * 
	 * @param size
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> getFrequency(int size){
		return new GosplDistributionFactory().createDistribution(generator.generate(size));
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
		log.debug("Try to build segmented matrix with {} dimensions", this.attributes.size());
		Map<APopulationAttribute, Double> attributesProb = this.attributes.stream().collect(
				Collectors.toMap(Function.identity(), att -> new Double(0.5)));
		Set<IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>> populations = new HashSet<>();
		while(!populations.stream().flatMap(pop -> pop.getPopulationAttributes().stream())
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
			populations.add(new UtilGenerator(atts).generate(segmentSize));
		}
		log.debug("Build the segmented matrix with {} inner full matrix", populations.size());
		return new GosplDistributionFactory().createDistribution(populations);
	}

}
