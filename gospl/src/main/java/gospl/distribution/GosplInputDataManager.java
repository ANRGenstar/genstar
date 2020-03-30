/*********************************************************************************************
 *
 * 'GosplDistributionFactory.java, in plugin gospl, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gospl.distribution;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

import org.apache.commons.math3.exception.ZeroException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.IEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.io.util.ReadMultiLayerEntityUtils;

/**
 * Main class to setup and harmonize input data. Can handle:
 * <p><ul>
 * <li>Contingency or frequency table => collapse into one distribution of attribute, i.e. {@link INDimensionalMatrix}
 * <li>Sample => convert to population, i.e. {@link IPopulation}
 * </ul><p>
 * TODO: the ability to input statistical moment or custom distribution
 * TODO: move all static method into a factory
 * 
 * @author kevinchapuis
 *
 */
public class GosplInputDataManager {

	protected final static Logger logger = LogManager.getLogger();
	
	protected final static double EPSILON = Math.pow(10d, -3);

	private final GenstarConfigurationFile configuration;

	private Set<AFullNDimensionalMatrix<? extends Number>> inputData;
	private Set<GosplPopulation> samples;

	public GosplInputDataManager(final Path configurationFilePath) 
			throws IllegalArgumentException, IOException {
		this.configuration = new GenstarJsonUtil()
				.unmarchalConfigurationFileFromGenstarJson(configurationFilePath);
	}
	
	public GosplInputDataManager(final GenstarConfigurationFile configurationFile) {
		this.configuration = configurationFile;
	}

	/**
	 * Returns the configuration file used to manage data
	 * 
	 * @return
	 */
	public GenstarConfigurationFile getConfiguration() {
		return configuration;
	}
	
	/**
	 * 
	 * Main methods to parse and get control totals from a {@link GSDataFile} file and with the help of a specified set
	 * of {@link Attribute}
	 * <p>
	 * Method gets all data file from the builder and harmonizes them to one another using line identifier attributes
	 * 
	 * @return A {@link Set} of {@link INDimensionalMatrix}
	 * @throws InputFileNotSupportedException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws MatrixCoordinateException
	 * @throws InvalidFileTypeException
	 */
	public void buildDataTables() throws IOException, InvalidSurveyFormatException, InvalidFormatException {
		GosplSurveyFactory sf = new GosplSurveyFactory();
		this.inputData = new HashSet<>();
		for (final GSSurveyWrapper wrapper : this.configuration.getSurveyWrappers())
			if (!wrapper.getSurveyType().equals(GSSurveyType.Sample))
				this.inputData.addAll(
						getDataTables(
								sf.getSurvey(
										wrapper, 
										this.configuration.getBaseDirectory()
										), 
						this.configuration.getDictionary()
						));
	}

	/**
	 * Main methods to parse and get samples cast into population of according type in Gospl. More precisely, each
	 * sample is transposed where each individual in the survey is a {@link IEntity} in a synthetic {@link IPopulation}
	 * 
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws InvalidFileTypeException
	 * 
	 */
	public void buildSamples() throws IOException, InvalidSurveyFormatException, InvalidFormatException {
		GosplSurveyFactory sf = new GosplSurveyFactory();
		samples = new HashSet<>();
		for (final GSSurveyWrapper wrapper : this.configuration.getSurveyWrappers())
			if (wrapper.getSurveyType().equals(GSSurveyType.Sample))
				samples.add(
						getSample(
								sf.getSurvey(
										wrapper, 
										this.configuration.getBaseDirectory()),
								this.configuration.getDictionary(), null,
								Collections.emptyMap()
						));
	}
	
	/**
	 * Main methods to parse a multi layered population, based on {@link #getMutliLayerSample(IGSSurvey, Set, Integer, Map)}
	 * 
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public void buildMultiLayerSamples() throws InvalidFormatException, IOException, InvalidSurveyFormatException {
		GosplSurveyFactory sf = new GosplSurveyFactory();
		samples = new HashSet<>();
		for (final GSSurveyWrapper wrapper : this.configuration.getSurveyWrappers()
				.stream().filter(survey -> survey.getSurveyType().equals(GSSurveyType.Sample))
				.collect(Collectors.toList())) {
			samples.addAll(
					getMutliLayerSample(
							sf.getSurvey(wrapper, this.configuration.getBaseDirectory()), 
							this.configuration.getDictionaries(), null, Collections.emptyMap())
					);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------------- ACCESSORS -------------------------------- //
	/////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns an unmodifiable view of input data tables, as a raw set of matrices
	 * @return
	 */
	public Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, ? extends Number>> 
			getRawDataTables() {
		return Collections.unmodifiableSet(this.inputData);
	}
	
