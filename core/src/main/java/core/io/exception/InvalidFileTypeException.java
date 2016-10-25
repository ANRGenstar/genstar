package core.io.exception;

import java.util.List;

public class InvalidFileTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileTypeException(String fileName, List<String> supportedFileFormat) {
		super("file "+fileName+" is not a valide file type which are "+supportedFileFormat.toString());	
	}

}
