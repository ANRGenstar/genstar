package spll.datamapper.matcher;

import java.io.IOException;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.ISPLFileIO;
import io.datareaders.georeader.geodat.GSFeature;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcherFactory<V extends ISPLVariable<?>, T> {

	public List<ISPLVariableFeatureMatcher<V, T>> getMatchers(GSFeature geoData, ISPLFileIO ancillaryFile) throws IOException, TransformException;
	
}
