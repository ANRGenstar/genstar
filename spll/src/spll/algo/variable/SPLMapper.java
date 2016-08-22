package spll.algo.variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;

import spll.algo.ISPLRegressionAlgorithm;
import spll.algo.exception.IllegalRegressionException;
import io.datareaders.georeader.ISPLFileIO;

/**
 * TODO: force <T> generic to fit a regression style contract: either boolean (variable is present or not) 
 * or numeric (variable has a certain amount)
 * 
 * @author kevinchapuis
 *
 * @param <F>
 * @param <V>
 * @param <T>
 */
public class SPLMapper<F extends Feature, V extends ISPLVariable<?>, T> {

	private final ISPLRegressionAlgorithm<V> regFunction;
	private final SPLMatcherFactory<F, V, T> matcherFactory;
	
	private final ISPLFileIO<F> mainSPLFile;
	private Name targetProp;

	private Set<ISPLVariableFeatureMatcher<F, V, T>> mapper = new HashSet<>();

	// --------------------- Constructor --------------------- //

	public SPLMapper(ISPLFileIO<F> mainSPLFile, Name propertyName, 
			SPLMatcherFactory<F, V, T> matcherFactory, 
			ISPLRegressionAlgorithm<V> regFunction){
		this.mainSPLFile = mainSPLFile;
		this.targetProp = propertyName;
		this.regFunction = regFunction;
		this.matcherFactory = matcherFactory;
	}

	// --------------------- Modifier --------------------- //

	public boolean insertMatchedVariable(List<ISPLFileIO<F>> regressorsFiles){
		boolean result = true;
		for(ISPLFileIO<F> file : regressorsFiles)
			for(F feature : mainSPLFile.getFeatures())
				for(ISPLVariableFeatureMatcher<F, V, T> matchedVariable : matcherFactory.getAreaMatchers(feature, file))
					if(!insertMatchedVariable(matchedVariable) && result)
						result = false;
		return result;
	}

	private boolean insertMatchedVariable(ISPLVariableFeatureMatcher<F, V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}

	public void setTargetedProperty(Name property){
		this.targetProp = property;
	}

	// --------------------- Accessor --------------------- //

	public List<F> getFeature(){
		return mainSPLFile.getFeatures();
	}

	public ISPLRegressionAlgorithm<V> getRegFunction(){
		return regFunction;
	}

	public Map<F, Set<ISPLVariableFeatureMatcher<F, V, T>>> getVarMatrix() {
		return getFeature().stream().collect(Collectors.toMap(
				feat -> feat, 
				feat -> mapper.parallelStream().filter(map -> map.getFeature().equals(feat))
				.collect(Collectors.toSet())));
	}

	public Set<ISPLVariableFeatureMatcher<F, V, T>> getVariableSet() {
		return Collections.unmodifiableSet(mapper);
	}

	// ------------------- Main Contract ------------------- //

	public Map<V, Double> regression() throws IllegalRegressionException {
		if(mapper.parallelStream().anyMatch(var -> var.getFeature().getProperties(this.targetProp).isEmpty()))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		return regFunction.regression();
	}

	public Map<F, Double> getCorrectionCoefficient() throws IllegalRegressionException {
		Map<F, Double> correcCoeff = new HashMap<>();
		Map<V, Double> regCoeff = this.regression();
		for(F feature : getFeature()){
			double targetVal = Double.valueOf(feature.getProperty(targetProp).getValue().toString());
			double regressVal = mapper.parallelStream().filter(varMatcher -> varMatcher.getFeature().equals(feature))
					.mapToDouble(map -> getComputedRegressValue(regCoeff.get(map.getVariable()), map.getValue())).sum();
			correcCoeff.put(feature, targetVal / regressVal);
		}
		return correcCoeff;
	}

	// ------------------- Inner utilities ------------------- //

	// TODO: one of the most challenging methods, hence matchedValue could be of type Number or boolean
	// WARNING: ugly algo
	private double getComputedRegressValue(Double regCoeff, T matchedValue) {
		if(matchedValue.toString().compareToIgnoreCase("true") == 0)
			return regCoeff;
		else if(matchedValue.toString().compareToIgnoreCase("false") == 0)
			return 0d;
		return regCoeff * Double.valueOf(matchedValue.toString());
	}

}
