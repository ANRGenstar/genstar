package spll.datamapper.matcher;

import java.io.IOException;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.ISPLFileIO;
import io.datareaders.georeader.geodat.GenstarFeature;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcherFactory<V extends ISPLVariable<?>, T> {

	public List<ISPLVariableFeatureMatcher<V, T>> getMatchers(GenstarFeature geoData, ISPLFileIO ancillaryFile) throws IOException, TransformException;
	
}
