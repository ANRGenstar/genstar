package spll.algo.variable;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.Feature;

import io.datareaders.georeader.ISPLFileIO;

public class SPLMatcherFactory<F extends Feature, V extends ISPLVariable<?>, T> {

	public List<ISPLVariableFeatureMatcher<F, V, T>> getAreaMatchers(F feature, ISPLFileIO<F> file) {
		List<ISPLVariableFeatureMatcher<F, V, T>> areaMatcherList = new ArrayList<>();
		for(F feat : file.getFeatures()){

		}
		return areaMatcherList;
	}

}
