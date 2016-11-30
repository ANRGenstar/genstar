package gospl.algo.sampler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

/**
 * A Hierarchical sampler explores the variables in a given order to generate the individuals.  
 * 
 * @author Samuel Thiriot
 *
 */
public class GosplHierarchicalSampler implements IHierarchicalSampler {

	private Logger logger = LogManager.getLogger();
	private GosplBasicDistribution gosplBasicDistribution = null;
	private Collection<List<ASurveyAttribute>> explorationOrder = null;
	
	public GosplHierarchicalSampler() {
		// TODO Auto-generated constructor stub
	}


	// -------------------- setup methods -------------------- //




	@Override
	public void setDistribution(
			GosplBasicDistribution gosplBasicDistribution,
			Collection<List<ASurveyAttribute>> explorationOrder
			) {
		this.gosplBasicDistribution = gosplBasicDistribution;
		this.explorationOrder = explorationOrder;
		
	}

	
	// -------------------- main contract -------------------- //

	@Override
	public ACoordinate<ASurveyAttribute, AValue> draw() {

		Map<ASurveyAttribute,AValue> att2value = new HashMap<>();
		
		logger.info("starting hierarchical sampling...");
		for (List<ASurveyAttribute> subgraph : explorationOrder) {
			logger.info("starting hierarchical sampling for the first subgraph {}", subgraph);
			for (ASurveyAttribute att: subgraph) {
				logger.info("sampling att {}", att);
				
				//gosplBasicDistribution.get
				
			}
		}
		
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<ASurveyAttribute, AValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}

	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}


}
