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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.exception.InvalidFileTypeException;
import core.io.survey.IGSSurvey;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.metamodel.GSSurveyFile;
import gospl.metamodel.GSSurveyType;
import gospl.metamodel.GosplEntity;
import gospl.metamodel.GosplPopulation;
import gospl.metamodel.configuration.GosplConfigurationFile;
import gospl.metamodel.configuration.GosplXmlSerializer;

public class GosplDistributionFactory {

	private final double EPSILON = Math.pow(10d, -3);

	private final GosplConfigurationFile configuration;
	private final GSDataParser dataParser;

	private Set<AFullNDimensionalMatrix<? extends Number>> distributions;
	private Set<GosplPopulation> samples;

	public GosplDistributionFactory(final Path configurationFilePath) throws FileNotFoundException {
		this.configuration = new GosplXmlSerializer().deserializeGSConfig(configurationFilePath);
		this.dataParser = new GSDataParser();
	}

	/**
	 * 
	 * Main methods to parse and get control totals from a {@link GSDataFile} file and with the help of a specified set
	 * of {@link ASurveyAttribute}
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
	public void buildDistributions() throws IOException, InvalidFileTypeException {
		this.distributions = new HashSet<>();
		for (final GSSurveyFile file : this.configuration.getDataFiles())
			if (!file.getDataFileType().equals(GSSurveyType.Sample))
				this.distributions.addAll(getDistribution(file, this.configuration.getAttributes()));
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
	public INDimensionalMatrix<ASurveyAttribute, AValue, Double> collapseDistributions()
			throws IllegalDistributionCreation, IllegalControlTotalException {
		if (distributions.isEmpty())
			throw new IllegalArgumentException(
					"To collapse matrices you must build at least one first: see buildDistributions method");
		if (distributions.size() == 1)
			return getFrequency(distributions.iterator().next());
		final Set<AFullNDimensionalMatrix<Double>> fullMatrices = new HashSet<>();
		for (final AFullNDimensionalMatrix<? extends Number> mat : distributions.stream()
				.filter(mat -> mat.getDimensions().stream().allMatch(d -> !d.isRecordAttribute()))
				.collect(Collectors.toSet()))
			fullMatrices.add(getFrequency(mat));
		return new GosplConditionalDistribution(fullMatrices);
	}

	/**
	 * Main methods to parse and get samples cast into population of according type in Gospl. More precisely, each
	 * sample is transposed where each individual in the survey is a {@link IEntity} in a synthetic {@link IPopulation}
	 * 
	 * @return
	 * 
	 * 		TODO: implement sample parser
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws InvalidFileTypeException
	 * 
	 */
	public void buildSamples() throws IOException, InvalidFileTypeException {
		samples = new HashSet<>();
		for (final GSSurveyFile file : this.configuration.getDataFiles())
			if (file.getDataFileType().equals(GSSurveyType.Sample))
				samples.add(getSample(file, this.configuration.getAttributes()));
	}

	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------- inner utility methods -------------------------- //
	/////////////////////////////////////////////////////////////////////////////////

