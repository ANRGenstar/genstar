package spll.datamapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.ShapeFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import io.datareaders.georeader.geodat.IGeoGSAttribute;
import spll.algo.ISPLRegressionAlgorithm;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.ISPLMatcherFactory;
import spll.datamapper.matcher.ISPLVariableFeatureMatcher;
import spll.datamapper.variable.ISPLVariable;

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
public class SPLMapper<V extends ISPLVariable<?>, T> {

	private ISPLRegressionAlgorithm<V, T> regFunction;
	private ISPLMatcherFactory<V, T> matcherFactory;

	private ShapeFileIO mainSPLFile;
	private Name targetProp;

	private Set<ISPLVariableFeatureMatcher<V, T>> mapper = new HashSet<>();

	// --------------------- Constructor --------------------- //

	protected SPLMapper() { }

	// --------------------- Modifier --------------------- //

	protected void setRegAlgo(ISPLRegressionAlgorithm<V, T> regressionAlgorithm) {
		this.regFunction = regressionAlgorithm;
	}

	protected void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}

	protected void setMainSPLFile(ShapeFileIO mainSPLFile){
		this.mainSPLFile = mainSPLFile;
	}

	protected void setMainProperty(Name propertyName){
		this.targetProp = propertyName;
	}

	protected boolean insertMatchedVariable(@SuppressWarnings("rawtypes") IGeoGSFileIO regressorsFiles) 
			throws IOException, TransformException{
		boolean result = true;
		for(ISPLVariableFeatureMatcher<V, T> matchedVariable : matcherFactory.getMatchers(mainSPLFile.getGeoData(), regressorsFiles))
			if(!insertMatchedVariable(matchedVariable) && result)
				result = false;
		return result;
	}

	protected boolean insertMatchedVariable(ISPLVariableFeatureMatcher<V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}


	// --------------------- Accessor --------------------- //

	public List<GSFeature> getAttributes(){
		return mainSPLFile.getGeoData();
	}

	public ISPLRegressionAlgorithm<V, T> getRegFunction(){
		return regFunction;
	}

	public Map<IGeoGSAttribute<Property, Object>, Set<ISPLVariableFeatureMatcher<V, T>>> getVarMatrix() {
		return getAttributes().stream().collect(Collectors.toMap(
				feat -> feat, 
				feat -> mapper.parallelStream().filter(map -> map.getFeature().equals(feat))
				.collect(Collectors.toSet())));
	}

	public Set<ISPLVariableFeatureMatcher<V, T>> getVariableSet() {
		return Collections.unmodifiableSet(mapper);
	}

	// ------------------- Main Contract ------------------- //

	public Map<V, Double> regression() throws IllegalRegressionException {
		if(mapper.parallelStream().anyMatch(var -> var.getFeature().getProperties(this.targetProp).isEmpty()))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		regFunction.setupData(mainSPLFile.getGeoData()
				.parallelStream().collect(Collectors.toMap(feat -> feat, 
						feat -> (Double) feat.getProperties(this.targetProp).iterator().next().getValue())),
				mapper);
		return regFunction.regression();
	}

	public Map<GSFeature, Double> getCorrectionCoefficient() throws IllegalRegressionException {
		Map<GSFeature, Double> correcCoeff = new HashMap<>();
		Map<V, Double> regCoeff = this.regression();
		for(GSFeature attribute : this.getAttributes()){
			double targetVal = Double.valueOf(attribute.getProperty(targetProp).getValue().toString());
			double regressVal = mapper.parallelStream().filter(varMatcher -> varMatcher.getFeature().equals(attribute))
					.mapToDouble(map -> getComputedRegressValue(regCoeff.get(map.getVariable()), map.getValue())).sum();
			correcCoeff.put(attribute, targetVal / regressVal);
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
