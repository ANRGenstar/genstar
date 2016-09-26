package spll.datamapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.ShapeFileIO;
import spll.algo.ISPLRegressionAlgorithm;
import spll.datamapper.matcher.ISPLMatcherFactory;
import spll.datamapper.variable.ISPLVariable;

public abstract class ASPLMapperBuilder<V extends ISPLVariable<?>, T> {
	
	protected final ShapeFileIO mainFile;
	protected final Name propertyName;
	
	@SuppressWarnings("rawtypes")
	protected List<IGeoGSFileIO> ancillaryFiles;
	protected ISPLMatcherFactory<V, T> matcherFactory;
	
	protected ISPLRegressionAlgorithm<V, T> regressionAlgorithm;
	
	public ASPLMapperBuilder(ShapeFileIO mainFile, Name propertyName,
			@SuppressWarnings("rawtypes") List<IGeoGSFileIO> ancillaryFiles) {
		this.mainFile = mainFile;
		this.propertyName = propertyName;
		this.ancillaryFiles = ancillaryFiles;
	}
	
	public void setRegressionAlgorithm(ISPLRegressionAlgorithm<V, T> regressionAlgorithm){
		this.regressionAlgorithm = regressionAlgorithm;
	}
	
	public void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}
	
	public abstract void mapRegressorFile(@SuppressWarnings("rawtypes") IGeoGSFileIO regressorFile) 
			throws IOException, TransformException, InterruptedException, ExecutionException;
	
	public abstract SPLMapper<V, T> buildMapper() throws IOException, TransformException, InterruptedException, ExecutionException;
	
}
