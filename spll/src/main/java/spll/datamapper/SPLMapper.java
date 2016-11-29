package spll.datamapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import core.io.geo.IGSGeofile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.AGeoEntity;
import core.io.geo.entity.GSFeature;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.matcher.ISPLMatcherFactory;
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

	private ISPLRegressionAlgo<V, T> regFunction;
	private boolean setupReg;
	
	private ISPLMatcherFactory<V, T> matcherFactory;

	private ShapeFile mainSPLFile;
	private String targetProp;

	private Set<ISPLMatcher<V, T>> mapper = new HashSet<>();

	// --------------------- Constructor --------------------- //

	protected SPLMapper() { }

	// --------------------- Modifier --------------------- //

	protected void setRegAlgo(ISPLRegressionAlgo<V, T> regressionAlgorithm) {
		this.regFunction = regressionAlgorithm;
	}

	protected void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}

	protected void setMainSPLFile(ShapeFile mainSPLFile){
		this.mainSPLFile = mainSPLFile;
	}

	protected void setMainProperty(String propertyName){
		this.targetProp = propertyName;
	}

	protected boolean insertMatchedVariable(IGSGeofile regressorsFiles) 
			throws IOException, TransformException, InterruptedException, ExecutionException{
		boolean result = true;
		for(ISPLMatcher<V, T> matchedVariable : matcherFactory
				.getMatchers(mainSPLFile.getGeoData(), regressorsFiles))
			if(!insertMatchedVariable(matchedVariable) && result)
				result = false;
		return result;
	}

	protected boolean insertMatchedVariable(ISPLMatcher<V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}


	// --------------------- Accessor --------------------- //

	public Collection<GSFeature> getAttributes(){
		return mainSPLFile.getGeoData();
	}

	public Map<AGeoEntity, Set<ISPLMatcher<V, T>>> getVarMatrix() {
		return getAttributes().stream().collect(Collectors.toMap(
				feat -> feat, 
				feat -> mapper.parallelStream().filter(map -> map.getFeature().equals(feat))
				.collect(Collectors.toSet())));
	}

	public Set<ISPLMatcher<V, T>> getVariableSet() {
		return Collections.unmodifiableSet(mapper);
	}

	// ------------------- Main Contract ------------------- //

	/**
	 * Gives the intercept of the regression
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 */
	public double getIntercept() throws IllegalRegressionException {
		this.setupRegression();
		return regFunction.getIntercept();
	}
	
	/**
	 * Operate regression given the data that have been setup for this mapper
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 */
	public Map<V, Double> getRegression() throws IllegalRegressionException {
		this.setupRegression();
		return regFunction.getRegressionParameter();
	}
	
	/**
	 * 
	 * TODO javadoc
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 */
	public Map<AGeoEntity, Double> getResidual() throws IllegalRegressionException {
		this.setupRegression();
		return regFunction.getResidual();
	}

	// ------------------- Inner utilities ------------------- //
	
	private void setupRegression() throws IllegalRegressionException{
		if(mapper.stream().anyMatch(var -> var.getFeature().getPropertyAttribute(this.targetProp) == null))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		if(mapper.stream().anyMatch(var -> !var.getFeature().getValueForAttribute(this.targetProp).isNumericalValue()))
			throw new IllegalArgumentException("Property value must be of numerical type in order to setup regression on");
		if(!setupReg){
			Collection<GSFeature> geoData = mainSPLFile.getGeoData();
			regFunction.setupData(geoData.stream().collect(Collectors.toMap(feat -> feat, 
					feat -> feat.getValueForAttribute(this.targetProp).getNumericalValue().doubleValue())), mapper);
			setupReg = true;
		}
	}
	
}
