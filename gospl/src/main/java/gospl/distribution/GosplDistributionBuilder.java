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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.metamodel.pop.io.IGSSurvey;
import core.util.GSPerformanceUtil;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.GosplPopulation;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.entity.GosplEntity;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;

public class GosplDistributionBuilder {

	private Logger logger = LogManager.getLogger();
	
	private final double EPSILON = Math.pow(10d, -3);

	private final GenstarConfigurationFile configuration;
	private final GSDataParser dataParser;

	private Set<AFullNDimensionalMatrix<? extends Number>> distributions;
	private Set<GosplPopulation> samples;

	public GosplDistributionBuilder(final Path configurationFilePath) throws FileNotFoundException {
		this.configuration = new GenstarXmlSerializer().deserializeGSConfig(configurationFilePath);
		this.configuration.setBaseDirectory(configurationFilePath.toFile());
		this.dataParser = new GSDataParser();
	}
	
	public GosplDistributionBuilder(final GenstarConfigurationFile configurationFile) {
		this.configuration = configurationFile;
		this.dataParser = new GSDataParser();
	}

	/**
	 * 
	 * Main methods to parse and get control totals from a {@link GSDataFile} file and with the help of a specified set
	 * of {@link APopulationAttribute}
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
	public void buildDistributions() throws IOException, InvalidSurveyFormatException, InvalidFormatException {
		GosplSurveyFactory sf = new GosplSurveyFactory();
		this.distributions = new HashSet<>();
		for (final GSSurveyWrapper wrapper : this.configuration.getSurveyWrappers())
			if (!wrapper.getSurveyType().equals(GSSurveyType.Sample))
				this.distributions.addAll(getDistribution(sf.getSurvey(wrapper, this.configuration.getBaseDirectory()==null?null:this.configuration.getBaseDirectory()), 
						this.configuration.getAttributes()));
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
				samples.add(getSample(sf.getSurvey(wrapper, this.configuration.getBaseDirectory()), this.configuration.getAttributes()));
	}

	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------------- ACCESSORS -------------------------------- //
	/////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns an unmodifiable view of the distributions, as a raw distributions i.e. without any prior checking
	 * @return
	 */
	public Set<INDimensionalMatrix<APopulationAttribute, APopulationValue, ? extends Number>> getRawDistributions() {
		return Collections.unmodifiableSet(this.distributions);
	}
	
	/**
	 * Returns an unmodifiable view of input samples 
	 * @return
	 */
	public Set<IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>> getRawSamples(){
		return Collections.unmodifiableSet(this.samples);
	}
	
