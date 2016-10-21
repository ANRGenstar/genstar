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

import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.metamodel.GosplEntity;
import gospl.metamodel.GosplPopulation;
import io.data.readers.exception.InvalidFileTypeException;
import io.data.survey.IGSSurvey;
import io.data.survey.configuration.GSSurveyFile;
import io.data.survey.configuration.GSSurveyType;
import io.data.survey.configuration.GosplConfigurationFile;
import io.data.survey.configuration.GosplXmlSerializer;
import io.metamodel.IEntity;
import io.metamodel.IPopulation;
import io.metamodel.attribut.AbstractAttribute;
import io.metamodel.attribut.IAttribute;
import io.metamodel.attribut.value.IValue;
import io.util.data.GSDataParser;
import io.util.data.GSEnumDataType;

public class GosplDistributionFactory {

	private final double EPSILON = Math.pow(10d, -6);

	private final GosplConfigurationFile configuration;
	private final GSDataParser dataParser;

	private Set<AFullNDimensionalMatrix<? extends Number>> distributions;
	private Set<IPopulation> samples;

	public GosplDistributionFactory(Path configurationFilePath) throws FileNotFoundException {
		this.configuration = new GosplXmlSerializer().deserializeGSConfig(configurationFilePath);
		this.dataParser = new GSDataParser();
	}

