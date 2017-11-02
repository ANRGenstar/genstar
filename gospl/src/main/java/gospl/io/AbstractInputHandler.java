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

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;

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
	public Map<Integer, Set<IValue>> getColumnHeaders(Set<DemographicAttribute<? extends IValue>> attributes) {
		
		final Map<Integer, Set<IValue>> columnHeaders = new HashMap<>();
		
		for (int i = getFirstColumnIndex(); i <= getLastColumnIndex(); i++) {
			final List<String> column = readLines(0, getFirstRowIndex(), i);
			for (String columnVal : column) {
				Set<IValue> vals = attributes.stream().flatMap(att -> att.getValueSpace().stream())
						.filter(asp -> asp.getStringValue().equals(columnVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<IValue> vals2 = new HashSet<>(vals);
					vals = column.stream()
							.flatMap(s -> attributes.stream().filter(att -> att.getAttributeName().equals(s)))
							.flatMap(att -> vals2.stream().filter(v -> v.getValueSpace().getAttribute().equals(att)))
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
	public Map<Integer, Set<IValue>> getRowHeaders(Set<DemographicAttribute<? extends IValue>> attributes) {
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
					if (attributes.stream().anyMatch(att -> att.getValueSpace().stream()
							.allMatch(val -> valList.contains(val.getStringValue()))))
						attributeIdx.add(idx);
				}
			}
		}

		final Map<Integer, Set<IValue>> rowHeaders = new HashMap<>();
		for (int i = getFirstRowIndex(); i <= getLastRowIndex(); i++) {
			final List<String> rawLine = readColumns(0, getFirstColumnIndex(), i);
			final List<String> line = attributeIdx.stream().map(idx -> rawLine.get(idx)).collect(Collectors.toList());
			for (int j = 0; j < line.size(); j++) {
				final String lineVal = line.get(j);
				final Set<IValue> vals = attributes.stream().flatMap(att -> att.getValueSpace().stream())
						.filter(asp -> asp.getStringValue().equals(lineVal)).collect(Collectors.toSet());
				if (vals.isEmpty())
					continue;
				if (vals.size() > 1) {
					final Set<DemographicAttribute<? extends IValue>> inferedHeads = new HashSet<>();
					final List<String> headList = readLines(0, getFirstRowIndex(), j);
					if (headList.stream().allMatch(s -> s.isEmpty())) {
						for (final List<String> column : readColumns(0, getFirstColumnIndex()))
							inferedHeads.addAll(attributes.stream()
									.filter(a -> a.getValueSpace().stream()
											.allMatch(av -> column.contains(av.getStringValue())))
									.collect(Collectors.toSet()));
					} else {
						inferedHeads.addAll(headList.stream()
								.flatMap(s -> attributes.stream().filter(a -> a.getAttributeName().equals(s)))
								.collect(Collectors.toSet()));
					}
					final Set<IValue> vals2 = new HashSet<>(vals);
					for (final IValue val : vals2)
						if (!inferedHeads.contains(val.getValueSpace().getAttribute()))
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
	public Map<Integer, DemographicAttribute<? extends IValue>> getColumnSample(Set<DemographicAttribute<? extends IValue>> attributes) {
		
		Map<Integer, DemographicAttribute<? extends IValue>> columnHeaders = new HashMap<>();
		
		for(int i = getFirstColumnIndex(); i <= getLastColumnIndex(); i++){
			List<String> columnAtt = readLines(0, getFirstRowIndex(), i);
			Set<DemographicAttribute<? extends IValue>> attSet = attributes.stream()
					.filter(att -> columnAtt.stream().anyMatch(s -> att.getAttributeName().equals(s)))
					.collect(Collectors.toSet());
			if(attSet.isEmpty())
				continue;
			if(attSet.size() > 1){
				int row = getFirstRowIndex();
				Optional<DemographicAttribute<? extends IValue>> opAtt = null;
				do {
					String value = read(row++, i);
					opAtt = attSet.stream().filter(att -> att.getValueSpace()
							.stream().anyMatch(val -> val.getStringValue().equals(value)))
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
