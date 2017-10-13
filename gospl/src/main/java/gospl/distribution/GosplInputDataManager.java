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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarXmlSerializer;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.DemographicAttribute;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.metamodel.pop.io.IGSSurvey;
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

/**
 * Main class to setup and harmonize input data. Can handle:
 * <p><ul>
 * <li>Contingency or frequency table => collapse into one distribution of attribute, i.e. {@link INDimensionalMatrix}
 * <li>Sample => convert to population, i.e. {@link IPopulation}
 * </ul><p>
 * TODO: the ability to input statistical moment or custom distribution
 * 
 * @author kevinchapuis
 *
 */
public class GosplInputDataManager {

	private static Logger logger = LogManager.getLogger();
	
	private final double EPSILON = Math.pow(10d, -3);

	private final GenstarConfigurationFile configuration;
	private final GSDataParser dataParser;

	private Set<AFullNDimensionalMatrix<? extends Number>> inputData;
	private Set<GosplPopulation> samples;

	public GosplInputDataManager(final Path configurationFilePath) throws FileNotFoundException {
		this.configuration = new GenstarXmlSerializer().deserializeGSConfig(configurationFilePath);
		this.configuration.setBaseDirectory(configurationFilePath.toFile());
		this.dataParser = new GSDataParser();
	}
	
	public GosplInputDataManager(final GenstarConfigurationFile configurationFile) {
		this.configuration = configurationFile;
		this.dataParser = new GSDataParser();
	}

	/**
	 * 
	 * Main methods to parse and get control totals from a {@link GSDataFile} file and with the help of a specified set
	 * of {@link DemographicAttribute}
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
				this.inputData.addAll(getDataTables(sf.getSurvey(wrapper, this.configuration.getBaseDirectory() == null ? 
						null : this.configuration.getBaseDirectory()), 
						this.configuration.getDemoDictionary().getAttributes()
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
				samples.add(getSample(sf.getSurvey(wrapper, this.configuration.getBaseDirectory()), 
						this.configuration.getDemoDictionary().getAttributes()));
	}

	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------------- ACCESSORS -------------------------------- //
	/////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns an unmodifiable view of input data tables, as a raw set of matrices
	 * @return
	 */
	public Set<INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, ? extends Number>> getRawDataTables() {
		return Collections.unmodifiableSet(this.inputData);
	}
	
