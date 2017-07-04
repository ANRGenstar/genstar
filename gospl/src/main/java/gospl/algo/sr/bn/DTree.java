package gospl.algo.sr.bn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A DTree for a Bayesian network is a full binary tree, the leaves of which correspond to the network CPTs 
 * (A. Dawiche 2001)
 * 
 * @author Samuel Thiriot
 *
 */
public class DTree {

	private static Logger logger = LogManager.getLogger();

	private final CategoricalBayesianNetwork bn;
	
	
	public DTree(CategoricalBayesianNetwork bn) {
		this.bn = bn;
		
		// initialize from BN
		
	}
	

}
