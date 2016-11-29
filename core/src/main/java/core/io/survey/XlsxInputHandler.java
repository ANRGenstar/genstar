/*********************************************************************************************
 *
 * 'XlsxInputHandler.java, in plugin core, is part of the source code of the GAMA modeling and simulation platform. (c)
 * 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package core.io.survey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class XlsxInputHandler extends AbstractXlsXlsxInputHandler {

	protected XlsxInputHandler(final String fileName)
			throws FileNotFoundException, IOException, InvalidFormatException {
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(fileName)));
		this.setCurrentSheet(wb.getSheetAt(0));
	}

	protected XlsxInputHandler(final File file) throws FileNotFoundException, IOException {
		try {
			wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(file)));
		} catch (final InvalidFormatException e) {
			throw new RuntimeException(e.getMessage());
		}
		this.setCurrentSheet(wb.getSheetAt(0));
	}

	protected XlsxInputHandler(final String fileName, final int sheetNumber)
			throws FileNotFoundException, IOException, InvalidFormatException {
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(fileName)));
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));
	}

	protected XlsxInputHandler(final InputStream surveyIS) throws IOException {
		wb = new XSSFWorkbook(surveyIS);
		this.setCurrentSheet(wb.getSheetAt(0));
	}

}
