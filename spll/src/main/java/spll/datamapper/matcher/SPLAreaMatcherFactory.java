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

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import io.datareaders.georeader.geodat.IGeoGSAttribute;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLRawVariable, Double> {

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature, 
			IGeoGSFileIO regressorsFile) throws IOException, TransformException {
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> list = getMatchers(feature, 
				regressorsFile.getGeoAttributeIterator(feature)); 
		return list;
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(Collection<GSFeature> features,
			IGeoGSFileIO regressorsFile) 
					throws IOException, TransformException, InterruptedException, ExecutionException {
		System.out.println("["+this.getClass().getSimpleName()+ "] Start processing regressors' data");

		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> varList = features
				.parallelStream().map(feat -> getMatchers(feat, 
						regressorsFile.getGeoAttributeIterator(feat)))
				.flatMap(list -> list.stream()).collect(Collectors.toList());
		System.out.println("["+this.getClass().getSimpleName()+ "] end up with "+varList.size()+" collected matches");
		return varList;
	}

	private List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature,
			Iterator<? extends IGeoGSAttribute> geoData) {
		System.out.println("\tprocessing feature "+feature.getIdentifier().getID());
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
						areaMatcherList.add(new SPLAreaMatcher(feature, 
								new SPLRawVariable(feat.getValue(prop), prop.toString())));
					}
				}
			}
		}
		System.out.println("\tfeature "+feature.getIdentifier().getID()+" proceeded & matched with "
				+areaMatcherList.size()+" regressor variable");
		return areaMatcherList;
	}

}
