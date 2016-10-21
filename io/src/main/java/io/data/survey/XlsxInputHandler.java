package io.data.survey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class XlsxInputHandler extends AbstractXlsXlsxInputHandler{

	protected XlsxInputHandler(String fileName) throws FileNotFoundException, IOException, InvalidFormatException {
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(fileName)));
		this.setCurrentSheet(wb.getSheetAt(0));
	}
	
	protected XlsxInputHandler(File file) throws FileNotFoundException, IOException, InvalidFormatException{
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(file)));
		this.setCurrentSheet(wb.getSheetAt(0));
	}
	
	protected XlsxInputHandler(String fileName, int sheetNumber) throws FileNotFoundException, IOException, InvalidFormatException{
		wb = new XSSFWorkbook(OPCPackage.open(new FileInputStream(fileName)));
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));
	}

	protected XlsxInputHandler(InputStream surveyIS) throws IOException {
		wb = new XSSFWorkbook(surveyIS);
		this.setCurrentSheet(wb.getSheetAt(0));
	}
	
}
