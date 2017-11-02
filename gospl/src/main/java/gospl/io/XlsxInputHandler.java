/*********************************************************************************************
 *
 * 'XlsxInputHandler.java, in plugin core, is part of the source code of the GAMA modeling and simulation platform. (c)
 * 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gospl.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.metamodel.io.GSSurveyType;

class XlsxInputHandler extends AbstractXlsXlsxInputHandler {

	protected XlsxInputHandler(final String fileName, final int sheetNumber, 
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType)
			throws FileNotFoundException, IOException, InvalidFormatException {
		super(fileName, firstRowDataIndex, firstColumnDataIndex, dataFileType);
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(fileName)));
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));
	}

	protected XlsxInputHandler(final File file, int sheetNumber, 
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType) 
					throws FileNotFoundException, IOException {
		super(file.getPath(), firstRowDataIndex, firstColumnDataIndex, dataFileType);
		try {
			wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(file)));
		} catch (final InvalidFormatException e) {
			throw new RuntimeException(e.getMessage());
		}
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));
	}


	protected XlsxInputHandler(final InputStream surveyIS, int sheetNumber, String filename,
			int firstRowDataIndex, int firstColumnDataIndex, GSSurveyType dataFileType) 
					throws IOException {
		super(filename, firstRowDataIndex, firstColumnDataIndex, dataFileType);
		wb = new XSSFWorkbook(surveyIS);
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));
	}

}
