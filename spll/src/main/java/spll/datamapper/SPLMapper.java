package spll.datamapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.matcher.ISPLMatcherFactory;
import spll.datamapper.variable.ISPLVariable;

/**
 * TODO: force <T> generic to fit a regression style contract: either boolean (variable is present or not) 
 * or numeric (variable has a certain amount)
 * <p>
 * This object purpose is to setup a regression between a variable contains in a main geographic file
 * with variables contains in one or more ancillary geographic files. The mapping is based on a shared
 * geographical referent space: all file must use the same {@link CoordinateReferenceSystem} and overlap
 * -- at least only overlapped places will be processed.
 * <p> 
 * This object should be created using any {@link ASPLMapperBuilder}
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

	private IGSGeofile<? extends AGeoEntity> mainSPLFile;
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

	protected void setMainSPLFile(IGSGeofile<? extends AGeoEntity> mainSPLFile){
		this.mainSPLFile = mainSPLFile;
	}

	protected void setMainProperty(String propertyName){
		this.targetProp = propertyName;
	}

	protected boolean insertMatchedVariable(IGSGeofile<? extends AGeoEntity> regressorsFiles) 
			throws IOException, TransformException, InterruptedException, ExecutionException{
		boolean result = true;
		for(ISPLMatcher<V, T> matchedVariable : matcherFactory
				.getMatchers(mainSPLFile.getGeoEntity(), regressorsFiles))
			if(!insertMatchedVariable(matchedVariable) && result)
				result = false;
		return result;
	}

	protected boolean insertMatchedVariable(ISPLMatcher<V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}


	// --------------------- Accessor --------------------- //

	public Collection<? extends AGeoEntity> getAttributes() throws IOException{
		return mainSPLFile.getGeoEntity();
	}

	public Map<AGeoEntity, Set<ISPLMatcher<V, T>>> getVarMatrix() throws IOException {
		return getAttributes().stream().collect(Collectors.toMap(
				feat -> feat, 
				feat -> mapper.parallelStream().filter(map -> map.getEntity().equals(feat))
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
	 * @throws IOException 
	 */
	public double getIntercept() throws IllegalRegressionException, IOException {
		this.setupRegression();
		return regFunction.getIntercept();
	}
	
	/**
	 * Operate regression given the data that have been setup for this mapper
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 * @throws IOException 
	 */
	public Map<V, Double> getRegression() throws IllegalRegressionException, IOException {
		this.setupRegression();
		return regFunction.getRegressionParameter();
	}
	
	/**
	 * 
	 * TODO javadoc
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 * @throws IOException 
	 */
	public Map<AGeoEntity, Double> getResidual() throws IllegalRegressionException, IOException {
		this.setupRegression();
		return regFunction.getResidual();
	}

	// ------------------- Inner utilities ------------------- //
	
	private void setupRegression() throws IllegalRegressionException, IOException{
		if(mapper.stream().anyMatch(var -> var.getEntity().getPropertyAttribute(this.targetProp) == null))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		if(mapper.stream().anyMatch(var -> !var.getEntity().getValueForAttribute(this.targetProp).isNumericalValue()))
			throw new IllegalArgumentException("Property value must be of numerical type in order to setup regression on");
		if(!setupReg){
			Collection<? extends AGeoEntity> geoData = mainSPLFile.getGeoEntity();
			regFunction.setupData(geoData.stream().collect(Collectors.toMap(feat -> feat, 
					feat -> feat.getValueForAttribute(this.targetProp).getNumericalValue().doubleValue())), mapper);
			setupReg = true;
		}
	}
	
}
