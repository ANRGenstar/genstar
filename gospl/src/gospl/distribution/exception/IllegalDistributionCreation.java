package gospl.distribution.exception;

public class IllegalDistributionCreation extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalDistributionCreation(String message) {
		super("["+IllegalDistributionCreation.class.getName()+"] invoque issue regarding a set of Joint distribution"
				+message);
	}

	
}