	/**
	 * Returns an unmodifiable view of input contingency tables. If there is not any
	 * contingency data in input tables, then return an empty set
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<AFullNDimensionalMatrix<Integer>> getContingencyTables(){
		return this.inputData.stream()
					.filter(
							matrix -> matrix.getMetaDataType().equals(GSSurveyType.ContingencyTable))
					.map(
							matrix -> (AFullNDimensionalMatrix<Integer>) matrix)
					.collect(Collectors.toSet());
	}
	
	/**
	 * Returns an unmodifiable view of input samples 
	 * @return
	 */
	public Set<IPopulation<ADemoEntity, Attribute<? extends IValue>>> getRawSamples(){
		return Collections.unmodifiableSet(this.samples);
	}
	
	/**
	 * 
	 * Create a frequency matrix from all input data tables
	 * 
	 * @return
	 * @throws IllegalDistributionCreation
	 * @throws IllegalControlTotalException
	 * @throws MatrixCoordinateException
	 *
	 */
	public INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> 
			collapseDataTablesIntoDistribution()
					throws IllegalDistributionCreation, IllegalControlTotalException {
		
		if (inputData.isEmpty())
			throw new IllegalArgumentException(
					"To collapse matrices you must build at least one first: "+
					"see the buildDataTables method");
		
		if (inputData.size() == 1)
			return getFrequency(inputData.iterator().next(), inputData);
		
		final Set<AFullNDimensionalMatrix<Double>> fullMatrices = new HashSet<>();
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Proceed to distribution collapse", logger);
		gspu.sysoStempPerformance(0, this);
		
		// Matrices that contain a record attribute
		for (AFullNDimensionalMatrix<? extends Number> recordMatrices : inputData.stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(d -> 
					this.configuration.getDictionary().getRecords().contains(d)))
				.collect(Collectors.toSet()))
				fullMatrices.add(getTransposedRecord(this.getConfiguration().getDictionary(), recordMatrices));
		
		gspu.sysoStempPerformance(1, this);
		gspu.sysoStempMessage("Collapse record attribute: done");
		
		// Matrices that do not contain any record attribute
		for (final AFullNDimensionalMatrix<? extends Number> mat : inputData.stream()
				.filter(mat -> mat.getDimensions().stream().allMatch(d -> !isRecordAttribute(this.getConfiguration().getDictionary(), d)))
				.collect(Collectors.toSet()))
			fullMatrices.add(getFrequency(mat, inputData));
		
		gspu.sysoStempPerformance(2, this);
		gspu.sysoStempMessage("Transpose to frequency: done");
				
