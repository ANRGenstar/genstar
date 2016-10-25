package spll.datamapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import core.io.geo.IGSGeofile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.AGeoEntity;
import core.io.geo.entity.GSFeature;
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
public class SPLMapper<V extends ISPLVariable, T> {

	private ISPLRegressionAlgorithm<V, T> regFunction;
	private ISPLMatcherFactory<V, T> matcherFactory;

	private ShapeFile mainSPLFile;
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

	protected void setMainSPLFile(ShapeFile mainSPLFile){
		this.mainSPLFile = mainSPLFile;
	}

	protected void setMainProperty(Name propertyName){
		this.targetProp = propertyName;
	}

	protected boolean insertMatchedVariable(IGSGeofile regressorsFiles) 
			throws IOException, TransformException, InterruptedException, ExecutionException{
		boolean result = true;
		for(ISPLVariableFeatureMatcher<V, T> matchedVariable : matcherFactory
				.getMatchers(mainSPLFile.getGeoData(), regressorsFiles))
			if(!insertMatchedVariable(matchedVariable) && result)
				result = false;
		return result;
	}

	protected boolean insertMatchedVariable(ISPLVariableFeatureMatcher<V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}


	// --------------------- Accessor --------------------- //

	public Collection<GSFeature> getAttributes(){
		return mainSPLFile.getGeoData();
	}

	public ISPLRegressionAlgorithm<V, T> getRegFunction(){
		return regFunction;
	}

	public Map<AGeoEntity, Set<ISPLVariableFeatureMatcher<V, T>>> getVarMatrix() {
		return getAttributes().stream().collect(Collectors.toMap(
				feat -> feat, 
				feat -> mapper.parallelStream().filter(map -> map.getFeature().equals(feat))
				.collect(Collectors.toSet())));
	}

	public Set<ISPLVariableFeatureMatcher<V, T>> getVariableSet() {
		return Collections.unmodifiableSet(mapper);
	}

	// ------------------- Main Contract ------------------- //

	/**
	 * Operate regression given the data that have been setup for this mapper
	 * WARNING: make use of {@link Stream#parallel()}
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 */
	public Map<V, Double> getRegression() throws IllegalRegressionException {
		if(mapper.parallelStream().anyMatch(var -> var.getFeature().getProperties(this.targetProp).isEmpty()))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		Collection<GSFeature> geoData = mainSPLFile.getGeoData();
		regFunction.setupData(geoData.parallelStream().collect(Collectors.toMap(feat -> feat, 
						feat -> Double.valueOf(feat.getProperties(this.targetProp).iterator().next().getValue().toString()))), mapper);
		return regFunction.regression();
	}

	/**
	 * Compute coefficient to adjust regression relsults
	 * WARNING: make use of {@link Stream#parallel()} 
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 */
	public Map<GSFeature, Double> getCorrectionCoefficient() throws IllegalRegressionException {
		Map<GSFeature, Double> correcCoeff = new HashMap<>();
		Map<V, Double> regCoeff = this.getRegression();
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