	/*
	 * Get the distribution matrix from data files
	 */
	private Set<AFullNDimensionalMatrix<? extends Number>> getDistribution(final GSSurveyFile file,
			final Set<ASurveyAttribute> attributes) throws IOException, InvalidFileTypeException {
		final Set<AFullNDimensionalMatrix<? extends Number>> cTableSet = new HashSet<>();
		// Load survey
		final IGSSurvey survey = file.getSurvey();

		// Read headers and store possible variables by line index
		final Map<Integer, Set<AValue>> rowHeaders = getRowHeaders(file, survey, attributes);
		// Read headers and store possible variables by column index
		final Map<Integer, Set<AValue>> columnHeaders = getColumnHeaders(file, survey, attributes);

		// Store column related attributes while keeping unrelated attributes separated
		final Set<Set<ASurveyAttribute>> columnSchemas = columnHeaders.values().stream()
				.map(head -> head.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		// Store line related attributes while keeping unrelated attributes separated
		final Set<Set<ASurveyAttribute>> rowSchemas = rowHeaders.values().stream()
				.map(line -> line.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());

		// Start iterating over each related set of attribute
		for (final Set<ASurveyAttribute> rSchema : rowSchemas) {
			for (final Set<ASurveyAttribute> cSchema : columnSchemas) {
				// Create a matrix for each set of related attribute
				AFullNDimensionalMatrix<? extends Number> jDistribution;
				// Matrix 'dimension / aspect' map
				final Map<ASurveyAttribute, Set<AValue>> dimTable = Stream.concat(rSchema.stream(), cSchema.stream())
						.collect(Collectors.toMap(a -> a, a -> a.getValues()));
				// Instantiate either contingency (int and global frame of reference) or frequency (double and either
				// global or local frame of reference) matrix
				if (file.getDataFileType().equals(GSSurveyType.ContingencyTable))
					jDistribution = new GosplContingencyTable(dimTable);
				else
					jDistribution = new GosplJointDistribution(dimTable, file.getDataFileType());
				// Fill in the matrix through line & column
				for (final Integer row : rowHeaders.entrySet().stream()
						.filter(e -> e.getValue().stream().allMatch(v -> rSchema.contains(v.getAttribute())))
						.map(e -> e.getKey()).collect(Collectors.toSet())) {
					for (final Integer col : columnHeaders.entrySet().stream()
							.filter(e -> e.getValue().stream().allMatch(v -> cSchema.contains(v.getAttribute())))
							.map(e -> e.getKey()).collect(Collectors.toSet())) {
						// The value
						final String stringVal = survey.read(row, col);
						// Value type
						final GSEnumDataType dt = dataParser.getValueType(stringVal);
						// Store coordinate for the value. It is made of all line & column attribute's aspects
						final Set<AValue> coordSet =
								Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream())
										.collect(Collectors.toSet());
						final ACoordinate<ASurveyAttribute, AValue> coord = new GosplCoordinate(coordSet);
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
			final Map<ASurveyAttribute, List<AControl<? extends Number>>> mappedControls =
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues().parallelStream()
							.map(a -> matrix.getVal(a)).collect(Collectors.toList())));
			final ASurveyAttribute localReferentDimension =
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
				final Map<AValue, Double> freqControls =
						localReferentDimension.getValues().stream().collect(Collectors.toMap(lrv -> lrv,
								lrv -> matrixOfReference.getVal(lrv).getValue().doubleValue() / totalControl));

				for (final ACoordinate<ASurveyAttribute, AValue> controlKey : matrix.getMatrix().keySet()) {
					freqMatrix.addValue(controlKey,
							new ControlFrequency(matrix.getVal(controlKey).getValue().doubleValue()
									/ localReferentControl.getValue().doubleValue()
									* freqControls.get(controlKey.getMap().get(localReferentDimension))));
				}
			} else
				throw new IllegalControlTotalException("The matrix (" + matrix.hashCode()
						+ ") must be align to globale frequency table but lack of a referent matrix", matrix);
		} else {
			// Init output matrix
			freqMatrix = new GosplJointDistribution(
					matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues())),
					GSSurveyType.GlobalFrequencyTable);

