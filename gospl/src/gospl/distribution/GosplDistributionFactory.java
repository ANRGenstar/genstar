package gospl.distribution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.metamodel.IEntity;
import gospl.metamodel.IPopulation;
import gospl.metamodel.attribut.AbstractAttribute;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.survey.GosplConfigurationFile;
import gospl.survey.GosplMetatDataType;
import gospl.survey.adapter.GosplDataFile;
import gospl.survey.adapter.GosplXmlSerializer;
import io.data.GSDataParser;
import io.data.GSDataType;
import io.datareaders.surveyreader.IGSSurvey;

public class GosplDistributionFactory {

	private final GosplConfigurationFile configuration;
	private final GSDataParser dataParser;
	
	private Set<INDimensionalMatrix<IAttribute, IValue, ? extends Number>> distributions;
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
	 */
	public void buildDistributions() throws InvalidFormatException, IOException, MatrixCoordinateException {
		this.distributions = new HashSet<>();
		for(GosplDataFile file : this.configuration.getDataFiles())
			if(!file.getDataFileType().equals(GosplMetatDataType.Sample))
				this.distributions.addAll(getDistribution(file, this.configuration.getAttributes()));
	}

	public INDimensionalMatrix<IAttribute, IValue, Double> collapseDistributions() {
		INDimensionalMatrix<IAttribute, IValue, Double> matrix = null;
		
		// TODO: collapse all matrix 
		
		return matrix;
	}

	/**
	 * Main methods to parse and get samples cast into population of according type in Gospl. More
	 * precisely, each sample is transposed where each individual in the survey
	 * is a {@link IEntity} in a synthetic {@link IPopulation}
	 * 
	 * @return
	 * 
	 * TODO: implement sample parser
	 * 
	 */
	public void buildSamples() {
		samples = new HashSet<>();
		for(GosplDataFile file : this.configuration.getDataFiles())
			if(file.getDataFileType().equals(GosplMetatDataType.Sample))
				samples.add(getSample(file, this.configuration.getAttributes()));
	}


	/////////////////////////////////////////////////////////////////////////////////
	// -------------------------- inner utility methods -------------------------- //
	/////////////////////////////////////////////////////////////////////////////////


	private Set<INDimensionalMatrix<IAttribute, IValue, ? extends Number>> getDistribution(
			GosplDataFile file, Set<IAttribute> attributes) throws InvalidFormatException, IOException, MatrixCoordinateException {
		Set<INDimensionalMatrix<IAttribute, IValue, ? extends Number>> cTableSet = new HashSet<>();
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
				INDimensionalMatrix<IAttribute, IValue, ? extends Number> jDistribution;
				//Matrix 'dimension / aspect' map
				Map<IAttribute, Set<IValue>> dimTable = Stream.concat(rSchema.stream(), cSchema.stream())
						.collect(Collectors.toMap(a -> a, a -> a.getValues()));
				//Instantiate either contingency (int and global frame of reference) or frequency (double and either global or local frame of reference) matrix
				if(file.getDataFileType().equals(GosplMetatDataType.ContingencyTable))
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
						GSDataType dt = dataParser.getValueType(stringVal);
						//Store coordinate for the value. It is made of all line & column attribute's aspects
						Set<IValue> coordSet = Stream.concat(rowHeaders.get(row).stream(), columnHeaders.get(col).stream()).collect(Collectors.toSet());
						ACoordinate<IAttribute, IValue> coord = new GosplCoordinate(coordSet);
						//Add the coordinate / parsed value pair into the matrix
						if(dt == GSDataType.Integer || dt == GSDataType.Double)
							if(!jDistribution.addValue(coord, jDistribution.parseVal(dataParser, stringVal)))
								jDistribution.getVal(coord).add(jDistribution.parseVal(dataParser, stringVal));
					}
				}
				cTableSet.add(jDistribution);
			}
		}
		return cTableSet;
	}

	private IPopulation getSample(GosplDataFile file, Set<IAttribute> attributes) {
		// TODO Auto-generated method stub
		return null;
	}


	///////////////////////////////////////////////////////////////////////
	// -------------------------- back office -------------------------- //
	///////////////////////////////////////////////////////////////////////


	private Map<Integer, Set<IValue>> getColumnHeaders(GosplDataFile file, IGSSurvey survey,
			Set<IAttribute> attributes) {
		List<Integer> attributeIdx = new ArrayList<>();
		for(int line = 0; line < file.getFirstRowDataIndex(); line++){
			List<String> sLine = survey.readLine(line);
			for(int idx = 0; idx < file.getFirstColumnDataIndex(); idx++){
				String headAtt = sLine.get(idx);
				if(attributes.stream().map(att -> att.getName()).anyMatch(attName -> attName.equals(headAtt)))
					attributeIdx.add(idx);
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
					vals = vals.stream().filter(av -> inferedHeads.contains(av.getAttribute())).collect(Collectors.toSet());
				}
				if(rowHeaders.containsKey(i))
					rowHeaders.get(i).addAll(vals);
				else 
					rowHeaders.put(i, new HashSet<>(vals));				
			}
		}
		return rowHeaders;
	}

	private Map<Integer, Set<IValue>> getRowHeaders(GosplDataFile file, IGSSurvey survey, Set<IAttribute> attributes) {
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
