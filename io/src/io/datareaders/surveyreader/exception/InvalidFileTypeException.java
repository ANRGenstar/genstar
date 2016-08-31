package io.datareaders.surveyreader.exception;

import java.io.IOException;
import java.util.List;

public class InvalidFileTypeException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileTypeException(String path, List<String> list) {
		super("File type "+path+" does not fall within the expected range: "+list);
	}

}
