package spll.datamapper.matcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import io.geofile.IGSGeofile;
import io.geofile.data.GSFeature;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcherFactory<V extends ISPLVariable<?>, T> {

	public List<ISPLVariableFeatureMatcher<V, T>> getMatchers(GSFeature geoData, 
			IGSGeofile ancillaryFile) throws IOException, TransformException;

	public List<ISPLVariableFeatureMatcher<V, T>> getMatchers(Collection<GSFeature> geoData, 
			IGSGeofile regressorsFiles) throws IOException, TransformException, InterruptedException, ExecutionException;
	
}