			if (matrix.getMetaDataType().equals(GSSurveyType.GlobalFrequencyTable)) {
				for (final ACoordinate<ASurveyAttribute, AValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue()));
			} else {
				final List<ASurveyAttribute> attributes = new ArrayList<>(matrix.getDimensions());
				Collections.shuffle(attributes);
				final AControl<? extends Number> total = matrix.getVal(attributes.remove(0).getValues());
				for (final ASurveyAttribute attribut : attributes) {
					final AControl<? extends Number> controlAtt = matrix.getVal(attribut.getValues());
					if (Math.abs(controlAtt.getValue().doubleValue() - total.getValue().doubleValue())
							/ controlAtt.getValue().doubleValue() > this.EPSILON)
						throw new IllegalControlTotalException(total, controlAtt);
				}
				for (final ACoordinate<ASurveyAttribute, AValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(
							matrix.getVal(coord).getValue().doubleValue() / total.getValue().doubleValue()));
			}
		}
		return freqMatrix;
	}

	private GosplPopulation getSample(final GSSurveyFile file, final Set<ASurveyAttribute> attributes)
			throws IOException, InvalidFileTypeException {
		final GosplPopulation sampleSet = new GosplPopulation();

		final IGSSurvey survey = file.getSurvey();
		// Read headers and store possible variables by column index
		final Map<Integer, Set<AValue>> columnHeaders = getColumnHeaders(file, survey, attributes);

		for (int i = file.getFirstRowDataIndex(); i <= survey.getLastRowIndex(); i++) {
			final Map<ASurveyAttribute, AValue> entityAttributes = new HashMap<>();
			final List<String> indiVals = survey.readLine(i);
			for (final Integer idx : columnHeaders.keySet())
				entityAttributes.put(columnHeaders.get(idx).iterator().next().getAttribute(), columnHeaders.get(idx)
						.stream().filter(val -> val.getInputStringValue().equals(indiVals.get(idx))).findAny().get());
			sampleSet.add(new GosplEntity(entityAttributes));
		}

		return sampleSet;
	}

	///////////////////////////////////////////////////////////////////////
	// -------------------------- back office -------------------------- //
	///////////////////////////////////////////////////////////////////////

	private Map<Integer, Set<AValue>> getRowHeaders(final GSSurveyFile file, final IGSSurvey survey,
			final Set<ASurveyAttribute> attributes) {
		final List<Integer> attributeIdx = new ArrayList<>();
		for (int line = 0; line < file.getFirstRowDataIndex(); line++) {
			final List<String> sLine = survey.readLine(line);
			for (int idx = 0; idx < file.getFirstColumnDataIndex(); idx++) {
				final String headAtt = sLine.get(idx);
				if (attributes.stream().map(att -> att.getAttributeName()).anyMatch(attName -> attName.equals(headAtt)))
					attributeIdx.add(idx);
				if (headAtt.isEmpty()) {
					final List<String> valList = survey.readColumn(idx);
					if (attributes.stream().anyMatch(att -> att.getValues().stream()
							.allMatch(val -> valList.contains(val.getInputStringValue()))))
						attributeIdx.add(idx);
				}
			}
		}

		final Map<Integer, Set<AValue>> rowHeaders = new HashMap<>();
		for (int i = file.getFirstRowDataIndex(); i <= survey.getLastRowIndex(); i++) {
			final List<String> rawLine = survey.readColumns(0, file.getFirstColumnDataIndex(), i);
			final List<String> line = attributeIdx.stream().map(idx -> rawLine.get(idx)).collect(Collectors.toList());
			for (int j = 0; j < line.size(); j++) {
				final String lineVal = line.get(j);
				final Set<AValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(lineVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<ASurveyAttribute> inferedHeads = new HashSet<>();
					final List<String> headList = survey.readLines(0, file.getFirstRowDataIndex(), j);
					if (headList.stream().allMatch(s -> s.isEmpty())) {
						for (final List<String> column : survey.readColumns(0, file.getFirstColumnDataIndex()))
							inferedHeads.addAll(attributes.stream()
									.filter(a -> a.getValues().stream()
											.allMatch(av -> column.contains(av.getInputStringValue())))
									.collect(Collectors.toSet()));
					} else {
						inferedHeads.addAll(headList.stream()
								.flatMap(s -> attributes.stream().filter(a -> a.getAttributeName().equals(s)))
								.collect(Collectors.toSet()));
					}
					final Set<AValue> vals2 = new HashSet<>(vals);
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

	private Map<Integer, Set<AValue>> getColumnHeaders(final GSSurveyFile file, final IGSSurvey survey,
			final Set<ASurveyAttribute> attributes) {
		final Map<Integer, Set<AValue>> columnHeaders = new HashMap<>();
		for (int i = file.getFirstColumnDataIndex(); i <= survey.getLastColumnIndex(); i++) {
			final List<String> column = survey.readLines(0, file.getFirstRowDataIndex(), i);
			for (int j = 0; j < column.size(); j++) {
				final String columnVal = column.get(j);
				Set<AValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(columnVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<AValue> vals2 = new HashSet<>(vals);
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

}