		return new GosplConditionalDistribution(fullMatrices);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------- inner utility methods -------------------------- //
	/////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get the distribution matrix from data files, using provided dictionary
	 * 
	 * FIXME: must check for record attribute to build data table accordingly
	 * 
	 * @param survey
	 * @param dictionary
	 * @return
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public static Set<AFullNDimensionalMatrix<? extends Number>> getDataTables(final IGSSurvey survey,
			final IGenstarDictionary<Attribute<? extends IValue>> dictionary) 
			throws IOException, InvalidSurveyFormatException {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Retrieve data table from files", logger, Level.TRACE);
		
		final Set<AFullNDimensionalMatrix<? extends Number>> cTableSet = new HashSet<>();
		final GSDataParser dataParser = new GSDataParser();
		
		// Read headers and store possible variables by line index
		final Map<Integer, Set<IValue>> rowHeaders = survey.getRowHeaders(dictionary);
		gspu.sysoStempMessage("detected in {} {} row headers : {}", 
				survey.getSurveyFilePath(), rowHeaders.size(), rowHeaders);
		
		// Read headers and store possible variables by column index
		final Map<Integer, Set<IValue>> columnHeaders = survey.getColumnHeaders(dictionary);
		gspu.sysoStempMessage("detected in {} {} column headers : {}", 
				survey.getSurveyFilePath(), columnHeaders.size(), columnHeaders);

		// Store column related attributes while keeping unrelated attributes separated
		// WARNING: Works with attribute name because of record attribute
		final Set<Set<String>> columnSchemas = new HashSet<>();
		for(Set<IValue> cValues : columnHeaders.values())
			columnSchemas.add(cValues.stream()
										.map(v -> v.getValueSpace().getAttribute().getAttributeName())
										.collect(Collectors.toSet())
										);
						
		// Remove lower generality schema: e.g. if we have schema [A,B] then [A] or [B] will be skiped
		columnSchemas.removeAll(columnSchemas.stream().filter(schema -> 
			columnSchemas.stream()
						.anyMatch(higherSchema -> schema.stream()
								.allMatch(att -> higherSchema.contains(att)) 
									&& higherSchema.size() > schema.size()))
						.collect(Collectors.toSet()));
		
		// Store line related attributes while keeping unrelated attributes separated
		// WARNING: Works with attribute name because of record attribute
		final Set<Set<String>> rowSchemas = new HashSet<>();
		for(Set<IValue> rValues : rowHeaders.values())
			rowSchemas.add(rValues.stream()
									.map(v -> v.getValueSpace().getAttribute().getAttributeName())
									.collect(Collectors.toSet())
									);
		
		rowSchemas.removeAll(rowSchemas.stream().filter(schema -> 
			rowSchemas.stream()
				.anyMatch(higherSchema -> schema.stream()
						.allMatch(att -> higherSchema.contains(att)) 
							&& higherSchema.size() > schema.size()))
				.collect(Collectors.toSet()));

		// Start iterating over each related set of attribute
		for (final Set<String> rSchema : rowSchemas) {
			for (final Set<String> cSchema : columnSchemas) {
				// Create a matrix for each set of related attribute
				AFullNDimensionalMatrix<? extends Number> jDistribution;
				// Matrix 'dimension / aspect' map
				final Set<Attribute<? extends IValue>> dimTable = 
						Stream.concat(
								rSchema.stream().filter(ra -> dictionary.containsAttribute(ra))
									.map(ra -> dictionary.getAttribute(ra)), 
								cSchema.stream().filter(ra -> dictionary.containsAttribute(ra))
									.map(ra -> dictionary.getAttribute(ra)))
								.collect(Collectors.toSet());
				// Instantiate either contingency (int and global frame of reference) or frequency (double and either
				// global or local frame of reference) matrix
				if (survey.getDataFileType().equals(GSSurveyType.ContingencyTable))
					jDistribution = new GosplContingencyTable(dimTable);
				else
					jDistribution = new GosplJointDistribution(dimTable, survey.getDataFileType());
				
				jDistribution.setLabel(survey.getName());
				jDistribution.addGenesis("from file "+survey.getName());
				
				// Fill in the matrix through line & column
				for (final Integer row : rowHeaders.entrySet().stream()
						.filter(e -> rSchema.stream().allMatch(att -> e.getValue().stream()
								.anyMatch(val -> val.getValueSpace().getAttribute().getAttributeName().equals(att))))
						.map(e -> e.getKey()).collect(Collectors.toSet())) {
					
					for (final Integer col : columnHeaders.entrySet().stream()
							.filter(e -> cSchema.stream().allMatch(att -> e.getValue().stream()
									.anyMatch(val -> val.getValueSpace().getAttribute().getAttributeName().equals(att))))
							.map(e -> e.getKey()).collect(Collectors.toSet())) {
						// The value
						final String stringVal = survey.read(row, col);
						// Value type
						final GSEnumDataType dt = dataParser.getValueType(stringVal);
						// Store coordinate for the value. It is made of all line & column attribute's aspects
						final Map<Attribute<? extends IValue>, IValue> coordSet =
								Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream())
									.filter(vals -> dictionary.containsValue(vals.getStringValue())) // Filter record value
									.collect(Collectors.toMap(
											val -> dictionary.getAttribute(val.getValueSpace().getAttribute().getAttributeName()), 
											Function.identity()));
						final ACoordinate<Attribute<? extends IValue>, IValue> coord = new GosplCoordinate(coordSet);
						// Add the coordinate / parsed value pair into the matrix
						if (dt.isNumericValue())
							if (!jDistribution.addValue(coord, jDistribution.parseVal(dataParser, stringVal)))
								jDistribution.getVal(coord).add(jDistribution.parseVal(dataParser, stringVal));
					}
				}
				cTableSet.add(jDistribution);
			}
		}
		return cTableSet;
	}

	/**
	 * Based on a survey wrapping data, and for a given set of expected attributes, 
	 * creates a GoSPl population.
	 */
	public static GosplPopulation getSample(final IGSSurvey survey, 
			final IGenstarDictionary<Attribute<? extends IValue>> dictionnary)
			throws IOException, InvalidSurveyFormatException {
		return getSample(survey, dictionnary, null, Collections.emptyMap());
	}
	
	/**
	 * Based on a survey wrapping data, and for a given set of expected attributes, 
	 * creates a GoSPl population.
	 */
	public static GosplPopulation getSample(final IGSSurvey survey, 
			final IGenstarDictionary<Attribute<? extends IValue>> dictionnary, 
			Integer maxIndividuals,
			Map<String,String> keepOnlyEqual
			)
			throws IOException, InvalidSurveyFormatException {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Retrieve a sample from a data file", logger, Level.DEBUG);
		
		final GosplPopulation sampleSet = new GosplPopulation();
		
		// Read headers and store possible variables by column index
		final Map<Integer, Attribute<? extends IValue>> columnHeaders = 
				survey.getColumnSample(dictionnary);

		if (columnHeaders.isEmpty()) 
			throw new RuntimeException("no column header was decoded in survey "+survey+"; are you sure you provided a relevant dictionnary of data?");
		
		int unmatchSize = 0;
		int maxIndivSize = columnHeaders.keySet().stream().max((i1, i2) -> i1.compareTo(i2)).get();
		
		loopLines: for (int i = survey.getFirstRowIndex(); i <= survey.getLastRowIndex(); i++) {
			
			// too much ?
			if (maxIndividuals != null && sampleSet.size() >= maxIndividuals)
				break;
			
			final Map<Attribute<? extends IValue>, IValue> entityAttributes = new HashMap<>();
			final List<String> indiVals = survey.readLine(i);
			//System.err.println(i+" "+indiVals);

			if(indiVals.size() <= maxIndivSize){
				gspu.sysoStempMessage("One individual does not fit required number of attributes: \n"
						+ Arrays.toString(indiVals.toArray()));
						
				unmatchSize++;
				continue;
			}
			for (final Integer idx : columnHeaders.keySet()){
				
				String actualStringValue = indiVals.get(idx);
				
				Attribute<? extends IValue> att = columnHeaders.get(idx);
				IValue val = null;
				if(actualStringValue == GosplSurveyFactory.UNKNOWN_VARIABLE)
					val = att.getValueSpace().getEmptyValue();
				else
					val = att.getValueSpace().addValue(actualStringValue);
				
				// filter
				if (val != null) {
					String expected = keepOnlyEqual.get(att.getAttributeName());
					if (expected != null && !val.getStringValue().equals(expected))
						// skip
						continue loopLines;
				}
				
				if (val!=null)
					entityAttributes.put(att, val);
				else if (	att.getEmptyValue().getStringValue() != null 
							&& att.getEmptyValue().getStringValue().equals(indiVals.get(idx)))
					entityAttributes.put(att, att.getValueSpace().getEmptyValue());
				else {
					gspu.sysoStempMessage("Data modality "+indiVals.get(idx)+" does not match any value for attribute "
							+att.getAttributeName());
					unmatchSize++;
				}
			}
			if(entityAttributes.size() == entityAttributes.size())
				sampleSet.add(new GosplEntity(entityAttributes));
		}
		if (unmatchSize > 0) {
			gspu.sysoStempMessage("Input sample has bypass "+new DecimalFormat("#.##").format(unmatchSize/(double)sampleSet.size()*100)
				+"% ("+unmatchSize+") of entities due to unmatching attribute's value");
		}
		return sampleSet;
	}
	
	/**
	 * Retrieve a multi layered sample from a survey (micro-data) and a dictionary per layer
	 * 
	 * @param survey
	 * @param layerDicos
	 * @return
	 */
	public static List<GosplPopulation> getMutliLayerSample(final IGSSurvey survey, 
			Set<IGenstarDictionary<Attribute<? extends IValue>>> layerDicos,
			Integer maxIndividuals, Map<String,String> keepOnlyEqual) {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Retrieve a multi layered sample from a data file", logger, Level.DEBUG);
		
		final List<GosplPopulation> samples = layerDicos.stream().map(dico -> new GosplPopulation())
				.collect(Collectors.toList());
		
		// Read dictionary and store attributes according to Layer level
		Map<Integer, String> layerId = new HashMap<>();
		Map<Integer, String> layerWgt = new HashMap<>();
		Map<Attribute<? extends IValue>,Integer> layerAtt = new HashMap<>();
		
		Map<Integer,Set<ReadMultiLayerEntityUtils>> layerEntityCollection = new HashMap<>();
		
		// Read headers and store possible variables by column index
		final Map<Integer, Attribute<? extends IValue>> columnHeaders = new HashMap<>();
		final Map<String, Integer> idWgtColumnHeaders = new HashMap<>();
		for (IGenstarDictionary<Attribute<? extends IValue>> dico : layerDicos) {
			
			layerId.put(dico.getLevel(), dico.getIdentifierAttributeName());
			layerWgt.put(dico.getLevel(), dico.getWeightAttributeName());
			
			dico.getAttributes().stream().forEach(a -> layerAtt.put(a, dico.getLevel()));
			
			columnHeaders.putAll(survey.getColumnSample(dico));
			idWgtColumnHeaders.putAll(survey.getColumnIdAndWeight(dico));
			
			layerEntityCollection.put(dico.getLevel(), new HashSet<>());
		}
		
		int unmatchSize = 0;
		int zeroLayerIdx = 0;

		loopLines : for (int i = survey.getFirstRowIndex(); i <= survey.getLastRowIndex(); i++) {
			
			// too much ?
			if (maxIndividuals != null && zeroLayerIdx >= maxIndividuals)
				break;
			
			final List<String> indiVals = survey.readLine(i);
			
			List<String> localIDs = layerId.keySet().stream().sorted()
					.map(layer -> indiVals.get(idWgtColumnHeaders.get(layerId.get(layer))))
					.collect(Collectors.toList());
			
			Map<Integer,ReadMultiLayerEntityUtils> localEntities = new HashMap<>();
			for (IGenstarDictionary<Attribute<? extends IValue>> layer : layerDicos) {
				ReadMultiLayerEntityUtils localEntity = new ReadMultiLayerEntityUtils(
						layer.getLevel(), // LAYER LEVEL
						indiVals.get(idWgtColumnHeaders.get(layerId.get(layer.getLevel()))), // ID 
						indiVals.get(idWgtColumnHeaders.get(layerWgt.get(layer.getLevel()))), // WEIGHT
						new HashMap<>()); // ATTRIBUTE :: VALUE
				localEntity.setIDs(localIDs);
				localEntities.put(layer.getLevel(), localEntity);
			}
						
			for (final Integer idx : columnHeaders.keySet()){
				String actualStringValue = indiVals.get(idx);
				
				Attribute<? extends IValue> att = columnHeaders.get(idx);
				IValue val = null;
				if(actualStringValue == GosplSurveyFactory.UNKNOWN_VARIABLE)
					val = att.getValueSpace().getEmptyValue();
				else
					val = att.getValueSpace().addValue(actualStringValue);
				
				// filter
				if (val != null) {
					String expected = keepOnlyEqual.get(att.getAttributeName());
					if (expected != null && !val.getStringValue().equals(expected))
						// skip
						continue loopLines;
				}
				
				if (val == null) {
					if (	att.getEmptyValue().getStringValue() != null 
								&& att.getEmptyValue().getStringValue().equals(indiVals.get(idx)))
						val = att.getValueSpace().getEmptyValue();
					else {
						gspu.sysoStempMessage("Data modality "+indiVals.get(idx)+" does not match any value for attribute "
								+att.getAttributeName());
						unmatchSize++;
						// skip because not valide value
						continue loopLines;
					}
				}
				
				localEntities.get(layerAtt.get(att)).getEntity().put(att,val);
				zeroLayerIdx++;
				
			}
			
			for (Integer layer : localEntities.keySet()) { layerEntityCollection.get(layer).add(localEntities.get(layer)); }
			
		}
		
		// Put lower level entities into upper level entities
		
		List<Integer> layers =  layerDicos.stream()
				.map(IGenstarDictionary::getLevel)
				.filter(i -> i > 0)
				.collect(Collectors.toList());
	
		// Start with 0 level
		
		Map<ReadMultiLayerEntityUtils,GosplEntity> zeroLayerEntities = layerEntityCollection.get(0).stream()
				.collect(Collectors.toMap(
						Function.identity(),
						ReadMultiLayerEntityUtils::toGosplEntity
						));
		samples.get(0).addAll(zeroLayerEntities.values());
		
		// Add upper level and build Parent > Child relationship
		
		for( Integer layer : layers ) {
			Map<ReadMultiLayerEntityUtils,GosplEntity> layerEntities = layerEntityCollection.get(layer).stream()
					.collect(Collectors.toMap(
							Function.identity(),
							ReadMultiLayerEntityUtils::toGosplEntity
							));
			for(ReadMultiLayerEntityUtils child : zeroLayerEntities.keySet()) {
				String upperId = child.getIDs().get(layer);
				
				Optional<ReadMultiLayerEntityUtils> parent = layerEntities.keySet().stream()
						.filter(upEntity -> upEntity.getId().equals(upperId)).findFirst();
				
				if(parent.isPresent()) {
					layerEntities.get(parent.get()).addChild(zeroLayerEntities.get(child));
					zeroLayerEntities.get(child).setParent(layerEntities.get(parent.get()));
				} else {
					throw new NullPointerException("Cannot find parent Entity for "+child);
				}
				
			}
			zeroLayerEntities = layerEntities;
			samples.get(layer).addAll(layerEntities.values());
		}
		
		if (unmatchSize > 0) {
			gspu.sysoStempMessage("Input sample has bypass "+new DecimalFormat("#.##").format(unmatchSize/(double)samples.get(0).size()*100)
				+"% ("+unmatchSize+") of entities due to unmatching attribute's value");
		}
		
		return samples;
	}
	
	/*
	 * Transpose any matrix to a frequency based matrix
	 */
	protected static AFullNDimensionalMatrix<Double> getFrequency(
			final AFullNDimensionalMatrix<? extends Number> matrix,
			final Set<AFullNDimensionalMatrix<? extends Number>> context)
					throws IllegalControlTotalException {
		
		// returned matrix
		AFullNDimensionalMatrix<Double> freqMatrix = null;
		
		if (matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)) {
			// Identify local referent dimension
			final Map<Attribute<? extends IValue>, List<AControl<? extends Number>>> mappedControls =
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValueSpace().getValues()
							.stream().map(a -> matrix.getVal(a)).collect(Collectors.toList())));
			final Attribute<? extends IValue> localReferentDimension =
					mappedControls.entrySet().stream()
							.filter(e -> e.getValue().stream()
									.allMatch(ac -> ac.equalsCastedVal(e.getValue().get(0), EPSILON)))
							.map(e -> e.getKey()).findFirst().get();
			final AControl<? extends Number> localReferentControl =
					mappedControls.get(localReferentDimension).iterator().next();

			// The most appropriate align referent matrix (the one that have most information about matrix to align,
			// i.e. the highest number of shared dimensions)
			final Optional<AFullNDimensionalMatrix<? extends Number>> optionalRef = context.stream()
					.filter(ctFitter -> !ctFitter.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)
							&& ctFitter.getDimensions().contains(localReferentDimension))
					.sorted((jd1,
							jd2) -> (int) jd2.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d))
									.count()
									- (int) jd1.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d))
											.count())
					.findFirst();
			
			if (optionalRef.isPresent()) {
				freqMatrix = new GosplJointDistribution(matrix.getDimensions(), GSSurveyType.GlobalFrequencyTable);
				final AFullNDimensionalMatrix<? extends Number> matrixOfReference = optionalRef.get();
				final double totalControl =
						matrixOfReference.getVal(localReferentDimension.getValueSpace().getValues()
								.stream().collect(Collectors.toSet())).getValue().doubleValue();
				final Map<IValue, Double> freqControls =
						localReferentDimension.getValueSpace().getValues().stream().collect(Collectors.toMap(lrv -> lrv,
								lrv -> matrixOfReference.getVal(lrv).getValue().doubleValue() / totalControl));

				for (final ACoordinate<Attribute<? extends IValue>, IValue> controlKey : matrix.getMatrix().keySet()) {
					freqMatrix.addValue(controlKey,
							new ControlFrequency(matrix.getVal(controlKey).getValue().doubleValue()
									/ localReferentControl.getValue().doubleValue()
									* freqControls.get(controlKey.getMap().get(localReferentDimension))));
				}
			} else
				throw new IllegalControlTotalException("The matrix (" + matrix.getLabel()
						+ ") must be aligned to global frequency table but lacks of a referent matrix", matrix);
		} else {
			// Init output matrix
			freqMatrix = new GosplJointDistribution(matrix.getDimensions(), GSSurveyType.GlobalFrequencyTable);
			freqMatrix.setLabel((matrix.getLabel()==null?"?/joint":matrix.getLabel()+"/joint"));

			if (matrix.getMetaDataType().equals(GSSurveyType.GlobalFrequencyTable)) {
				for (final ACoordinate<Attribute<? extends IValue>, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue()));
			} else {
				final AControl<? extends Number> total = matrix.getVal();
				for (final Attribute<? extends IValue> attribut : matrix.getDimensions()) {
					final AControl<? extends Number> controlAtt = matrix.getVal(attribut.getValueSpace().getValues().stream()
							.collect(Collectors.toSet()));
					if (Math.abs(controlAtt.getValue().doubleValue() - total.getValue().doubleValue())
							/ controlAtt.getValue().doubleValue() > EPSILON)
						throw new IllegalControlTotalException(total, controlAtt);
				}
				for (final ACoordinate<Attribute<? extends IValue>, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(
							matrix.getVal(coord).getValue().doubleValue() / total.getValue().doubleValue()));
			}
		}
		
		freqMatrix.inheritGenesis(matrix);
		freqMatrix.addGenesis("converted to frequency GosplDistributionBuilder@@getFrequency");

		return freqMatrix;
	}
	
	/*
	 * Result in the same matrix without any record attribute
	 */
	public static AFullNDimensionalMatrix<Double> getTransposedRecord(
			IGenstarDictionary<Attribute<? extends IValue>> dictionary,
			AFullNDimensionalMatrix<? extends Number> recordMatrices) {
		
		Set<Attribute<? extends IValue>> dims = recordMatrices.getDimensions().stream()
				.filter(d -> !isRecordAttribute(dictionary, d)).collect(Collectors.toSet());
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Transpose process of matrix "
				+Arrays.toString(recordMatrices.getDimensions().toArray()), logger, Level.TRACE);
		gspu.sysoStempPerformance(0, GosplInputDataManager.class);
		gspu.setObjectif(recordMatrices.getMatrix().size());
		
		AFullNDimensionalMatrix<Double> freqMatrix = new GosplJointDistribution(dims, GSSurveyType.GlobalFrequencyTable);
		freqMatrix.inheritGenesis(recordMatrices);
		freqMatrix.addGenesis("transposted by GosplDistributionBuilder@getTransposedRecord");

		AControl<? extends Number> recordMatrixControl = recordMatrices.getVal();
		
		int iter = 1;
		for(ACoordinate<Attribute<? extends IValue>, IValue> oldCoord : recordMatrices.getMatrix().keySet()){
			if(iter % (gspu.getObjectif()/10) == 0)
				gspu.sysoStempPerformance(0.1, GosplInputDataManager.class);
			Map<Attribute<? extends IValue>, IValue> newCoord = new HashMap<>(oldCoord.getMap());
			dims.stream().forEach(dim -> newCoord.remove(dim));
			freqMatrix.addValue(new GosplCoordinate(newCoord), 
					new ControlFrequency(recordMatrices.getVal(oldCoord).getValue().doubleValue() 
							/ recordMatrixControl.getValue().doubleValue()));
		}
		
		return freqMatrix;
	}
	
	public static boolean isRecordAttribute(IGenstarDictionary<Attribute<? extends IValue>> dictionary,
			Attribute<? extends IValue> attribute){
		return dictionary.getRecords().contains(attribute);
	}


}
