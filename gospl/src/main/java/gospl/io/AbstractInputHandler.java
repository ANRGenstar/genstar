package gospl.io;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.IGSSurvey;

/**
 * Abstraction for any input handler: able to store the path to the file its based on, 
 * and the nature of data stored there. Also provides basic algos for detecting row and 
 * column headers, suitable for tabular files.
 * 
 * @author Samuel Thiriot
 * @author Kevin Chapuis
 * 
 */
public abstract class AbstractInputHandler implements IGSSurvey {

	protected final String surveyCompleteFile;
	protected final String surveyFileName;
	protected final String surveyFilePath;
	protected final GSSurveyType dataFileType;

	
	public AbstractInputHandler(GSSurveyType dataFileType, String fileName) {

		this.dataFileType = dataFileType;
		
		this.surveyCompleteFile = fileName;
		this.surveyFileName = Paths.get(fileName).getFileName().toString();
		this.surveyFilePath = Paths.get(fileName).toAbsolutePath().toString();
		
	}
	
	public AbstractInputHandler(GSSurveyType dataFileType, File file) {

		this.dataFileType = dataFileType;
		
		this.surveyCompleteFile = file.getAbsolutePath();
		this.surveyFileName = file.getName();
		this.surveyFilePath = file.getAbsolutePath();
		
	}
	
	/**
	 * The default implementation tries to read the first line, and infer the corresponding values. 
	 * It is valid as long as the content includes the title line. Else inherited classes should
	 * override it.
	 * 
	 * @return returns for each column id the list of attributes values
	 */
	@Override
	public Map<Integer, Set<APopulationValue>> getColumnHeaders(Set<APopulationAttribute> attributes) {
		
		final Map<Integer, Set<APopulationValue>> columnHeaders = new HashMap<>();
		
		for (int i = getFirstColumnIndex(); i <= getLastColumnIndex(); i++) {
			final List<String> column = readLines(0, getFirstRowIndex(), i);
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
	
	@Override
	/**
	 * Default implementation for tabular data. Override if not suitable for another file format.
	 */
	public Map<Integer, Set<APopulationValue>> getRowHeaders(Set<APopulationAttribute> attributes) {
		final List<Integer> attributeIdx = new ArrayList<>();
		for (int line = 0; line < getFirstRowIndex(); line++) {
			final List<String> sLine = readLine(line);
			for (int idx = 0; idx < getFirstColumnIndex(); idx++) {
				final String headAtt = sLine.get(idx);
				if (attributes.stream().map(att -> att.getAttributeName())
						.anyMatch(attName -> attName.equals(headAtt)))
					attributeIdx.add(idx);
				if (headAtt.isEmpty()) {
					final List<String> valList = readColumn(idx);
					if (attributes.stream().anyMatch(att -> att.getValues().stream()
							.allMatch(val -> valList.contains(val.getInputStringValue()))))
						attributeIdx.add(idx);
				}
			}
		}

		final Map<Integer, Set<APopulationValue>> rowHeaders = new HashMap<>();
		for (int i = getFirstRowIndex(); i <= getLastRowIndex(); i++) {
			final List<String> rawLine = readColumns(0, getFirstColumnIndex(), i);
			final List<String> line = attributeIdx.stream().map(idx -> rawLine.get(idx)).collect(Collectors.toList());
			for (int j = 0; j < line.size(); j++) {
				final String lineVal = line.get(j);
				final Set<APopulationValue> vals = attributes.stream().flatMap(att -> att.getValues().stream())
						.filter(asp -> asp.getInputStringValue().equals(lineVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<APopulationAttribute> inferedHeads = new HashSet<>();
					final List<String> headList = readLines(0, getFirstRowIndex(), j);
					if (headList.stream().allMatch(s -> s.isEmpty())) {
						for (final List<String> column : readColumns(0, getFirstColumnIndex()))
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
	
	@Override
	public Map<Integer, APopulationAttribute> getColumnSample(Set<APopulationAttribute> attributes) {
		
		Map<Integer, APopulationAttribute> columnHeaders = new HashMap<>();
		
		for(int i = getFirstColumnIndex(); i <= getLastColumnIndex(); i++){
			List<String> columnAtt = readLines(0, getFirstRowIndex(), i);
			Set<APopulationAttribute> attSet = attributes.stream()
					.filter(att -> columnAtt.stream().anyMatch(s -> att.getAttributeName().equals(s)))
					.collect(Collectors.toSet());
			if(attSet.isEmpty())
				continue;
			if(attSet.size() > 1){
				int row = getFirstRowIndex();
				Optional<APopulationAttribute> opAtt = null;
				do {
					String value = read(row++, i);
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
	
	@Override
	public final GSSurveyType getDataFileType() {
		return this.dataFileType;
	}

	@Override
	public String getSurveyFilePath() {
		return surveyFilePath;
	}

}