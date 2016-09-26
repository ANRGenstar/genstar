package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import io.datareaders.georeader.geodat.IGeoGSAttribute;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLRawVariable, Double> {

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature, 
			@SuppressWarnings("rawtypes") IGeoGSFileIO regressorsFile) throws IOException, TransformException {
		@SuppressWarnings("unchecked")
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> list = getMatchers(feature, regressorsFile.getGeoAttributeIterator()); 
		return list;
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(Collection<GSFeature> features,
			@SuppressWarnings("rawtypes") IGeoGSFileIO regressorsFile) throws IOException, TransformException, InterruptedException, ExecutionException {
		System.out.println("["+this.getClass().getSimpleName()+ "] Start processing regressors' data");
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Iterator<IGeoGSAttribute> geoDataIter = regressorsFile.getGeoAttributeIterator();

		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> varList = features
				.parallelStream().flatMap(feat -> getMatchers(feat, geoDataIter).stream()).collect(Collectors.toList());
		System.out.println("["+this.getClass().getSimpleName()+ "] end up with "+varList.size()+" collected matches");

		System.out.println("["+this.getClass().getSimpleName()+ "] Setup aggregated variable-feature-matcher");
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> areaMatcherList = new ArrayList<>();
		for(ISPLVariableFeatureMatcher<SPLRawVariable, Double> var : new ArrayList<>(varList)){
			if(!areaMatcherList.stream().anyMatch(matcher -> matcher.getVariable().getName().equals(var.getVariable().getName())))
				areaMatcherList.add(varList.remove(varList.indexOf(var)));
			else {
				List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> matchedVar = varList
						.stream().filter(match -> match.getVariable().getName().equals(var.getVariable().getName()))
						.collect(Collectors.toList()); 

				areaMatcherList.stream().filter(matcher -> matcher.getVariable().getName().equals(var.getVariable().getName()))
				.findFirst().get().expandValue(matchedVar.stream().mapToDouble(mVar -> mVar.getValue()).sum());

				varList.removeAll(matchedVar);
			}
		}
		System.out.println("["+this.getClass().getSimpleName()+ "] end up with "+varList.size()+" individual match");
		return areaMatcherList;
	}

	@SuppressWarnings("unchecked")
	private List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature,
			@SuppressWarnings("rawtypes") Iterator<IGeoGSAttribute> geoData) {
		System.out.println("\tprocessing feature "+feature.getIdentifier().getID());
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> areaMatcherList = new ArrayList<>();
		Geometry geofeat = (Geometry) feature.getDefaultGeometryProperty().getValue();
		while(geoData.hasNext()){
			@SuppressWarnings("rawtypes")
			IGeoGSAttribute feat = geoData.next();
			if(geofeat.contains(feat.getPosition())){
				for(Object prop : feat.getProperties()){
					if(areaMatcherList.stream().anyMatch(varMatcher -> varMatcher.getVariable().getName().equals(feat.getGenstarName()) &&
							varMatcher.getVariable().equals(feat.getValue(prop)))){
						// IF Variable is already matched, update area (+1 px)
						areaMatcherList.stream().filter(varMatcher -> varMatcher.getVariable().getName().equals(feat.getGenstarName()) &&
								varMatcher.getVariable().equals(feat.getValue(prop))).findFirst().get().expandValue(1d);
					} else {
						// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
						areaMatcherList.add(new SPLAreaMatcher(feature, 
								new SPLRawVariable(feat.getValue(prop), feat.getGenstarName())));
					}
				}
			}
		}
		System.out.println("\tfeature "+feature.getIdentifier().getID()+" proceeded & matched with "+areaMatcherList.size()+" regressor variable");
		return areaMatcherList;
	}

}
