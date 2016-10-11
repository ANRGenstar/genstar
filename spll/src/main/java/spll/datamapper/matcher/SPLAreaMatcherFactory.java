package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import io.geofile.IGSGeofile;
import io.geofile.data.GSFeature;
import io.geofile.data.IGeoGSAttribute;
import io.util.GSPerformanceUtil;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLRawVariable, Double> {
	
	public static boolean LOGSYSO = true;
	
	private int matcherCount = 0;

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature, 
			IGSGeofile regressorsFile) throws IOException, TransformException {
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> list = getMatchers(feature, 
				regressorsFile.getGeoAttributeIterator(feature), null); 
		return list;
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(Collection<GSFeature> features,
			IGSGeofile regressorsFile) 
					throws IOException, TransformException, InterruptedException, ExecutionException {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regressors' data", LOGSYSO);
		gspu.setObjectif(features.size());
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> varList = features
				.parallelStream().map(feat -> getMatchers(feat, 
						regressorsFile.getGeoAttributeIterator(feat), gspu))
				.flatMap(list -> list.stream()).collect(Collectors.toList());
		gspu.sysoStempMessage("process ends up with "+varList.size()+" collected matches");
		return varList;
	}

	private List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature,
			Iterator<? extends IGeoGSAttribute> geoData, GSPerformanceUtil gspu) {
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> areaMatcherList = new ArrayList<>();
		Geometry geofeat = (Geometry) feature.getDefaultGeometryProperty().getValue();
		while(geoData.hasNext()){
			IGeoGSAttribute feat = geoData.next();
			if(feat.getPosition().within(geofeat)){
				for(String prop : feat.getPropertiesAttribute()){
					Optional<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> potentialMatch = areaMatcherList
							.stream().filter(varMatcher -> varMatcher.getVariable().getName().equals(prop.toString()) &&
							varMatcher.getVariable().getValue().equals(feat.getValue(prop))).findFirst();
					if(potentialMatch.isPresent()){
						// IF Variable is already matched, update area (+1 px)
						potentialMatch.get().expandValue(1d);
					} else {
						// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
						if(!feat.isNoDataValue(prop))
							areaMatcherList.add(new SPLAreaMatcher(feature, 
								new SPLRawVariable(feat.getValue(prop), prop.toString())));
					}
				}
			}
		}
		if(gspu != null && (++matcherCount/gspu.getObjectif() * 100) % 10 == 0d)
			gspu.sysoStempPerformance(matcherCount/gspu.getObjectif(), this);
//		gspu.sysoStempMessage("\tfeature "+feature.getIdentifier().getID()+" proceeded & matched with "
//				+areaMatcherList.size()+" regressor variable");
		return areaMatcherList;
	}

}
