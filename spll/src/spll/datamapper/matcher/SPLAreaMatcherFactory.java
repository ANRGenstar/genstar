package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.ISPLFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import io.datareaders.georeader.geodat.IGeoGSAttributes;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<SPLRawVariable, Double> {

	@Override
	public List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> getMatchers(GSFeature feature, ISPLFileIO file) throws IOException, TransformException {
		List<ISPLVariableFeatureMatcher<SPLRawVariable, Double>> areaMatcherList = new ArrayList<>();
		for(Object feat : file.getGeoData()){
			// WARNING: do something else 
			// TODO: do not import direct ISPLFileIO but use a spectified (yet to implement) ISPLFileIORegressors
			IGeoGSAttributes geoGenAtt = (IGeoGSAttributes) feat;
			if(feature.getBounds().contains(geoGenAtt.transposeToGenstarFeature().getBounds())){
				for(Object prop : geoGenAtt.getData()){
					if(areaMatcherList.stream().anyMatch(varMatcher -> varMatcher.getVariable().getName().equals(geoGenAtt.getGenstarName()) &&
							varMatcher.getVariable().equals(geoGenAtt.getValue(prop)))){
						// IF Variable is already matched, update area (+1 px)
						areaMatcherList.stream().filter(varMatcher -> varMatcher.getVariable().getName().equals(geoGenAtt.getGenstarName()) &&
								varMatcher.getVariable().equals(geoGenAtt.getValue(prop))).findFirst().get().expandValue(1d);
					} else {
						// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
						areaMatcherList.add(new SPLAreaMatcher(feature, 
								new SPLRawVariable(geoGenAtt.getValue(prop), geoGenAtt.getGenstarName())));
					}
				}
			}
		}
		return areaMatcherList;
	}

}