	/** 
	 * 
	 * Main methods to parse and get control totals from a {@link GSDataFile} file and with the
	 * help of a specified set of {@link AbstractAttribute}
	 * <p>
	 * Method gets all data file from the builder and harmonizes them to one another using line
	 * identifier attributes
	 * 
	 * @return A {@link Set} of {@link INDimensionalMatrix}
	 * @throws InputFileNotSupportedException 
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 * @throws MatrixCoordinateException 
	 * @throws InvalidFileTypeException 
	 */
	public void buildDistributions() throws InvalidFormatException, IOException, MatrixCoordinateException, InvalidFileTypeException {
		this.distributions = new HashSet<>();
		for(GSSurveyFile file : this.configuration.getDataFiles())
			if(!file.getDataFileType().equals(GSSurveyType.Sample))
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
	public INDimensionalMatrix<IAttribute, IValue, Double> collapseDistributions() 
			throws IllegalDistributionCreation, IllegalControlTotalException, MatrixCoordinateException {
		if(distributions.isEmpty())
			throw new IllegalDistributionCreation("To collapse matrices you must build at least one first: see buildDistributions method");
		if(distributions.size() == 1)
			return getFrequency(distributions.iterator().next());
		Set<AFullNDimensionalMatrix<Double>> fullMatrices = new HashSet<>();
		for(AFullNDimensionalMatrix<? extends Number> mat : distributions
				.stream().filter(mat -> mat.getDimensions().stream().allMatch(d -> !d.isRecordAttribute()))
				.collect(Collectors.toSet()))
			fullMatrices.add(getFrequency(mat));
		return new GosplConditionalDistribution(fullMatrices);
	}

	/**
	 * Main methods to parse and get samples cast into population of according type in Gospl. More
	 * precisely, each sample is transposed where each individual in the survey
	 * is a {@link IEntity} in a synthetic {@link IPopulation}
	 * 
	 * @return
	 * 
	 * TODO: implement sample parser
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 * @throws InvalidFileTypeException 
	 * 
	 */
	public void buildSamples() throws InvalidFormatException, IOException, InvalidFileTypeException {
		samples = new HashSet<>();
		for(GSSurveyFile file : this.configuration.getDataFiles())
			if(file.getDataFileType().equals(GSSurveyType.Sample))
				samples.add(getSample(file, this.configuration.getAttributes()));
	}


	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------- inner utility methods -------------------------- //
	/////////////////////////////////////////////////////////////////////////////////


	/*
	 * Get the distribution matrix from data files
	 */
	private Set<AFullNDimensionalMatrix<? extends Number>> getDistribution(GSSurveyFile file, Set<IAttribute> attributes) 
					throws InvalidFormatException, IOException, MatrixCoordinateException, InvalidFileTypeException {
		Set<AFullNDimensionalMatrix<? extends Number>> cTableSet = new HashSet<>();
		//Load survey
		IGSSurvey survey = file.getSurvey();

		//Read headers and store possible variables by line index
		Map<Integer, Set<IValue>> rowHeaders = getRowHeaders(file, survey, attributes);
		//Read headers and store possible variables by column index
		Map<Integer, Set<IValue>> columnHeaders = getColumnHeaders(file, survey, attributes);

		//Store column related attributes while keeping unrelated attributes separated
		Set<Set<IAttribute>> columnSchemas = columnHeaders.values()
				.stream().map(head -> head
						.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		//Store line related attributes while keeping unrelated attributes separated
		Set<Set<IAttribute>> rowSchemas = rowHeaders.values()
				.stream().map(line -> line
						.stream().map(v -> v.getAttribute()).collect(Collectors.toSet()))
				.collect(Collectors.toSet());

		//Start iterating over each related set of attribute
		for(Set<IAttribute> rSchema : rowSchemas){
			for(Set<IAttribute> cSchema : columnSchemas){
				//Create a matrix for each set of related attribute
				AFullNDimensionalMatrix<? extends Number> jDistribution;
				//Matrix 'dimension / aspect' map
				Map<IAttribute, Set<IValue>> dimTable = Stream.concat(rSchema.stream(), cSchema.stream())
						.collect(Collectors.toMap(a -> a, a -> a.getValues()));
				//Instantiate either contingency (int and global frame of reference) or frequency (double and either global or local frame of reference) matrix
				if(file.getDataFileType().equals(GSSurveyType.ContingencyTable))
					jDistribution = new GosplContingencyTable(dimTable);
				else
					jDistribution = new GosplJointDistribution(dimTable, file.getDataFileType());
				//Fill in the matrix through line & column
				for(Integer row : rowHeaders.entrySet()
						.stream().filter(e -> e.getValue()
								.stream().allMatch(v -> rSchema.contains(v.getAttribute())))
						.map(e -> e.getKey()).collect(Collectors.toSet())){
					for(Integer col : columnHeaders.entrySet()
							.stream().filter(e -> e.getValue()
									.stream().allMatch(v -> cSchema.contains(v.getAttribute())))
							.map(e -> e.getKey()).collect(Collectors.toSet())){
						//The value
						String stringVal = survey.read(row, col);
						//Value type
						GSEnumDataType dt = dataParser.getValueType(stringVal);
						//Store coordinate for the value. It is made of all line & column attribute's aspects
						Set<IValue> coordSet = Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream()).collect(Collectors.toSet());
						ACoordinate<IAttribute, IValue> coord = new GosplCoordinate(coordSet);
						//Add the coordinate / parsed value pair into the matrix
						if(dt == GSEnumDataType.Integer || dt == GSEnumDataType.Double)
							if(!jDistribution.addValue(coord, jDistribution.parseVal(dataParser, stringVal)))
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
	private AFullNDimensionalMatrix<Double> getFrequency(AFullNDimensionalMatrix<? extends Number> matrix) throws IllegalControlTotalException, MatrixCoordinateException {
		// returned matrix
		AFullNDimensionalMatrix<Double> freqMatrix = null;

		if(matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)){
			// Identify local referent dimension
			Map<IAttribute, List<AControl<? extends Number>>> mappedControls = matrix.getDimensions()
					.stream().collect(Collectors.toMap(d -> d, d -> d.getValues()
							.parallelStream().map(a -> matrix.getVal(a)).collect(Collectors.toList())));
			Set<IAttribute> localReferentDimensions = mappedControls.entrySet()
					.parallelStream().filter(e -> e.getValue().stream().allMatch(ac -> ac.equalsCastedVal(e.getValue().get(0), EPSILON)))
					.map(e -> e.getKey()).collect(Collectors.toSet());

			// The most appropriate align referent matrix (the one that have most information about matrix to align, i.e. the highest number of shared dimensions)
			Optional<AFullNDimensionalMatrix<? extends Number>> optionalRef = distributions
					.stream().filter(ctFitter -> !ctFitter.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable)
							&& localReferentDimensions.stream().allMatch(d -> ctFitter.getDimensions().contains(d)))
					.sorted((jd1, jd2) -> (int) jd2.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d)).count() 
							- (int) jd1.getDimensions().stream().filter(d -> matrix.getDimensions().contains(d)).count())
					.findFirst();
			if(optionalRef.isPresent()){
				freqMatrix = new GosplJointDistribution(matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues())),
						GSSurveyType.GlobalFrequencyTable);
				AFullNDimensionalMatrix<? extends Number> matrixOfReference = optionalRef.get();
				for(ACoordinate<IAttribute, IValue> controlKey : matrix.getMatrix().keySet()){
					freqMatrix.addValue(controlKey, new ControlFrequency(matrix.getVal(controlKey).getRowProduct(matrixOfReference.getVal(controlKey.values()
							.stream().filter(asp -> matrixOfReference.getDimensions()
									.contains(asp.getAttribute())).collect(Collectors.toSet()))).doubleValue()));
				}
			} else
				throw new IllegalControlTotalException("The matrix ("+matrix.hashCode()+") must be align to globale frequency table but lack of a referent matrix", matrix);
		} else {
			// Init output matrix
			freqMatrix = new GosplJointDistribution(matrix.getDimensions().stream().collect(Collectors.toMap(d -> d, d -> d.getValues())),
					GSSurveyType.GlobalFrequencyTable);

			if(matrix.getMetaDataType().equals(GSSurveyType.GlobalFrequencyTable)){
				for(ACoordinate<IAttribute, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue()));
			} else {
				List<IAttribute> attributes = new ArrayList<>(matrix.getDimensions());
				Collections.shuffle(attributes);
				AControl<? extends Number> total = matrix.getVal(attributes.remove(0).getValues());
				for(IAttribute attribut : attributes){
					AControl<? extends Number> controlAtt = matrix.getVal(attribut.getValues());
					if(Math.abs(controlAtt.getValue().doubleValue() - total.getValue().doubleValue()) / controlAtt.getValue().doubleValue() > this.EPSILON)
						throw new IllegalControlTotalException(total, controlAtt);
				}
				for(ACoordinate<IAttribute, IValue> coord : matrix.getMatrix().keySet())
					freqMatrix.addValue(coord, new ControlFrequency(matrix.getVal(coord).getValue().doubleValue() / total.getValue().doubleValue()));
			}
		}
		return freqMatrix;
	}

	private IPopulation getSample(GSSurveyFile file, Set<IAttribute> attributes) throws InvalidFormatException, IOException, InvalidFileTypeException {
		IPopulation sampleSet = new GosplPopulation();
		
		IGSSurvey survey = file.getSurvey();
		//Read headers and store possible variables by column index
		Map<Integer, Set<IValue>> columnHeaders = getColumnHeaders(file, survey, attributes);
		
		for(int i = file.getFirstRowDataIndex(); i <= survey.getLastRowIndex(); i++){
			Map<IAttribute, IValue> entityAttributes = new HashMap<>();
			List<String> indiVals = survey.readLine(i);
			for(Integer idx : columnHeaders.keySet())
				entityAttributes.put(columnHeaders.get(idx).iterator().next().getAttribute(), 
						columnHeaders.get(idx).stream().filter(val -> val.getInputStringValue().equals(indiVals.get(idx))).findAny().get());
			sampleSet.add(new GosplEntity(entityAttributes));
		}
		
		return sampleSet;
	}


	///////////////////////////////////////////////////////////////////////
	// -------------------------- back office -------------------------- //
	///////////////////////////////////////////////////////////////////////


	private Map<Integer, Set<IValue>> getRowHeaders(GSSurveyFile file, IGSSurvey survey,
			Set<IAttribute> attributes) {
		List<Integer> attributeIdx = new ArrayList<>();
		for(int line = 0; line < file.getFirstRowDataIndex(); line++){
			List<String> sLine = survey.readLine(line);
			for(int idx = 0; idx < file.getFirstColumnDataIndex(); idx++){
				String headAtt = sLine.get(idx);
				if(attributes.stream().map(att -> att.getName()).anyMatch(attName -> attName.equals(headAtt)))
					attributeIdx.add(idx);
				if(headAtt.isEmpty()){
					List<String> valList = survey.readColumn(idx);
					if(attributes.stream().anyMatch(att -> att.getValues().stream().allMatch(val -> valList.contains(val.getInputStringValue()))))
						attributeIdx.add(idx);
				}
			}
		}

		Map<Integer, Set<IValue>> rowHeaders = new HashMap<>();
		for(int i = file.getFirstRowDataIndex(); i <= survey.getLastRowIndex(); i++){
			List<String> rawLine = survey.readColumns(0, file.getFirstColumnDataIndex(), i);
			List<String> line = attributeIdx.stream().map(idx -> rawLine.get(idx)).collect(Collectors.toList());
			for(int j = 0; j < line.size(); j++){
				String lineVal = line.get(j);
				Set<IValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(lineVal))
						.collect(Collectors.toSet());
				if(vals.isEmpty())
					continue;
				if(vals.size() > 1){
					Set<IAttribute> inferedHeads = new HashSet<>();
					List<String> headList = survey.readLines(0, file.getFirstRowDataIndex(), j);
					if(headList.stream().allMatch(s -> s.isEmpty())) {
						for(List<String> column : survey.readColumns(0, file.getFirstColumnDataIndex()))
							inferedHeads.addAll(attributes
									.stream().filter(a -> a.getValues()
											.stream().allMatch(av -> column.contains(av.getInputStringValue())))
									.collect(Collectors.toSet()));
					} else {
						inferedHeads.addAll(headList
								.stream().flatMap(s -> attributes.stream().filter(a -> a.getName().equals(s)))
								.collect(Collectors.toSet()));
					}
					Set<IValue> vals2 = new HashSet<>(vals);
					for(IValue val : vals2)
						if(!inferedHeads.contains(val.getAttribute()))
							vals.remove(val);
				}
				if(rowHeaders.containsKey(i))
					rowHeaders.get(i).addAll(vals);
				else 
					rowHeaders.put(i, new HashSet<>(vals));				
			}
		}
		return rowHeaders;
	}

	private Map<Integer, Set<IValue>> getColumnHeaders(GSSurveyFile file, IGSSurvey survey, Set<IAttribute> attributes) {
		Map<Integer, Set<IValue>> columnHeaders = new HashMap<>();
		for(int i = file.getFirstColumnDataIndex(); i <= survey.getLastColumnIndex(); i++){
			List<String> column = survey.readLines(0, file.getFirstRowDataIndex(), i);
			for(int j = 0; j < column.size(); j++){
				String columnVal = column.get(j);
				Set<IValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(columnVal))
						.collect(Collectors.toSet());
				if(vals.isEmpty())
					continue;
				if(vals.size() > 1){
					Set<IValue> vals2 = new HashSet<>(vals);
					vals = column.stream().flatMap(s -> attributes
							.stream().filter(att -> att.getName().equals(s)))
							.flatMap(att -> vals2
									.stream().filter(v -> v.getAttribute().equals(att)))
							.collect(Collectors.toSet());
				}
				if(columnHeaders.containsKey(i))
					columnHeaders.get(i).addAll(vals);
				else
					columnHeaders.put(i, new HashSet<>(vals));
			}
		}
		return columnHeaders;
	}

}
