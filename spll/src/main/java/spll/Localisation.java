package spll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.GSImportFactory;
import io.datareaders.exception.InvalidFileTypeException;
import io.geofile.IGSGeofile;
import io.geofile.ShapeFile;
import io.geofile.data.GSFeature;
import io.util.GSPerformanceUtil;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.SPLMapper;
import spll.datamapper.variable.SPLRawVariable;

public class Localisation {

	static ShapeFile sfAdmin = null;
	
	// WARNING: list of regressor file should be transpose to the main CRS projection !!!
	// Geo data could have divergent referent projection => transposed should be made with care
	// 
	// HINT: See what is made in GAMA
	// get the right projection for data given long / lat
	// 
	// int idx = (int) (0.5 + (longitude + 186) / 6d);
	// boolean north = latitude > 0;
	// String newCode = "EPSG:"+(32600 + idx + (north ? 0 : 100));
	// CoordinateReferentSystem crs = CRS.decode(newCode, true);
	//
	static List<IGSGeofile> endogeneousVarFile = new ArrayList<>();

	/**
	 * args[0] = shape file of administrative & demographic information
	 * args[1] = The name (String) of the targeted dependant variable
	 * args[2...] = shape file or raster of the inference info (e.g. 30 x 30m of land use or cover)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Localisation of people in Bangkok based on Kwaeng (district) population", true);

		try {
			sfAdmin = GSImportFactory.getShapeFile(args[0]);
			for(int i = 2; i < args.length; i++)
				endogeneousVarFile.add(GSImportFactory.getGeofile(args[i]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Name propertyName = sfAdmin.getGeoData().stream()
				.findFirst().get().getProperties(args[1])
				.stream().findFirst().get().getName();

		gspu.sysoStempPerformance("Input files data import: done", "Main");

		ASPLMapperBuilder<SPLRawVariable, Double> mBuilder = new SPLAreaMapperBuilder(sfAdmin, propertyName, endogeneousVarFile);
		
		gspu.sysoStempPerformance("Setup MapperBuilder to proceed regression: done", "Main");

		SPLMapper<SPLRawVariable, Double> splMapper = null;
		try {
			splMapper = mBuilder.buildMapper();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(splMapper.getVariableSet().isEmpty()){
			gspu.sysoStempMessage("build mapper has failed because no geo-variable has been recognized and encoded");
			System.exit(1);
		} else {
			gspu.sysoStempPerformance("Mapper build: done", Localisation.class);
			gspu.sysoStempMessage("\t contains "+splMapper.getAttributes().size()+" attributes");
			gspu.sysoStempMessage("\t contains "+splMapper.getVariableSet().stream().count()+" mapped variables");
		}

		// WARNING: often regression function can be customize using an "intercept", that is a specific value for coordinate [0;0]
		// e.g. when area of a specific endogeneous variable is null, the population must be null
		gspu.sysoStempMessage("\nStart regression: ...");
		Map<SPLRawVariable, Double> coeffRegression = null;
		try {
			coeffRegression = splMapper.getRegression();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gspu.sysoStempPerformance("Regression: done", Localisation.class);

		// TODO: compute a feature specific coefficient of correction to exactly match the dependent variable of each feature
		// e.g. for each kwaeng we compute estimate population and then compute the ration of (real / estimate)
		gspu.sysoStempMessage("Start correction coefficient computation: ...");
		Map<GSFeature, Double> coeffCorrection = null;
		try {
			coeffCorrection = splMapper.getCorrectionCoefficient();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gspu.sysoStempPerformance("Correction coefficient computation: done", Localisation.class);

		coeffRegression.entrySet().stream().forEach(e -> gspu.sysoStempMessage(e.getKey().getName()+" ("+e.getKey().getValue()+") reg coeff = "+e.getValue()));
		coeffCorrection.entrySet().stream().forEach(e -> gspu.sysoStempMessage(e.getKey().getIdentifier().getID()+" reg correction coeff = "+e.getValue()));

		@SuppressWarnings("unused")
		IGSGeofile geoOutput = splMapper.getMappedRegression();
		// TODO: make geoFile exportable
	}

}