	/**
	 * 
	 * Create a matrix from all matrices build with this factory
	 * 
	 * @return
	 * @throws IllegalDistributionCreation
	 * @throws IllegalControlTotalException
	 * @throws MatrixCoordinateException
	 *
	 */
	public INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> collapseDistributions()
			throws IllegalDistributionCreation, IllegalControlTotalException {
		if (distributions.isEmpty())
			throw new IllegalArgumentException(
					"To collapse matrices you must build at least one first: see buildDistributions method");
		if (distributions.size() == 1)
			return getFrequency(distributions.iterator().next());
		final Set<AFullNDimensionalMatrix<Double>> fullMatrices = new HashSet<>();
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Proceed to distribution collapse", logger);
		gspu.sysoStempPerformance(0, this);
		
		// Matrices that contain a record attribute
		for (AFullNDimensionalMatrix<? extends Number> recordMatrices : distributions.stream()
				.filter(mat -> mat.getDimensions().stream().anyMatch(d -> d.isRecordAttribute()))
				.collect(Collectors.toSet())){
			if(recordMatrices.getDimensions().stream().filter(d -> !d.isRecordAttribute())
					.allMatch(d -> fullMatrices.stream().allMatch(matOther -> !matOther.getDimensions().contains(d))))
				fullMatrices.add(getTransposedRecord(recordMatrices));
		}
		
		gspu.sysoStempPerformance(1, this);
		gspu.sysoStempMessage("Collapse record attribute: done");
		
		// Matrices that do not contain any record attribute
		for (final AFullNDimensionalMatrix<? extends Number> mat : distributions.stream()
				.filter(mat -> mat.getDimensions().stream().allMatch(d -> !d.isRecordAttribute()))
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
	private Set<AFullNDimensionalMatrix<? extends Number>> getDistribution(final IGSSurvey survey,
			final Set<APopulationAttribute> attributes) throws IOException, InvalidSurveyFormatException {
		final Set<AFullNDimensionalMatrix<? extends Number>> cTableSet = new HashSet<>();
		
		// Read headers and store possible variables by line index
		final Map<Integer, Set<APopulationValue>> rowHeaders = getRowHeaders(survey, attributes);
		// Read headers and store possible variables by column index
		final Map<Integer, Set<APopulationValue>> columnHeaders = getColumnHeaders(survey, attributes);

		// Store column related attributes while keeping unrelated attributes separated
		final Set<Set<APopulationAttribute>> columnSchemas = columnHeaders.values().stream()
				.map(head -> head.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		// Remove lower generality schema: e.g. if we have scheam [A,B] then [A] or [B] will be skiped
		columnSchemas.removeAll(columnSchemas.stream().filter(schema -> 
			columnSchemas.stream().anyMatch(higherSchema -> schema.stream()
					.allMatch(att -> higherSchema.contains(att)) && higherSchema.size() > schema.size()))
				.collect(Collectors.toSet()));
		// Store line related attributes while keeping unrelated attributes separated
		final Set<Set<APopulationAttribute>> rowSchemas = rowHeaders.values().stream()
				.map(line -> line.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		rowSchemas.removeAll(rowSchemas.stream().filter(schema -> 
			rowSchemas.stream().anyMatch(higherSchema -> schema.stream()
				.allMatch(att -> higherSchema.contains(att)) && higherSchema.size() > schema.size()))
				.collect(Collectors.toSet()));

		// Start iterating over each related set of attribute
		for (final Set<APopulationAttribute> rSchema : rowSchemas) {
			for (final Set<APopulationAttribute> cSchema : columnSchemas) {
				// Create a matrix for each set of related attribute
				AFullNDimensionalMatrix<? extends Number> jDistribution;
				// Matrix 'dimension / aspect' map
				final Map<APopulationAttribute, Set<APopulationValue>> dimTable = Stream.concat(rSchema.stream(), cSchema.stream())
						.collect(Collectors.toMap(a -> a, a -> a.getValues()));
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
								.anyMatch(val -> val.getAttribute().equals(att))))
						.map(e -> e.getKey()).collect(Collectors.toSet())) {
					for (final Integer col : columnHeaders.entrySet().stream()
							.filter(e -> cSchema.stream().allMatch(att -> e.getValue().stream()
									.anyMatch(val -> val.getAttribute().equals(att))))
							.map(e -> e.getKey()).collect(Collectors.toSet())) {
						// The value
						final String stringVal = survey.read(row, col);
						// Value type
						final GSEnumDataType dt = dataParser.getValueType(stringVal);
						// Store coordinate for the value. It is made of all line & column attribute's aspects
						final Set<APopulationValue> coordSet =
								Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream())
										.collect(Collectors.toSet());
						final ACoordinate<APopulationAttribute, APopulationValue> coord = new GosplCoordinate(coordSet);
						// Add the coordinate / parsed value pair into the matrix
						if (dt == GSEnumDataType.Integer || dt == GSEnumDataType.Double)
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
			final Map<APopulationAttribute, List<AControl<? extends Number>>> mappedControls =
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues().parallelStream()
							.map(a -> matrix.getVal(a)).collect(Collectors.toList())));
			final APopulationAttribute localReferentDimension =
					mappedControls.entrySet().stream()
							.filter(e -> e.getValue().stream()
									.allMatch(ac -> ac.equalsCastedVal(e.getValue().get(0), EPSILON)))
							.map(e -> e.getKey()).findFirst().get();
			final AControl<? extends Number> localReferentControl =
					mappedControls.get(localReferentDimension).iterator().next();

			// The most appropriate align referent matrix (the one that have most information about matrix to align,
			// i.e. the highest number of shared dimensions)
			final Optional<AFullNDimensionalMatrix<? extends Number>> optionalRef = distributions.stream()
					.filter(ctFitter -> !ctFitter.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)
							&& ctFitter.getDimensions().contains(localReferentDimension))
					.sorted((jd1,
							jd2) -> (int) jd2.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d))
									.count()
									- (int) jd1.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d))
											.count())
					.findFirst();
			if (optionalRef.isPresent()) {
				freqMatrix = new GosplJointDistribution(
						matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues())),
						GSSurveyType.GlobalFrequencyTable);
				final AFullNDimensionalMatrix<? extends Number> matrixOfReference = optionalRef.get();
				final double totalControl =
						matrixOfReference.getVal(localReferentDimension.getValues()).getValue().doubleValue();
				final Map<APopulationValue, Double> freqControls =
						localReferentDimension.getValues().stream().collect(Collectors.toMap(lrv -> lrv,
								lrv -> matrixOfReference.getVal(lrv).getValue().doubleValue() / totalControl));

				for (final ACoordinate<APopulationAttribute, APopulationValue> controlKey : matrix.getMatrix().keySet()) {
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
			freqMatrix = new GosplJointDistribution(
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues())),
					GSSurveyType.GlobalFrequencyTable);
			freqMatrix.setLabel((matrix.getLabel()==null?"?/joint":matrix.getLabel()+"/joint"));

			if (matrix.getMetaDataType().equals(GSSurveyType.GlobalFrequencyTable)) {
				for (final ACoordinate<APopulationAttribute, APopulationValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue()));
			} else {
				final AControl<? extends Number> total = matrix.getVal();
				for (final APopulationAttribute attribut : matrix.getDimensions()) {
					final AControl<? extends Number> controlAtt = matrix.getVal(attribut.getValues());
					if (Math.abs(controlAtt.getValue().doubleValue() - total.getValue().doubleValue())
							/ controlAtt.getValue().doubleValue() > this.EPSILON)
						throw new IllegalControlTotalException(total, controlAtt);
				}
				for (final ACoordinate<APopulationAttribute, APopulationValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(
							matrix.getVal(coord).getValue().doubleValue() / total.getValue().doubleValue()));
			}
		}
		
		freqMatrix.inheritGenesis(matrix);
		freqMatrix.addGenesis("converted to frequency GosplDistributionBuilder@@getFrequency");

		return freqMatrix;
	}

	/*
	 * TODO: describre
	 */
	private GosplPopulation getSample(final IGSSurvey survey, 
			final Set<APopulationAttribute> attributes)
			throws IOException, InvalidSurveyFormatException {
		final GosplPopulation sampleSet = new GosplPopulation();
		
		// Read headers and store possible variables by column index
		final Map<Integer, APopulationAttribute> columnHeaders = getColumnSample(survey, attributes);

		if (columnHeaders.isEmpty()) 
			throw new RuntimeException("no column header was found in survey "+survey);
		
		int unmatchSize = 0;
		int maxIndivSize = columnHeaders.keySet().stream().max((i1, i2) -> i1.compareTo(i2)).get();
		
		for (int i = survey.getFirstRowIndex(); i <= survey.getLastRowIndex(); i++) {
			final Map<APopulationAttribute, APopulationValue> entityAttributes = new HashMap<>();
			final List<String> indiVals = survey.readLine(i);
			if(indiVals.size() <= maxIndivSize){
				logger.trace("One individual does not fit required number of attributes: \n"
						+ Arrays.toString(indiVals.toArray()));
						
				unmatchSize++;
				continue;
			}
			for (final Integer idx : columnHeaders.keySet()){
				Optional<APopulationValue> opVal = columnHeaders.get(idx).getValues()
						.stream().filter(val -> val.getInputStringValue().equals(indiVals.get(idx))).findAny();
				if(opVal.isPresent())
					entityAttributes.put(columnHeaders.get(idx), opVal.get());
				else if(columnHeaders.get(idx).getEmptyValue().getInputStringValue().equals(indiVals.get(idx)))
					entityAttributes.put(columnHeaders.get(idx), columnHeaders.get(idx).getEmptyValue());
				else{
					logger.warn("Data modality "+indiVals.get(idx)+" does not match any value for attribute "
							+columnHeaders.get(idx).getAttributeName());
					unmatchSize++;
				}
			}
			if(entityAttributes.size() == entityAttributes.size())
				sampleSet.add(new GosplEntity(entityAttributes));
		}
		if (unmatchSize > 0) {
			logger.debug("Input sample have bypass "+new DecimalFormat("#.##").format(unmatchSize/(double)sampleSet.size()*100)
				+"% ("+unmatchSize+") of entities due to unmatching attribute's value");
		}
		return sampleSet;
	}
	
	/*
	 * Result in the same matrix without any record attribute
	 */
	private AFullNDimensionalMatrix<Double> getTransposedRecord(
			AFullNDimensionalMatrix<? extends Number> recordMatrices) {
		
		Set<APopulationAttribute> dims = recordMatrices.getDimensions().stream().filter(d -> !d.isRecordAttribute())
				.collect(Collectors.toSet());
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Transpose process of matrix "
				+Arrays.toString(recordMatrices.getDimensions().toArray()), logger, Level.TRACE);
		gspu.sysoStempPerformance(0, this);
		gspu.setObjectif(recordMatrices.getMatrix().size());
		
		AFullNDimensionalMatrix<Double> freqMatrix = new GosplJointDistribution(dims.stream()
				.collect(Collectors.toMap(d -> d, d -> d.getValues())), GSSurveyType.GlobalFrequencyTable);
		freqMatrix.inheritGenesis(recordMatrices);
		freqMatrix.addGenesis("transposted by GosplDistributionBuilder@getTransposedRecord");

		AControl<? extends Number> recordMatrixControl = recordMatrices.getVal();
		
		int iter = 1;
		for(ACoordinate<APopulationAttribute, APopulationValue> oldCoord : recordMatrices.getMatrix().keySet()){
			if(iter % (gspu.getObjectif()/10) == 0)
				gspu.sysoStempPerformance(0.1, this);
			Set<APopulationValue> newCoord = new HashSet<>(oldCoord.values());
			newCoord.retainAll(dims.stream().flatMap(dim -> dim.getValues().stream()).collect(Collectors.toSet()));
			freqMatrix.addValue(new GosplCoordinate(newCoord), 
					new ControlFrequency(recordMatrices.getVal(oldCoord).getValue().doubleValue() 
							/ recordMatrixControl.getValue().doubleValue()));
		}
		
		return freqMatrix;
	}

	///////////////////////////////////////////////////////////////////////
	// -------------------------- back office -------------------------- //
	///////////////////////////////////////////////////////////////////////

	private Map<Integer, Set<APopulationValue>> getRowHeaders(
			final IGSSurvey survey, final Set<APopulationAttribute> attributes) {
		final List<Integer> attributeIdx = new ArrayList<>();
		for (int line = 0; line < survey.getFirstRowIndex(); line++) {
			final List<String> sLine = survey.readLine(line);
			for (int idx = 0; idx < survey.getFirstColumnIndex(); idx++) {
				final String headAtt = sLine.get(idx);
				if (attributes.stream().map(att -> att.getAttributeName())
						.anyMatch(attName -> attName.equals(headAtt)))
					attributeIdx.add(idx);
				if (headAtt.isEmpty()) {
					final List<String> valList = survey.readColumn(idx);
					if (attributes.stream().anyMatch(att -> att.getValues().stream()
							.allMatch(val -> valList.contains(val.getInputStringValue()))))
						attributeIdx.add(idx);
				}
			}
		}

		final Map<Integer, Set<APopulationValue>> rowHeaders = new HashMap<>();
		for (int i = survey.getFirstRowIndex(); i <= survey.getLastRowIndex(); i++) {
			final List<String> rawLine = survey.readColumns(0, survey.getFirstColumnIndex(), i);
			final List<String> line = attributeIdx.stream().map(idx -> rawLine.get(idx)).collect(Collectors.toList());
			for (int j = 0; j < line.size(); j++) {
				final String lineVal = line.get(j);
				final Set<APopulationValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(lineVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<APopulationAttribute> inferedHeads = new HashSet<>();
					final List<String> headList = survey.readLines(0, survey.getFirstRowIndex(), j);
					if (headList.stream().allMatch(s -> s.isEmpty())) {
						for (final List<String> column : survey.readColumns(0, survey.getFirstColumnIndex()))
							inferedHeads.addAll(attributes.stream()
									.filter(a -> a.getValues().stream()
											.allMatch(av -> column.contains(av.getInputStringValue())))
									.collect(Collectors.toSet()));
					} else {
						inferedHeads.addAll(headList.stream()
								.flatMap(s -> attributes.stream().filter(a -> a.getAttributeName().equals(s)))
								.collect(Collectors.toSet()));
					}
					final Set<APopulationValue> vals2 = new HashSet<>(vals);
					for (final IValue val : vals2)
						if (!inferedHeads.contains(val.getAttribute()))
							vals.remove(val);
				}
				if (rowHeaders.containsKey(i))
					rowHeaders.get(i).addAll(vals);
				else
					rowHeaders.put(i, new HashSet<>(vals));
			}
		}
		return rowHeaders;
	}

	private Map<Integer, Set<APopulationValue>> getColumnHeaders(
			final IGSSurvey survey, final Set<APopulationAttribute> attributes) {
		final Map<Integer, Set<APopulationValue>> columnHeaders = new HashMap<>();
		for (int i = survey.getFirstColumnIndex(); i <= survey.getLastColumnIndex(); i++) {
			final List<String> column = survey.readLines(0, survey.getFirstRowIndex(), i);
			for (String columnVal : column) {
				Set<APopulationValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(columnVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<APopulationValue> vals2 = new HashSet<>(vals);
					vals = column.stream()
							.flatMap(s -> attributes.stream().filter(att -> att.getAttributeName().equals(s)))
							.flatMap(att -> vals2.stream().filter(v -> v.getAttribute().equals(att)))
							.collect(Collectors.toSet());
				}
				if (columnHeaders.containsKey(i))
					columnHeaders.get(i).addAll(vals);
				else
					columnHeaders.put(i, new HashSet<>(vals));
			}
		}
		return columnHeaders;
	}
	
	/*
	 * Retrieves column headers from sample data file
	 */
	private Map<Integer, APopulationAttribute> getColumnSample(
			final IGSSurvey survey, final Set<APopulationAttribute> attributes){
		Map<Integer, APopulationAttribute> columnHeaders = new HashMap<>();
		for(int i = survey.getFirstColumnIndex(); i <= survey.getLastColumnIndex(); i++){
			List<String> columnAtt = survey.readLines(0, survey.getFirstRowIndex(), i);
			Set<APopulationAttribute> attSet = attributes.stream()
					.filter(att -> columnAtt.stream().anyMatch(s -> att.getAttributeName().equals(s)))
					.collect(Collectors.toSet());
			if(attSet.isEmpty())
				continue;
			if(attSet.size() > 1){
				int row = survey.getFirstRowIndex();
				Optional<APopulationAttribute> opAtt = null;
				do {
					String value = survey.read(row++, i);
					opAtt = attSet.stream().filter(att -> att.getValues()
							.stream().anyMatch(val -> val.getInputStringValue().equals(value)))
							.findAny();
				} while (opAtt.isPresent());
				columnHeaders.put(i, opAtt.get());
			} else {
				columnHeaders.put(i, attSet.iterator().next());
			}
		}
		return columnHeaders;
	}

}