	/**
	 * Returns an unmodifiable view of input contingency tables. If there is not any
	 * contingency data in input tables, then return an empty set
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<AFullNDimensionalMatrix<Integer>> getContingencyTables(){
		return this.inputData.stream().filter(matrix -> matrix.getMetaDataType().equals(GSSurveyType.ContingencyTable))
				.map(matrix -> (AFullNDimensionalMatrix<Integer>) matrix).collect(Collectors.toSet());
	}
	
	/**
	 * Returns an unmodifiable view of input samples 
	 * @return
	 */
	public Set<IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>>> getRawSamples(){
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
	public INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> collapseDataTablesIntoDistributions()
			throws IllegalDistributionCreation, IllegalControlTotalException {
		if (inputData.isEmpty())
			throw new IllegalArgumentException(
					"To collapse matrices you must build at least one first: see buildDistributions method");
		if (inputData.size() == 1)
			return getFrequency(inputData.iterator().next());
		final Set<AFullNDimensionalMatrix<Double>> fullMatrices = new HashSet<>();
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Proceed to distribution collapse", logger);
		gspu.sysoStempPerformance(0, this);
		
		// Matrices that contain a record attribute
		for (AFullNDimensionalMatrix<? extends Number> recordMatrices : inputData.stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(d -> this.isRecordAttribute(d)))
				.collect(Collectors.toSet())){
			if(recordMatrices.getDimensions().stream().filter(d -> !configuration.getDemoDictionary()
					.getRecordAttribute().contains(d))
					.allMatch(d -> fullMatrices.stream().allMatch(matOther -> !matOther.getDimensions().contains(d))))
				fullMatrices.add(getTransposedRecord(recordMatrices));
		}
		
		gspu.sysoStempPerformance(1, this);
		gspu.sysoStempMessage("Collapse record attribute: done");
		
		// Matrices that do not contain any record attribute
		for (final AFullNDimensionalMatrix<? extends Number> mat : inputData.stream()
				.filter(mat -> mat.getDimensions().stream().allMatch(d -> !this.isRecordAttribute(d)))
				.collect(Collectors.toSet()))
			fullMatrices.add(getFrequency(mat));
		
		gspu.sysoStempPerformance(2, this);
		gspu.sysoStempMessage("Transpose to frequency: done");
				
		return new GosplConditionalDistribution(fullMatrices);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------- inner utility methods -------------------------- //
	/////////////////////////////////////////////////////////////////////////////////

	/*
	 * Get the distribution matrix from data files
	 */
	private Set<AFullNDimensionalMatrix<? extends Number>> getDataTables(final IGSSurvey survey,
			final Set<DemographicAttribute<? extends IValue>> attributes) throws IOException, InvalidSurveyFormatException {
		
		final Set<AFullNDimensionalMatrix<? extends Number>> cTableSet = new HashSet<>();
		
		// Read headers and store possible variables by line index
		final Map<Integer, Set<IValue>> rowHeaders = survey.getRowHeaders(attributes);
		// Read headers and store possible variables by column index
		final Map<Integer, Set<IValue>> columnHeaders = survey.getColumnHeaders(attributes);

		// Store column related attributes while keeping unrelated attributes separated
		final Set<Set<DemographicAttribute<? extends IValue>>> columnSchemas = new HashSet<>();
		for(Set<IValue> cValues : columnHeaders.values())
			columnSchemas.add(cValues.stream()
					.map(v -> attributes.stream().filter(att -> att.equals(v.getValueSpace().getAttribute()))
							.findFirst().get()).collect(Collectors.toSet()));
						
		// Remove lower generality schema: e.g. if we have scheam [A,B] then [A] or [B] will be skiped
		columnSchemas.removeAll(columnSchemas.stream().filter(schema -> 
			columnSchemas.stream().anyMatch(higherSchema -> schema.stream()
					.allMatch(att -> higherSchema.contains(att)) && higherSchema.size() > schema.size()))
				.collect(Collectors.toSet()));
		
		// Store line related attributes while keeping unrelated attributes separated
		final Set<Set<DemographicAttribute<? extends IValue>>> rowSchemas = new HashSet<>();
		for(Set<IValue> rValues : rowHeaders.values())
			rowSchemas.add(rValues.stream()
					.map(v -> attributes.stream().filter(att -> att.equals(v.getValueSpace().getAttribute()))
							.findFirst().get()).collect(Collectors.toSet()));
		
		rowSchemas.removeAll(rowSchemas.stream().filter(schema -> 
			rowSchemas.stream().anyMatch(higherSchema -> schema.stream()
				.allMatch(att -> higherSchema.contains(att)) && higherSchema.size() > schema.size()))
				.collect(Collectors.toSet()));

		// Start iterating over each related set of attribute
		for (final Set<DemographicAttribute<? extends IValue>> rSchema : rowSchemas) {
			for (final Set<DemographicAttribute<? extends IValue>> cSchema : columnSchemas) {
				// Create a matrix for each set of related attribute
				AFullNDimensionalMatrix<? extends Number> jDistribution;
				// Matrix 'dimension / aspect' map
				final Set<DemographicAttribute<? extends IValue>> dimTable = Stream.concat(rSchema.stream(), cSchema.stream())
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
								.anyMatch(val -> val.getValueSpace().getAttribute().equals(att))))
						.map(e -> e.getKey()).collect(Collectors.toSet())) {
					for (final Integer col : columnHeaders.entrySet().stream()
							.filter(e -> cSchema.stream().allMatch(att -> e.getValue().stream()
									.anyMatch(val -> val.getValueSpace().getAttribute().equals(att))))
							.map(e -> e.getKey()).collect(Collectors.toSet())) {
						// The value
						final String stringVal = survey.read(row, col);
						// Value type
						final GSEnumDataType dt = dataParser.getValueType(stringVal);
						// Store coordinate for the value. It is made of all line & column attribute's aspects
						final Map<DemographicAttribute<? extends IValue>, IValue> coordSet =
								Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream())
										.collect(Collectors.toMap(val -> attributes.stream()
												.filter(att -> att.equals(val.getValueSpace().getAttribute()))
												.findFirst().get(), Function.identity()));
						final ACoordinate<DemographicAttribute<? extends IValue>, IValue> coord = new GosplCoordinate(coordSet);
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

	/*
	 * Transpose any matrix to a frequency based matrix
	 */
	private AFullNDimensionalMatrix<Double> getFrequency(final AFullNDimensionalMatrix<? extends Number> matrix)
			throws IllegalControlTotalException {
		
		// returned matrix
		AFullNDimensionalMatrix<Double> freqMatrix = null;
		
		if (matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)) {
			// Identify local referent dimension
			final Map<DemographicAttribute<? extends IValue>, List<AControl<? extends Number>>> mappedControls =
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValueSpace()
							.stream().map(a -> matrix.getVal(a)).collect(Collectors.toList())));
			final DemographicAttribute<? extends IValue> localReferentDimension =
					mappedControls.entrySet().stream()
							.filter(e -> e.getValue().stream()
									.allMatch(ac -> ac.equalsCastedVal(e.getValue().get(0), EPSILON)))
							.map(e -> e.getKey()).findFirst().get();
			final AControl<? extends Number> localReferentControl =
					mappedControls.get(localReferentDimension).iterator().next();

			// The most appropriate align referent matrix (the one that have most information about matrix to align,
			// i.e. the highest number of shared dimensions)
			final Optional<AFullNDimensionalMatrix<? extends Number>> optionalRef = inputData.stream()
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
						matrixOfReference.getVal(localReferentDimension.getValueSpace()
								.stream().collect(Collectors.toSet())).getValue().doubleValue();
				final Map<IValue, Double> freqControls =
						localReferentDimension.getValueSpace().stream().collect(Collectors.toMap(lrv -> lrv,
								lrv -> matrixOfReference.getVal(lrv).getValue().doubleValue() / totalControl));

				for (final ACoordinate<DemographicAttribute<? extends IValue>, IValue> controlKey : matrix.getMatrix().keySet()) {
					freqMatrix.addValue(controlKey,
							new ControlFrequency(matrix.getVal(controlKey).getValue().doubleValue()
									/ localReferentControl.getValue().doubleValue()
									* freqControls.get(controlKey.getMap().get(localReferentDimension))));
				}
			} else
				throw new IllegalControlTotalException("The matrix (" + matrix.getLabel()
						+ ") must be align to globale frequency table but lack of a referent matrix", matrix);
		} else {
			// Init output matrix
			freqMatrix = new GosplJointDistribution(matrix.getDimensions(), GSSurveyType.GlobalFrequencyTable);
			freqMatrix.setLabel((matrix.getLabel()==null?"?/joint":matrix.getLabel()+"/joint"));

			if (matrix.getMetaDataType().equals(GSSurveyType.GlobalFrequencyTable)) {
				for (final ACoordinate<DemographicAttribute<? extends IValue>, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue()));
			} else {
				final AControl<? extends Number> total = matrix.getVal();
				for (final DemographicAttribute<? extends IValue> attribut : matrix.getDimensions()) {
					final AControl<? extends Number> controlAtt = matrix.getVal(attribut.getValueSpace().stream()
							.collect(Collectors.toSet()));
					if (Math.abs(controlAtt.getValue().doubleValue() - total.getValue().doubleValue())
							/ controlAtt.getValue().doubleValue() > this.EPSILON)
						throw new IllegalControlTotalException(total, controlAtt);
				}
				for (final ACoordinate<DemographicAttribute<? extends IValue>, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(
							matrix.getVal(coord).getValue().doubleValue() / total.getValue().doubleValue()));
			}
		}
		
		freqMatrix.inheritGenesis(matrix);
		freqMatrix.addGenesis("converted to frequency GosplDistributionBuilder@@getFrequency");

		return freqMatrix;
	}

	/**
	 * Based on a survey wrapping data, and for a given set of expected attributes, 
	 * creates a GoSPl population.
	 */
	public static GosplPopulation getSample(final IGSSurvey survey, 
			final Set<DemographicAttribute<? extends IValue>> attributes)
			throws IOException, InvalidSurveyFormatException {
		return getSample(survey, attributes, null, Collections.emptyMap());
	}
	
	/**
	 * Based on a survey wrapping data, and for a given set of expected attributes, 
	 * creates a GoSPl population.
	 */
	public static GosplPopulation getSample(final IGSSurvey survey, 
			final Set<DemographicAttribute<? extends IValue>> attributes, 
			Integer maxIndividuals,
			Map<String,String> keepOnlyEqual
			)
			throws IOException, InvalidSurveyFormatException {
		
		final GosplPopulation sampleSet = new GosplPopulation();
		
		// Read headers and store possible variables by column index
		final Map<Integer, DemographicAttribute<? extends IValue>> columnHeaders = survey.getColumnSample(attributes);

		if (columnHeaders.isEmpty()) 
			throw new RuntimeException("no column header was decoded in survey "+survey+"; are you sure you provided a relevant dictionnary of data?");
		
		int unmatchSize = 0;
		int maxIndivSize = columnHeaders.keySet().stream().max((i1, i2) -> i1.compareTo(i2)).get();
		
		loopLines: for (int i = survey.getFirstRowIndex(); i <= survey.getLastRowIndex(); i++) {
			
			// too much ?
			if (maxIndividuals != null && sampleSet.size() >= maxIndividuals)
				break;
			
			final Map<DemographicAttribute<? extends IValue>, IValue> entityAttributes = new HashMap<>();
			final List<String> indiVals = survey.readLine(i);
			//System.err.println(i+" "+indiVals);

			if(indiVals.size() <= maxIndivSize){
				logger.warn("One individual does not fit required number of attributes: \n"
						+ Arrays.toString(indiVals.toArray()));
						
				unmatchSize++;
				continue;
			}
			for (final Integer idx : columnHeaders.keySet()){
				
				DemographicAttribute<? extends IValue> att = columnHeaders.get(idx);
				IValue val = att.getValueSpace().addValue(indiVals.get(idx));
				
				// filter
				if (val != null) {
					String expected = keepOnlyEqual.get(att.getAttributeName());
					if (expected != null && !val.getStringValue().equals(expected))
						// skip
						continue loopLines;
				}
				
				if (val!=null)
					entityAttributes.put(att, val);
				else if (att.getEmptyValue().getStringValue().equals(indiVals.get(idx)))
					entityAttributes.put(att, att.getValueSpace().getEmptyValue());
				else {
					logger.warn("Data modality "+indiVals.get(idx)+" does not match any value for attribute "
							+att.getAttributeName());
					unmatchSize++;
				}
			}
			if(entityAttributes.size() == entityAttributes.size())
				sampleSet.add(new GosplEntity(entityAttributes));
		}
		if (unmatchSize > 0) {
			logger.debug("Input sample has bypass "+new DecimalFormat("#.##").format(unmatchSize/(double)sampleSet.size()*100)
				+"% ("+unmatchSize+") of entities due to unmatching attribute's value");
		}
		return sampleSet;
	}
	
	/*
	 * Result in the same matrix without any record attribute
	 */
	private AFullNDimensionalMatrix<Double> getTransposedRecord(
			AFullNDimensionalMatrix<? extends Number> recordMatrices) {
		
		Set<DemographicAttribute<? extends IValue>> dims = recordMatrices.getDimensions().stream().filter(d -> !this.isRecordAttribute(d))
				.collect(Collectors.toSet());
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Transpose process of matrix "
				+Arrays.toString(recordMatrices.getDimensions().toArray()), logger, Level.TRACE);
		gspu.sysoStempPerformance(0, this);
		gspu.setObjectif(recordMatrices.getMatrix().size());
		
		AFullNDimensionalMatrix<Double> freqMatrix = new GosplJointDistribution(dims, GSSurveyType.GlobalFrequencyTable);
		freqMatrix.inheritGenesis(recordMatrices);
		freqMatrix.addGenesis("transposted by GosplDistributionBuilder@getTransposedRecord");

		AControl<? extends Number> recordMatrixControl = recordMatrices.getVal();
		
		int iter = 1;
		for(ACoordinate<DemographicAttribute<? extends IValue>, IValue> oldCoord : recordMatrices.getMatrix().keySet()){
			if(iter % (gspu.getObjectif()/10) == 0)
				gspu.sysoStempPerformance(0.1, this);
			Map<DemographicAttribute<? extends IValue>, IValue> newCoord = new HashMap<>(oldCoord.getMap());
			dims.stream().forEach(dim -> newCoord.remove(dim));
			freqMatrix.addValue(new GosplCoordinate(newCoord), 
					new ControlFrequency(recordMatrices.getVal(oldCoord).getValue().doubleValue() 
							/ recordMatrixControl.getValue().doubleValue()));
		}
		
		return freqMatrix;
	}
	
	private boolean isRecordAttribute(DemographicAttribute<? extends IValue> attribute){
		return configuration.getDemoDictionary().getRecordAttribute().contains(attribute);
	}


}
