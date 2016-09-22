package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import io.datareaders.georeader.geodat.IGeoGSAttribute;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLRawVariable, Double> {

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature, 
			@SuppressWarnings("rawtypes") IGeoGSFileIO regressorsFile) throws IOException, TransformException {
		@SuppressWarnings("unchecked")
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> list = 
				getMatchers(feature, regressorsFile.getGeoData()); 
		return list;
	}

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(List<GSFeature> features,
			@SuppressWarnings("rawtypes") IGeoGSFileIO regressorsFile) throws IOException, TransformException {
		System.out.println("["+this.getClass().getSimpleName()+ "] Start processing regressors' data");
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<IGeoGSAttribute> geoData = regressorsFile.getGeoData();
		System.out.println("["+this.getClass().getSimpleName()+ "] Start parallel processing "+geoData.size()+" regressor attributes");
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> varList = features
				.parallelStream().map(feat -> getMatchers(feat, geoData))
				.flatMap(list -> list.stream()).collect(Collectors.toList());
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
			@SuppressWarnings("rawtypes") List<IGeoGSAttribute> geoData) {
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> areaMatcherList = new ArrayList<>();
		int nb = 0;
		for(@SuppressWarnings("rawtypes") IGeoGSAttribute feat : geoData){
			// WARNING: do something else 
			// TODO: do not import direct ISPLFileIO but use a spectified (yet to implement) ISPLFileIORegressors
//			if(!feature.getBounds().getCoordinateReferenceSystem().equals(feat.transposeToGenstarFeature().getBounds().getCoordinateReferenceSystem())){
//				System.out.println("Coordinate referent systems effectively differ (after "
//						+ nb +" matches):\n"
//						+ "\tFeature has CRS: "+feature.getBounds().getCoordinateReferenceSystem().getName()
//						+ "\n\tPixel has CRS: "+feat.transposeToGenstarFeature().getBounds().getCoordinateReferenceSystem().getName());
//				System.exit(1);
//			} else {
//				nb++;
//			}
			if(feature.getBounds().contains(feat.transposeToGenstarFeature().getBounds())){
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
		return areaMatcherList;
	}

}
