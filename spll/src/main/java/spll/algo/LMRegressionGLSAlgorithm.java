package spll.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;

import core.io.geo.entity.AGeoEntity;
import spll.datamapper.matcher.ISPLVariableFeatureMatcher;
import spll.datamapper.variable.SPLVariable;

public class LMRegressionGLSAlgorithm extends GLSMultipleLinearRegression implements ISPLRegressionAlgorithm<SPLVariable, Double> {

	private List<SPLVariable> regVars;
	private Map<SPLVariable, Double> regression;

	@Override
	public void setupData(Map<AGeoEntity, Double> observations,
			Set<ISPLVariableFeatureMatcher<SPLVariable, Double>> regressors){
		this.regVars = new ArrayList<>(regressors
				.parallelStream().map(varfm -> varfm.getVariable())
				.collect(Collectors.toSet()));
		double[] instances = new double[regVars.size() * observations.size()];
		int instanceCount = 0;
		for(AGeoEntity geoEntity : observations.keySet()){
			instances[instanceCount++] = observations.get(geoEntity);
			for(int i = 0; i < regVars.size(); i++){
				int idx = i;
				Optional<ISPLVariableFeatureMatcher<SPLVariable, Double>> optVar = regressors.parallelStream()
						.filter(varfm -> varfm.getFeature().equals(geoEntity) 
								&& varfm.getVariable().equals(regVars.get(idx)))
						.findFirst();
				instances[instanceCount++] = optVar.isPresent() ? optVar.get().getValue() : 0d;
			}
		}
		
		super.newSampleData(instances, observations.size(), regVars.size());
	}

	@Override
	public Map<SPLVariable, Double> regression() {
		if(regression == null){
			regression = new HashMap<>();
			double[] rVec = super.estimateRegressionParameters();
			for(int i = 0; i < regVars.size(); i++)
				regression.put(regVars.get(i), rVec[i]);
		}
		return regression;
	}

	public RealVector getSampleData(){
		return super.getY();
	}

	public RealMatrix getObservations(){
		return super.getX();
	}

}
