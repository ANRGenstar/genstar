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

import com.vividsolutions.jts.geom.Geometry;

import io.data.geo.IGSGeofile;
import io.data.geo.attribute.GSFeature;
import io.data.geo.attribute.IGeoGSAttribute;
import io.data.geo.attribute.IGeoValue;
import io.util.GSPerformanceUtil;
import spll.datamapper.variable.SPLVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLVariable, Double> {
	
	public static boolean LOGSYSO = true;
	private int matcherCount = 0;
	
	private Collection<IGeoValue> variables;

	public SPLAreaMatcherFactory(Collection<IGeoValue> variables) {
		this.variables = variables;
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLVariable, Double>> getMatchers(GSFeature feature, 
			IGSGeofile regressorsFile) throws IOException, TransformException, InterruptedException, ExecutionException { 
		return getMatchers(Arrays.asList(feature), regressorsFile);
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLVariable, Double>> getMatchers(Collection<GSFeature> features,
			IGSGeofile regressorsFile) 
					throws IOException, TransformException, InterruptedException, ExecutionException {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regressors' data", LOGSYSO);
		gspu.setObjectif(features.size());
		List<ISPLVariableFeatureMatcher<SPLVariable, Double>> varList = features
				.parallelStream().map(feat -> getMatchers(feat, 
						regressorsFile.getGeoAttributeIteratorWithin(feat.getGeometry()), 
						this.variables, gspu))
				.flatMap(list -> list.stream()).collect(Collectors.toList());
		gspu.sysoStempMessage("process ends up with "+varList.size()+" collected matches");
		return varList;
	}

	private List<ISPLVariableFeatureMatcher<SPLVariable, Double>> getMatchers(GSFeature feature,
			Iterator<? extends IGeoGSAttribute> geoData, Collection<IGeoValue> variables, 
			GSPerformanceUtil gspu) {
		List<ISPLVariableFeatureMatcher<SPLVariable, Double>> areaMatcherList = new ArrayList<>();
		Geometry geofeat = feature.getGeometry();
		while(geoData.hasNext()){
			IGeoGSAttribute feat = geoData.next();
			if(feat.getPosition().within(geofeat)){
				for(String prop : feat.getPropertiesAttribute()){
					if(!variables.isEmpty() && !variables.contains(feat.getValue(prop)))
						continue;
					Optional<ISPLVariableFeatureMatcher<SPLVariable, Double>> potentialMatch = areaMatcherList
							.stream().filter(varMatcher -> varMatcher.getVariable().getName().equals(prop.toString()) &&
							varMatcher.getVariable().getValue().equals(feat.getValue(prop))).findFirst();
					if(potentialMatch.isPresent()){
						// IF Variable is already matched, update area
						potentialMatch.get().expandValue(feat.getArea());
					} else {
						// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
						if(!feat.isNoDataValue(prop))
							areaMatcherList.add(new SPLAreaMatcher(feature, 
								new SPLVariable(feat.getValue(prop), prop.toString()), feat.getArea()));
					}
				}
			}
		}
		if(gspu != null && ((++matcherCount+1)/gspu.getObjectif() * 100) % 10 == 0d)
			gspu.sysoStempPerformance((matcherCount+1)/gspu.getObjectif(), this);
		return areaMatcherList;
	}

}
