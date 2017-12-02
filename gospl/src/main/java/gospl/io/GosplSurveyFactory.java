/*********************************************************************************************
 *
 * 'SurveyFactory.java, in plugin core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gospl.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.metamodel.pop.io.IGSSurvey;
import core.util.GSPerformanceUtil;
import core.util.data.GSEnumDataType;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.io.exception.InvalidSurveyFormatException;

/**
 * Factory to setup data input into gospl generator
 * 
 * @author kevinchapuis
 *
 */
public class GosplSurveyFactory {

	@SuppressWarnings("unused")
	private double precision = Math.pow(10, -2);

	private final DecimalFormatSymbols dfs;
	private final DecimalFormat decimalFormat;

	private final char separator;
	private final int sheetNb;
	private final int firstRowDataIdx;
	private final int firstColumnDataIdx;

	public static final String CSV_EXT 	= ".csv";
	public static final String XLS_EXT 	= ".xls";
	public static final String XLSX_EXT = ".xlsx";
	public static final String DBF_EXT 	= ".dbf";


	/**
	 * The list of supported file formats (provided as extensions)
	 */
	public static final List<String> supportedFileFormat = Collections.unmodifiableList(Arrays.asList(
			CSV_EXT, 
			XLS_EXT, 
			XLSX_EXT,
			DBF_EXT
			));

