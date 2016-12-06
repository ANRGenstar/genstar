package gospl.distribution;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

public class GosplDistributionFactory {

	
	public INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> createDitribution(
			Set<APopulationAttribute> dimensions){
		return new GosplJointDistribution(dimensions.stream().collect(Collectors.toMap(dim -> dim, dim -> dim.getValues())), 
				GSSurveyType.GlobalFrequencyTable);
	}
	
	/**
	 * Create a frequency matrix from entity's population characteristics.  
	 * <p>
	 * WARNING: make use of parallelism through {@link Stream#parallel()}
	 * 
	 * @param population
	 * @return
	 */
	public INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> createDistribution(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
		// Init the output matrix
		AFullNDimensionalMatrix<Double> matrix = new GosplJointDistribution(
				population.getPopulationAttributes().stream().collect(Collectors.toMap(att -> att, att -> att.getValues())), 
				GSSurveyType.GlobalFrequencyTable);
		
		// Transpose each entity into a coordinate and adds it to the matrix by means of increments
		for(APopulationEntity entity : population){
			ACoordinate<APopulationAttribute, APopulationValue> entityCoord = new GosplCoordinate(
					new HashSet<>(entity.getValues()));
			AControl<Double> unitFreq = new ControlFrequency(1d);
			if(!matrix.addValue(entityCoord, unitFreq))
				matrix.getVal(entityCoord).add(unitFreq);
		}
		
		// Normalize increments to global frequency
		matrix.getMatrix().keySet().parallelStream().forEach(coord -> matrix.getVal(coord).multiply(1d/population.size()));
		
		return matrix;
	}
	
}
