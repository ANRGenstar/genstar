package idees.genstar.datareader.exception;

public class InputFileNotSupportedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InputFileNotSupportedException(){
		super();
		System.out.println("Ce type de fichier n'est pas pris en charge !");
	}

}
