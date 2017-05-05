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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.GSSurveyWrapper;
import core.metamodel.pop.io.IGSSurvey;
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
	private DecimalFormat decimalFormat = new DecimalFormat("#.##");

	private char separator = GSSurveyWrapper.DEFAULT_SEPARATOR;
	private int sheetNb = GSSurveyWrapper.DEFAULT_SHEET_NB;
	private int firstRowDataIdx = GSSurveyWrapper.FIRST_ROW_DATA;
	private int firstColumnDataIdx = GSSurveyWrapper.FIRST_COLUMN_DATA;

	private static final String CSV_EXT = ".csv";
	private static final String XLS_EXT = ".xls";
	private static final String XLSX_EXT = ".xlsx";

	private final List<String> supportedFileFormat;

	public GosplSurveyFactory() {
		supportedFileFormat = Arrays.asList(CSV_EXT, XLS_EXT, XLSX_EXT);
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
		this();
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
		return this.getSurvey(fileName, surveyIS, sheetNb, separator, 
				firstRowDataIdx, firstColumnDataIdx, dataFileType);
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
	public IGSSurvey createSurvey(File surveyFile, GSSurveyType surveyType,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) 
					throws InvalidFormatException, IOException, InvalidSurveyFormatException{
		switch (surveyType) {
		case Sample:
			return createSample(surveyFile, population);
		case ContingencyTable:
			return createTable(surveyFile, surveyType, population);
		case GlobalFrequencyTable:
			return createTable(surveyFile, surveyType, population);
		default:
			return createTable(surveyFile, GSSurveyType.GlobalFrequencyTable, population);
		}
	}
	
	// ---------------------- inner methods ---------------------- // 

	private IGSSurvey createTable(File surveyFile, GSSurveyType surveyType,
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) throws IOException, InvalidFormatException, InvalidSurveyFormatException {
		Set<APopulationAttribute> attributes = population.getPopulationAttributes();
		String report = attributes.stream().map(att -> att.getAttributeName() + separator + "frequence")
				.collect(Collectors.joining(String.valueOf(separator)))+"\n";
		List<String> lines = IntStream.range(0, attributes.stream().mapToInt(att -> att.getValues().size()+1).max().getAsInt())
				.mapToObj(i -> "").collect(Collectors.toList());
		
		Map<APopulationValue, Integer> mapReport = attributes.stream().flatMap(att -> 
					Stream.concat(att.getValues().stream(), Stream.of(att.getEmptyValue())))
				.collect(Collectors.toMap(Function.identity(), value -> 0)); 
		population.stream().forEach(entity -> entity.getValues()
				.forEach(eValue -> mapReport.put(eValue, mapReport.get(eValue)+1)));
		
		for(APopulationAttribute attribute : attributes){
			int lineNumber = 0;
			Set<APopulationValue> attValues = Stream.concat(attribute.getValues().stream(), 
					Stream.of(attribute.getEmptyValue())).collect(Collectors.toSet());
			for(APopulationValue value : attValues){
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
				lines.set(i, lines.get(i)
						.concat(lines.get(i).isEmpty() ? "" : separator + "") + separator + "");
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
		bw.write("Individual");
		bw.write(separator);
		bw.write(attributes.stream().map(att -> att.getAttributeName()).collect(Collectors.joining(String.valueOf(separator))));
		bw.write("\n");
		for (final APopulationEntity e : population) {
			bw.write(String.valueOf(individual++));
			for (final APopulationAttribute attribute : attributes) {
				bw.write(separator);
				try {
					bw.write(e.getValueForAttribute(attribute).getStringValue());
				} catch (NullPointerException e2) {
					bw.write("???");
				}
			}
			bw.write("\n");
		}
		bw.flush();
		return this.getSurvey(surveyFile, GSSurveyType.Sample);
	}

}
