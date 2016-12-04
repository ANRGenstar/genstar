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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.IGSSurvey;
import gospl.io.exception.InvalidSurveyFormatException;

/**
 * Factory to setup data input into gospl generator
 * 
 * @author kevinchapuis
 *
 */
public class SurveyFactory {

	private char DEFAULT_SEPARATOR = ',';
	private int DEFAULT_SHEET_NB = 0;
	private int FIRST_ROW_DATA = 1;
	private int FIRST_COLUMN_DATA = 1;

	private static final String CSV_EXT = ".csv";
	private static final String XLS_EXT = ".xls";
	private static final String XLSX_EXT = ".xlsx";

	private final List<String> supportedFileFormat;

	public SurveyFactory() {
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
	public SurveyFactory(final int sheetNn, final char csvSeparator,
			int firstRowDataIndex, int firstColumnDataIndex) {
		this();
		this.DEFAULT_SHEET_NB = sheetNn;
		this.DEFAULT_SEPARATOR = csvSeparator;
		this.FIRST_ROW_DATA = firstRowDataIndex;
		this.FIRST_COLUMN_DATA = firstColumnDataIndex;
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
		return this.getSurvey(filepath, DEFAULT_SHEET_NB, DEFAULT_SEPARATOR, 
				FIRST_ROW_DATA, FIRST_COLUMN_DATA, dataFileType);
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
		return this.getSurvey(file, DEFAULT_SHEET_NB, DEFAULT_SEPARATOR, 
				FIRST_ROW_DATA, FIRST_COLUMN_DATA, dataFileType);
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
		return this.getSurvey(fileName, surveyIS, DEFAULT_SHEET_NB, DEFAULT_SEPARATOR, 
				FIRST_ROW_DATA, FIRST_COLUMN_DATA, dataFileType);
	}
	
	// ----------------------------------------------------------------------- //
	// ---------------------- POPULATION EXPORT SECTION ---------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * TODO: javadoc
	 * 
	 * @param surveyFile
	 * @param population
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException 
	 * @throws InvalidFileTypeException
	 */
	public IGSSurvey createSurvey(File surveyFile, 
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) 
					throws IOException, InvalidSurveyFormatException, InvalidFormatException{
		try {
			int individual = 1;
			final BufferedWriter bw = Files.newBufferedWriter(surveyFile.toPath());
			final Collection<APopulationAttribute> attributes = population.getPopulationAttributes();
			bw.write("Individual" + DEFAULT_SEPARATOR
					+ attributes.stream().map(att -> att.getAttributeName()).collect(Collectors.joining(String.valueOf(DEFAULT_SEPARATOR)))
					+ "\n");
			for (final APopulationEntity e : population) {
				bw.write(String.valueOf(individual++));
				for (final APopulationAttribute attribute : attributes)
					bw.write(DEFAULT_SEPARATOR + e.getValueForAttribute(attribute).getStringValue());
				bw.write("\n");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		return this.getSurvey(surveyFile, GSSurveyType.Sample);
	}

}
