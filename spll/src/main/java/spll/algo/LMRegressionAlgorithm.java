package spll.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import io.datareaders.georeader.geodat.IGeoGSAttribute;
import spll.datamapper.matcher.ISPLVariableFeatureMatcher;
import spll.datamapper.variable.SPLRawVariable;

public class LMRegressionAlgorithm extends OLSMultipleLinearRegression implements ISPLRegressionAlgorithm<SPLRawVariable, Double> { 
	
	private List<SPLRawVariable> regVars;
	
	@Override
	public void setupData(Map<IGeoGSAttribute, Double> observations,
			Set<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> regressors){
		this.regVars = new ArrayList<>(regressors
				.parallelStream().map(varfm -> varfm.getVariable())
				.collect(Collectors.toSet()));
		double[] y = new double[observations.size()];
		double[][] x = new double[observations.size()][];
		int yIdx = 0;
		for(IGeoGSAttribute feature : observations.keySet()){
			y[yIdx] = observations.get(feature);
			x[yIdx] = new double[regVars.size()];
			for(int i = 0; i < regVars.size(); i++){
				int index = i;
				Optional<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> optVar = regressors.parallelStream()
					.filter(varfm -> varfm.getFeature().equals(feature) 
					&& varfm.getVariable().equals(regVars.get(index))).findFirst();
				x[yIdx][index] = optVar.isPresent() ? optVar.get().getValue() : 0d;
			}
			yIdx++;
		}
		super.newSampleData(y, x);
	}
	
	@Override
	public Map<SPLRawVariable, Double> regression() {
		Map<SPLRawVariable, Double> regMap = new HashMap<>();
		RealVector rVec = super.calculateBeta();
		for(int i = 0; i < regVars.size(); i++)
			regMap.put(regVars.get(i), rVec.getEntry(i));
		return regMap;
	}

}
