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
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.generator.ISyntheticGosplPopGenerator;

public class GSUtilPopulation {

	private Logger log = LogManager.getLogger();
	
	private ISyntheticGosplPopGenerator generator;
	
	private IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population = null;
	
	private DemographicDictionary<DemographicAttribute<? extends IValue>> dico;
	private Path pathToDictionary = FileSystems.getDefault().getPath("src","test","resources","attributedictionary");
	public static String defaultDictionary = "defaultDictionary.gns";
	
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
	
	@SuppressWarnings("unchecked")
	public GSUtilPopulation(Collection<DemographicAttribute<? extends IValue>> dictionary) {
		dico = new DemographicDictionary<>();
		dictionary.stream().forEach(att -> dico.addAttributes(att));
		this.generator = new GSUtilGenerator(dico);
	}
	
	public GSUtilPopulation() throws GSIllegalRangedData{
		this(defaultDictionary);
	}
	
	// ---------------------------------------------------- //
	
	public Path getPathToDictionary() {
		return pathToDictionary;
	}
	
	public DemographicDictionary<DemographicAttribute<? extends IValue>> getDictionary(){
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
			throw new NullPointerException("No population have been generated - see #buildPopulation");
		log.debug("Try to build segmented matrix with {} dimensions", this.dico.getAttributes().size());
		Map<DemographicAttribute<? extends IValue>, Double> attributesProb = this.dico.getAttributes()
				.stream().collect(Collectors.toMap(Function.identity(), att -> 0.5));

		Collection<Set<DemographicAttribute<? extends IValue>>> segmentedAttribute = new HashSet<>();
		while(!segmentedAttribute.stream().flatMap(set -> set.stream())
				.collect(Collectors.toSet()).containsAll(this.dico.getAttributes())){
			Set<DemographicAttribute<? extends IValue>> atts = new HashSet<>();
			// WARNING: linked attribute could be in the same matrix 
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
