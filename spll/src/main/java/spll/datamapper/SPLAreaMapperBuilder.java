package spll.datamapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.ShapeFileIO;
import spll.algo.LMRegressionAlgorithm;
import spll.datamapper.matcher.SPLAreaMatcherFactory;
import spll.datamapper.variable.SPLRawVariable;

public class SPLAreaMapperBuilder extends ASPLMapperBuilder<SPLRawVariable, Double> {

	private SPLMapper<SPLRawVariable, Double> mapper;
	
	public SPLAreaMapperBuilder(ShapeFileIO mainFile, Name propertyName,
			List<IGeoGSFileIO> ancillaryFiles) {
		super(mainFile, propertyName, ancillaryFiles);
		super.setRegressionAlgorithm(new LMRegressionAlgorithm());
		super.setMatcherFactory(new SPLAreaMatcherFactory());
	}

	@Override
	public SPLMapper<SPLRawVariable, Double> buildMapper() throws IOException, TransformException, InterruptedException, ExecutionException {
		mapper = new SPLMapper<>();
		mapper.setMainSPLFile(mainFile);
		mapper.setMainProperty(propertyName);
		mapper.setRegAlgo(regressionAlgorithm);
		mapper.setMatcherFactory(matcherFactory);
		for(IGeoGSFileIO file : ancillaryFiles)
			mapRegressorFile(file);
		return mapper;
	}

	@Override
	public void mapRegressorFile(IGeoGSFileIO regressorFile) throws IOException, TransformException, InterruptedException, ExecutionException {
		if(mapper != null)
			mapper.insertMatchedVariable(regressorFile);
	}

}
