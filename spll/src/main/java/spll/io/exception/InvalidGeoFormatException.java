package spll.io.exception;

import java.util.List;

public class InvalidGeoFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidGeoFormatException(String fileName, List<String> supportedFileFormat) {
		super("file "+fileName+" is not a valide file type which are "+supportedFileFormat.toString());	
	}

}
