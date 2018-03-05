package gospl.generator.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.DemographicDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.generator.ISyntheticGosplPopGenerator;

/**
 * Util class to generate population from a dictionary using either a custom generator or, if not provided,
 * a random default generator.
 * <p> 
 * <b>HINT</b>: Could be realy usefull when you want to quickly generate a population and you do not care about how reliable it is.
 * For ex. to make test on or to be used for localisation / networking.
 * 
 * @author kevinchapuis
 *
 */
public class GSUtilPopulation {

	private Logger log = LogManager.getLogger();
	
	private ISyntheticGosplPopGenerator generator;
	
	private IPopulation<ADemoEntity, Attribute<? extends IValue>> population = null;
	
	private DemographicDictionary<Attribute<? extends IValue>> dico;
	private Path pathToDictionary = FileSystems.getDefault().getPath("src","test","resources","attributedictionary");
	public static String defaultDictionary = "defaultDictionary.gns";
	
	/**
	 * Default constructor that use a pre-define dictionary of attributes.
	 * 
	 * @throws GSIllegalRangedData
	 */
	public GSUtilPopulation() throws GSIllegalRangedData{
		this(defaultDictionary);
	}
	
	/**
	 * Uses custom generator to generate a population. Relationship to dictionary is not guarantee and
	 * must be set before calling {@link GSUtilPopulation}
	 * 
	 * @param dictionary
	 * @param generator
	 */
	public GSUtilPopulation(DemographicDictionary<Attribute<? extends IValue>> dictionary,
			ISyntheticGosplPopGenerator generator) {
		this.dico = dictionary;
		this.generator = generator;
	}
	
	/**
	 * Uses a random generator to generate a population based on a dictionary of attribute
	 * 
	 * @param dictionary
	 */
	public GSUtilPopulation(DemographicDictionary<Attribute<? extends IValue>> dictionary) {
		this.dico = dictionary;
		this.generator = new GSUtilGenerator(dico);
	}
	
	/**
	 * Uses custom generator to generate a population based on a dictionary of attribute
	 * 
	 * @param dictionaryFile
	 * @param generator
	 */
	@SuppressWarnings("unchecked")
	public GSUtilPopulation(String dictionaryFile, 
			ISyntheticGosplPopGenerator generator){
		try {
			this.dico = new GenstarJsonUtil().unmarshalFromGenstarJson(pathToDictionary
					.resolve(dictionaryFile), DemographicDictionary.class);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.generator = generator;
	}
	
	/**
	 * same as {@link #GSUtilPopulation(DemographicDictionary)} with dictionary path
	 * 
	 * @param dictionaryFile
	 */
	@SuppressWarnings("unchecked")
	public GSUtilPopulation(Path dictionaryFile){
		try {
			this.dico = new GenstarJsonUtil().unmarshalFromGenstarJson(
					dictionaryFile, DemographicDictionary.class);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.generator = new GSUtilGenerator(dico);
	}
	
	/**
	 * same as {@link #GSUtilPopulation(DemographicDictionary)} with dictionary path provided as {@link String}
	 * 
	 * @param dictionaryFile
	 */
	@SuppressWarnings("unchecked")
	public GSUtilPopulation(String dictionaryFile){
		try {
			this.dico = new GenstarJsonUtil().unmarshalFromGenstarJson(pathToDictionary
					.resolve(dictionaryFile), DemographicDictionary.class);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.generator = new GSUtilGenerator(dico);
	}
	
	/**
	 * same as {@link #GSUtilPopulation(DemographicDictionary)} but with a custom dictionary based on attribute
	 * collection passed as argument
	 * 
	 * @param dictionary
	 */
	@SuppressWarnings("unchecked")
	public GSUtilPopulation(Collection<Attribute<? extends IValue>> dictionary) {
		dico = new DemographicDictionary<>();
		dictionary.stream().forEach(att -> dico.addAttributes(att));
		this.generator = new GSUtilGenerator(dico);
	}
		
	// ---------------------------------------------------- //
	
	/**
	 * Path to dictionary
	 * 
	 * @return
	 */
	public Path getPathToDictionary() {
		return pathToDictionary;
	}
	
	/**
	 * The dictionary of attribute used to generate entity wi
	 * th
	 * @return
	 */
	public DemographicDictionary<Attribute<? extends IValue>> getDictionary(){
		return dico;
	}
	
	// ---------------------------------------------------- //
	
	/**
	 * Create a population with random component and given attributes
	 * 
	 * @param size
	 * @return 
	 * @return
	 */
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> buildPopulation(int size){
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
			throw new NullPointerException("No population have been generated - see #buildPopulation");
		log.debug("Try to build segmented matrix with {} dimensions", this.dico.getAttributes().size());
		Map<Attribute<? extends IValue>, Double> attributesProb = this.dico.getAttributes()
				.stream().collect(Collectors.toMap(Function.identity(), att -> 0.5));

		Collection<Set<Attribute<? extends IValue>>> segmentedAttribute = new HashSet<>();
		while(!segmentedAttribute.stream().flatMap(set -> set.stream())
				.collect(Collectors.toSet()).containsAll(this.dico.getAttributes())){
			Set<Attribute<? extends IValue>> atts = new HashSet<>();
			// WARNING: linked attribute could be in the same matrix 
			for(Attribute<? extends IValue> attribute : attributesProb.keySet()){
				if(atts.stream().anyMatch(a -> a.getReferentAttribute().equals(attribute)
						|| a.equals(attribute.getReferentAttribute())))
					continue;
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