	public GosplSurveyFactory() {
		
		this(
				GSSurveyWrapper.DEFAULT_SHEET_NB, 
				GSSurveyWrapper.DEFAULT_SEPARATOR, 
				GSSurveyWrapper.FIRST_ROW_DATA,
				GSSurveyWrapper.FIRST_COLUMN_DATA,
				new DecimalFormatSymbols(Locale.FRANCE),
				new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.FRANCE))
				);
		
		this.dfs.setDecimalSeparator('.');
	}

	/**
	 * Replace default factory value by explicit ones
	 * 
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 */
	public GosplSurveyFactory(final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex) {
		
		this(
				sheetNn, 
				csvSeparator, 
				firstRowDataIndex,
				firstColumnDataIndex,
				new DecimalFormatSymbols(Locale.FRANCE),
				new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.FRANCE))
				);
	}
	

	/**
	 * Replace default factory value by explicit ones
	 * 
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param locale
	 */
	public GosplSurveyFactory(final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex, Locale locale) {
		
		this(
				sheetNn, 
				csvSeparator, 
				firstRowDataIndex,
				firstColumnDataIndex,
				new DecimalFormatSymbols(locale),
				new DecimalFormat("#.##", new DecimalFormatSymbols(locale))
				);
	}
	
	/**
	 * Replace default factory value by explicit ones
	 * 
	 *
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param decimalFormatSymbols
	 * @param decimalFormat
	 */
	public GosplSurveyFactory(
			final int sheetNn, 
			final char csvSeparator,
			int firstRowDataIndex, 
			int firstColumnDataIndex, 
			DecimalFormatSymbols decimalFormatSymbols, 
			DecimalFormat decimalFormat) {
		
		this.dfs = decimalFormatSymbols;
		this.decimalFormat = decimalFormat;
		this.sheetNb = sheetNn;
		this.separator = csvSeparator;
		this.firstRowDataIdx = firstRowDataIndex;
		this.firstColumnDataIdx = firstColumnDataIndex;
	}

	/**
	 * Gives the list of format (extension) the file this factory can setup input data with
	 * 
	 * @return
	 */
	public List<String> getSupportedFileFormat() {
		return supportedFileFormat;
	}

	// ----------------------------------------------------------------------- //
	// ------------------------- DATA IMPORT SECTION ------------------------- //
	// ----------------------------------------------------------------------- //

	/**
	 * Retrieve a survey from a wrapper (lighter memory version of a survey) 
	 * 
	 * @see GSSurveyWrapper
	 * 
	 * @param wrapper
	 * @param basePath
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(GSSurveyWrapper wrapper, File basePath) 
			throws InvalidFormatException, IOException, InvalidSurveyFormatException {

		File surveyFile = wrapper.getRelativePath().toFile();

		if (!surveyFile.isAbsolute()) {

			if (basePath == null)
				throw new IllegalArgumentException("cannot load relative file "+surveyFile+" if the configuration base path is not defined.");

			surveyFile = new File(basePath.toString()+File.separator+surveyFile.toString());

		}
		return this.getSurvey(surveyFile, wrapper.getSheetNumber(), 
				wrapper.getCsvSeparator(), wrapper.getFirstRowIndex(), wrapper.getFirstColumnIndex(),
				wrapper.getSurveyType());
	}

	/**
	 * TODO: javadoc
	 * 
	 * @param filepath
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param dataFileType
	 * @return
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 * @throws InvalidFormatException
	 */
	public IGSSurvey getSurvey(final String filepath, final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType) 
					throws IOException, InvalidSurveyFormatException, InvalidFormatException {
		
		if (filepath.endsWith(XLSX_EXT))
			return new XlsxInputHandler(filepath, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (filepath.endsWith(XLS_EXT))
			return new XlsInputHandler(filepath, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (filepath.endsWith(CSV_EXT))
			return new CsvInputHandler(filepath, csvSeparator, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (filepath.endsWith(DBF_EXT))
			return new DBaseInputHandler(dataFileType, filepath);
		
		final String[] pathArray = filepath.split(File.separator);
		throw new InvalidSurveyFormatException(pathArray[pathArray.length - 1], supportedFileFormat);
	}
	
	/**
	 * 
	 * @param filepath
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param dataFileType
	 * @param processAsFormat one of the formats supportedFileFormat 
	 * @return
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 * @throws InvalidFormatException
	 */
	public IGSSurvey getSurvey(final String filepath, final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType, 
			String processAsFormat) 
					throws IOException, InvalidSurveyFormatException, InvalidFormatException {
		
		if (processAsFormat.equals(XLSX_EXT))
			return new XlsxInputHandler(filepath, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (processAsFormat.equals(XLS_EXT))
			return new XlsInputHandler(filepath, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (processAsFormat.equals(CSV_EXT))
			return new CsvInputHandler(filepath, csvSeparator, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (processAsFormat.equals(DBF_EXT))
			return new DBaseInputHandler(dataFileType, filepath);
		
		final String[] pathArray = filepath.split(File.separator);
		throw new InvalidSurveyFormatException(pathArray[pathArray.length - 1], supportedFileFormat);
	}

	/**
	 * TODO: javadoc
	 * 
	 * @param filepath
	 * @param dataFileType
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(final String filepath, GSSurveyType dataFileType) 
			throws InvalidFormatException, IOException, InvalidSurveyFormatException{
		return this.getSurvey(filepath, sheetNb, separator, 
				firstRowDataIdx, firstColumnDataIdx, dataFileType);
	}

	/**
	 * TODO: javadoc
	 * 
	 * @param file
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param dataFileType
	 * @return
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(final File file, final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType) 
					throws IOException, InvalidSurveyFormatException {
		if (file.getName().endsWith(XLSX_EXT))
			return new XlsxInputHandler(file, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (file.getName().endsWith(XLS_EXT))
			return new XlsInputHandler(file, sheetNn, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (file.getName().endsWith(CSV_EXT))
			return new CsvInputHandler(file, csvSeparator, firstRowDataIndex, 
					firstColumnDataIndex, dataFileType);
		if (file.getName().endsWith(DBF_EXT))
			return new DBaseInputHandler(dataFileType, file);
		final String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidSurveyFormatException(pathArray[pathArray.length - 1], supportedFileFormat);
	}

	/**
	 * Retriev survey based on default factory parameter
	 * 
	 * @param file
	 * @param dataFileType
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(final File file, GSSurveyType dataFileType) 
			throws IOException, InvalidFormatException, InvalidSurveyFormatException {
		return this.getSurvey(file, sheetNb, separator, 
				firstRowDataIdx, firstColumnDataIdx, dataFileType);
	}

	/**
	 * TODO: javadoc
	 * 
	 * @param fileName
	 * @param surveyIS
	 * @param sheetNn
	 * @param csvSeparator
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param dataFileType
	 * @return
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(final String fileName, final InputStream surveyIS, 
			final int sheetNn, final char csvSeparator, int firstRowDataIndex, 
			int firstColumnDataIndex, GSSurveyType dataFileType) 
					throws IOException, InvalidSurveyFormatException {
		if (fileName.endsWith(XLSX_EXT))
			return new XlsxInputHandler(surveyIS, sheetNn, fileName, 
					firstRowDataIndex, firstColumnDataIndex, dataFileType);
		if (fileName.endsWith(XLS_EXT))
			return new XlsInputHandler(surveyIS, sheetNn, fileName, 
					firstRowDataIndex, firstColumnDataIndex, dataFileType);
		if (fileName.endsWith(CSV_EXT))
			return new CsvInputHandler(fileName, surveyIS, csvSeparator,
					firstRowDataIndex, firstColumnDataIndex, dataFileType);
		if (fileName.endsWith(DBF_EXT))
			throw new IllegalArgumentException("Cannot read format "+DBF_EXT+" from a, input stream, sorry");
		throw new InvalidSurveyFormatException(fileName, supportedFileFormat);
	}
	
	/**
	 * TODO: javadoc
	 * 
	 * @param fileName
	 * @param surveyIS
	 * @param dataFileType
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey getSurvey(final String fileName, final InputStream surveyIS, GSSurveyType dataFileType) 
			throws IOException, InvalidFormatException, InvalidSurveyFormatException {
		return this.getSurvey(fileName, surveyIS, sheetNb, separator, firstRowDataIdx, firstColumnDataIdx, dataFileType);
	}

	// ----------------------------------------------------------------------- //
	// ---------------------- POPULATION EXPORT SECTION ---------------------- //
	// ----------------------------------------------------------------------- //

	/**
	 * Export a population to a file. Each {@link GSSurveyType} will cause the export
	 * to be of the corresponding type; i.e. either a sample of the complete population
	 * or the contingency / frequency of each attribute
	 * <p>
	 * WARNING: make use of parallelism using {@link Stream#parallel()}
	 * 
	 * @param surveyFile
	 * @param surveyType
	 * @param population
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InvalidSurveyFormatException
	 */
	public IGSSurvey createSummary(File surveyFile, GSSurveyType surveyType,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) 
					throws InvalidFormatException, IOException, InvalidSurveyFormatException{
		switch (surveyType) {
		case Sample:
			return createSample(surveyFile, population);
		case ContingencyTable:
			return createTableSummary(surveyFile, surveyType, population);
		case GlobalFrequencyTable:
			return createTableSummary(surveyFile, surveyType, population);
		default:
			return createTableSummary(surveyFile, GSSurveyType.GlobalFrequencyTable, population);
		}
	}

	/**
	 * Export a population to a given file. The output format is a matrix with dimension being
	 * attribute set {@code format} passed as parameter.
	 * 
	 * @param surveyFile
	 * @param format
	 * @param population
	 * @return
	 * @throws IOException 
	 * @throws InvalidSurveyFormatException 
	 * @throws InvalidFormatException 
	 */
	public IGSSurvey createContingencyTable(File surveyFile, Set<APopulationAttribute> format,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) 
					throws IOException, InvalidFormatException, InvalidSurveyFormatException{
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("TEST OUTPUT TABLES", Level.TRACE);
		
		AFullNDimensionalMatrix<Integer> popMatrix = GosplNDimensionalMatrixFactory.getFactory()
				.createContingency(population);
		
		if(format.stream().anyMatch(att -> !popMatrix.getDimensions().contains(att)))
			throw new IllegalArgumentException("Format is not entirely aligned with population: \n"
					+ "Format: "+Arrays.toString(format.toArray())+"\n"
					+ "Population: "+Arrays.toString(popMatrix.getDimensions().toArray()));

		List<APopulationAttribute> columnHeaders = format.stream().skip(format.size()/2)
				.collect(Collectors.toList());
		List<APopulationAttribute> rowHeaders = format.stream()
				.filter(att -> !columnHeaders.contains(att)).collect(Collectors.toList());

		gspu.sysoStempMessage("Columns: "+columnHeaders.stream().map(att -> att.getAttributeName())
				.collect(Collectors.joining(" + ")));
		gspu.sysoStempMessage("Rows: "+rowHeaders.stream().map(att -> att.getAttributeName())
				.collect(Collectors.joining(" + ")));
		
		String report = "";

		if(rowHeaders.isEmpty()){
			List<APopulationValue> vals = columnHeaders.stream().flatMap(att -> att.getValues().stream())
					.collect(Collectors.toList());
			report += vals.stream().map(val -> val.getStringValue())
					.collect(Collectors.joining(String.valueOf(separator)))
					+String.valueOf(separator)+"TOTAL\n";
			report += vals.stream().map(val -> popMatrix.getVal(val, true).getValue().toString())
					.collect(Collectors.joining(String.valueOf(separator)))+String.valueOf(separator)
					+vals.stream().mapToInt(val -> popMatrix.getVal(val, true).getValue()).sum();
		} else {
			Map<Integer, List<APopulationValue>> columnHead = getTableHeader(columnHeaders);
			Map<Integer, List<APopulationValue>> rowHead = getTableHeader(rowHeaders);

			String blankHeadLine = rowHeaders.stream().map(rAtt -> " "+String.valueOf(separator))
					.collect(Collectors.joining());
			report += IntStream.range(0, columnHeaders.size()).mapToObj(index -> blankHeadLine 
							+ columnHead.values().stream().map(col -> col.get(index).getStringValue())
						.collect(Collectors.joining(String.valueOf(separator))))
					.collect(Collectors.joining("\n"))+String.valueOf(separator)+"TOTAL";
			
			gspu.sysoStempMessage("HEAD: "+report);
			gspu.sysoStempMessage("ROW VALUE"+rowHead.keySet().stream().sorted()
					.map(i -> rowHead.get(i).stream().map(val -> val.getStringValue())
							.collect(Collectors.joining(" + ")))
					.collect(Collectors.joining(" // ")));

			for(int rowIdx = 0; rowIdx < rowHead.size(); rowIdx++){
				List<Integer> data = new ArrayList<>();
				for(Integer colIdx : columnHead.keySet())
					data.add(colIdx, popMatrix.getVal(Stream.concat(columnHead.get(colIdx).stream(), 
							rowHead.get(rowIdx).stream()).collect(Collectors.toSet())).getValue());
				report += "\n"+rowHead.get(rowIdx).stream().map(row -> row.getStringValue())
								.collect(Collectors.joining(String.valueOf(separator)))
							+String.valueOf(separator)+data.stream().map(i -> i.toString())
						.collect(Collectors.joining(String.valueOf(separator)));
				report += String.valueOf(separator)+data.stream().reduce(0, (i1,i2) -> i1+i2);
				gspu.sysoStempMessage("New line ("+Arrays.toString(rowHead.get(rowIdx).toArray())
						+") = "+Arrays.toString(data.toArray()));
			}
			
			List<Integer> colTotals = IntStream.range(0, columnHead.size())
				.mapToObj(colIdx -> popMatrix.getVal(columnHead.get(colIdx)).getValue())
				.collect(Collectors.toList());
			report += "\n"+colTotals.stream().map(col -> col.toString())
					.collect(Collectors.joining(String.valueOf(separator)))+String.valueOf(separator)
					+colTotals.stream().reduce(1, (i1,i2) -> i1+i2);			
		}

		Files.write(surveyFile.toPath(), report.getBytes());
		return this.getSurvey(surveyFile, GSSurveyType.ContingencyTable);
	}

	// ---------------------- inner methods ---------------------- // 

	private IGSSurvey createTableSummary(File surveyFile, GSSurveyType surveyType,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) throws IOException, InvalidFormatException, InvalidSurveyFormatException {
		
		Set<APopulationAttribute> attributes = population.getPopulationAttributes();
		
		String report = attributes.stream().map(att -> att.getAttributeName() + separator + "frequence")
				.collect(Collectors.joining(String.valueOf(separator)))+"\n";
		List<String> lines = IntStream.range(0, attributes.stream().mapToInt(att -> att.getValues().size()+1).max().getAsInt())
				.mapToObj(i -> "").collect(Collectors.toList());

		Map<APopulationValue, Integer> mapReport = attributes.stream()
				.flatMap(att -> Stream.concat(att.getValues().stream(), Stream.of(att.getEmptyValue()))
				.collect(Collectors.toSet()).stream())
				.collect(Collectors.toMap(Function.identity(), value -> 0));
		
		population.stream().forEach(entity -> entity.getValues()
				.forEach(eValue -> mapReport.put(eValue, mapReport.get(eValue)+1)));

		for(APopulationAttribute attribute : attributes){
			
			int lineNumber = 0;
			
			Set<APopulationValue> attValues = Stream.concat(attribute.getValues().stream(), 
					Stream.of(attribute.getEmptyValue())).collect(Collectors.toSet());
			
			for(APopulationValue value : attValues) {
				String val = "";
				if(surveyType.equals(GSSurveyType.ContingencyTable))
					val = String.valueOf(mapReport.get(value));
				else
					val = decimalFormat.format(mapReport.get(value).doubleValue() / population.size());
				lines.set(lineNumber, lines.get(lineNumber)
						.concat(lines.get(lineNumber++).isEmpty() ? "" : String.valueOf(separator)) + 
						value.getStringValue() + separator + val);
			}
			
			for(int i = lineNumber; i < lines.size(); i++)
				lines.set(
						i, 
						lines.get(i).concat(lines.get(i).isEmpty() ? "" : separator + "") + separator + ""
						);
		}

		report += String.join("\n", lines);
		Files.write(surveyFile.toPath(), report.getBytes());
		return this.getSurvey(surveyFile, GSSurveyType.GlobalFrequencyTable);
	}

	/**
	 * Create a sample survey from a population: each entity of the population
	 * is depicted with their attribute's values 
	 * 
	 * @param surveyFile
	 * @param population
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException 
	 * @throws InvalidFileTypeException
	 */
	private IGSSurvey createSample(File surveyFile, 
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) 
					throws IOException, InvalidSurveyFormatException, InvalidFormatException{
		
		int individual = 1;
		final BufferedWriter bw = Files.newBufferedWriter(surveyFile.toPath());
		final Collection<APopulationAttribute> attributes = population.getPopulationAttributes();
		bw.write("ID");
		bw.write(separator);
		bw.write(attributes.stream().map(att -> att.getAttributeName()).collect(Collectors.joining(String.valueOf(separator))));
		bw.write("\n");
		bw.write("individual identifier");
		bw.write(separator);
		bw.write(attributes.stream().map(att -> att.getDescription()).collect(Collectors.joining(String.valueOf(separator))));
		bw.write("\n");
		for (final APopulationEntity e : population) {
			bw.write(String.valueOf(individual++));
			for (final APopulationAttribute attribute : attributes) {
				bw.write(separator);
				try {
		
					APopulationValue val = e.getValueForAttribute(attribute); 
					String v = val.getStringValue();
					
					
					if (attribute.getDataType() == GSEnumDataType.String) {
						bw.write("\"");
						bw.write(v);
						bw.write("\"");
					} else {
						bw.write(v);
					}
					
				} catch (NullPointerException e2) {
					bw.write("\"?\"");
				}
			}
			bw.write("\n");
		}
		bw.close();
		return this.getSurvey(surveyFile, GSSurveyType.Sample);
	}

	private Map<Integer, List<APopulationValue>> getTableHeader(Collection<APopulationAttribute> headerAttributes){
		List<APopulationAttribute> hAtt = new ArrayList<>(headerAttributes);
		APopulationAttribute anchor = hAtt.get(0);
		List<List<APopulationValue>> head = anchor.getValues()
				.stream().map(val -> Stream.of(val).collect(Collectors.toList()))
				.collect(Collectors.toList());
		hAtt.remove(anchor);
		for(APopulationAttribute headAtt : hAtt){
			List<List<APopulationValue>> tmpHead = new ArrayList<>();
			for(List<APopulationValue> currentHead : head)
				tmpHead.addAll(headAtt.getValues().stream()
						.map(val -> Stream.concat(currentHead.stream(), Stream.of(val))
								.collect(Collectors.toList())).collect(Collectors.toList()));
			head = tmpHead;
		}
		final List<List<APopulationValue>> headFinal = head;
		return head.stream().collect(Collectors.toMap(h -> headFinal.indexOf(h), Function.identity()));
	}

}
