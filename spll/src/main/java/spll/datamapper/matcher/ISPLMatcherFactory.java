package spll.datamapper.matcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;
import spll.datamapper.variable.ISPLVariable;
import spll.entity.GSFeature;

public interface ISPLMatcherFactory<V extends ISPLVariable, T> {

	public List<ISPLMatcher<V, T>> getMatchers(GSFeature geoData, 
			IGSGeofile<? extends AGeoEntity> ancillaryFile) throws IOException, TransformException, InterruptedException, ExecutionException;

	public List<ISPLMatcher<V, T>> getMatchers(Collection<GSFeature> geoData, 
			IGSGeofile<? extends AGeoEntity> regressorsFiles) throws IOException, TransformException, InterruptedException, ExecutionException;
	
}
