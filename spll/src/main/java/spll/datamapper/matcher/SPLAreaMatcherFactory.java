package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import core.io.geo.IGSGeofile;
import core.io.geo.entity.AGeoEntity;
import core.io.geo.entity.GSFeature;
import core.io.geo.entity.attribute.value.AGeoValue;
import core.util.GSPerformanceUtil;
import spll.datamapper.variable.SPLVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLVariable, Double> {

	public static boolean LOGSYSO = true;
	private int matcherCount = 0;

	private Collection<AGeoValue> variables;

	public SPLAreaMatcherFactory(Collection<AGeoValue> variables) {
		this.variables = variables;
	}

	@Override
	public List<ISPLMatcher<SPLVariable, Double>> getMatchers(GSFeature feature, 
			IGSGeofile regressorsFile) throws IOException, TransformException, InterruptedException, ExecutionException { 
		return getMatchers(Arrays.asList(feature), regressorsFile);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * </p>
	 * WARNING: make use of parallelism
	 * 
	 */
	@Override
	public List<ISPLMatcher<SPLVariable, Double>> getMatchers(Collection<GSFeature> features,
			IGSGeofile regressorsFile) 
					throws IOException, TransformException, InterruptedException, ExecutionException {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regressors' data", LOGSYSO);
		gspu.setObjectif(features.size());
		List<ISPLMatcher<SPLVariable, Double>> varList = features
				.parallelStream().map(feat -> getMatchers(feat, 
						regressorsFile.getGeoAttributeIteratorWithin(feat.getGeometry()), 
						this.variables, gspu))
				.flatMap(list -> list.stream()).collect(Collectors.toList());
		gspu.sysoStempMessage("-------------------------\n"
				+ "process ends up with "+varList.size()+" collected matches");
		return varList;
	}

	// ----------------------------------------------------------- //

	/*
	 * TODO: could be optimise
	 */
	private List<ISPLMatcher<SPLVariable, Double>> getMatchers(GSFeature feature,
			Iterator<? extends AGeoEntity> geoData, Collection<AGeoValue> variables, 
			GSPerformanceUtil gspu) {
		List<ISPLMatcher<SPLVariable, Double>> areaMatcherList = new ArrayList<>();
		while(geoData.hasNext()){
			AGeoEntity geoEntity = geoData.next();  
			for(String prop : geoEntity.getPropertiesAttribute()){
				AGeoValue value = geoEntity.getValueForAttribute(prop);
				if(!variables.isEmpty() && !variables.contains(value))
					continue;
				Optional<ISPLMatcher<SPLVariable, Double>> potentialMatch = areaMatcherList
						.stream().filter(varMatcher -> varMatcher.getVariable().getName().equals(prop.toString()) &&
								varMatcher.getVariable().getValue().equals(value)).findFirst();
				if(potentialMatch.isPresent()){
					// IF Variable is already matched, update area
					potentialMatch.get().expandValue(geoEntity.getArea());
				} else {
					// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
					//if(!geoEntity.getPropertyAttribute(prop).equals(value))
					areaMatcherList.add(new SPLAreaMatcher(feature, 
							new SPLVariable(value, prop.toString()), geoEntity.getArea()));
				}
			}
		}
		if(gspu != null && ((++matcherCount+1)/gspu.getObjectif() * 100) % 10 == 0d)
			gspu.sysoStempPerformance((matcherCount+1)/gspu.getObjectif(), this);
		return areaMatcherList;
	}

}
