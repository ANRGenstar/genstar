package io.datareaders.surveyreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

class XlsInputHandler extends AbstractXlsXlsxInputHandler {
	
	/**
	 * Create a concrete copy of the workbook from the
	 * file named as indicate with parameter. As default
	 * value, the sheet to work with is the first one.
	 * 
	 * @author kevin
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected XlsInputHandler(String filename) throws FileNotFoundException, IOException{
		wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(filename)));
		this.setCurrentSheet(wb.getSheetAt(0));
	}
	
	protected XlsInputHandler(String filename, int sheetNumber) throws FileNotFoundException, IOException{
		wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(filename)));
		this.setCurrentSheet(wb.getSheetAt(sheetNumber));		
	}

	protected XlsInputHandler(File file) throws FileNotFoundException, IOException {
		wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
		this.setCurrentSheet(wb.getSheetAt(0));
	}

	protected XlsInputHandler(InputStream surveyIS) throws IOException {
		wb = new HSSFWorkbook(surveyIS);
		this.setCurrentSheet(wb.getSheetAt(0));
	}
}
