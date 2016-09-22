package spll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.GeotiffFileIO;
import io.datareaders.georeader.IGeoGSFileIO;
import io.datareaders.georeader.ShapeFileIO;
import io.datareaders.georeader.exception.SPLFileIOException;
import io.datareaders.georeader.geodat.GSFeature;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.SPLMapper;
import spll.datamapper.variable.SPLRawVariable;

public class Localisation {

	static ShapeFileIO sfAdmin = null;
	@SuppressWarnings("rawtypes")
	static List<IGeoGSFileIO> endogeneousVarFile = new ArrayList<>();

	/**
	 * args[0] = shape file of administrative & demographic information
	 * args[1] = The name (String) of the targeted dependant variable
	 * args[2...] = shape file or raster of the inference info (e.g. 30 x 30m of land use or cover)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			sfAdmin = new ShapeFileIO(args[0]);

			for(int i = 2; i < args.length; i++){
				if(new File(args[i]).exists()){
					String landString = args[i];
					if(landString.contains(".shp"))
						endogeneousVarFile.add(new ShapeFileIO(landString));
					if(landString.contains(".tif"))
						endogeneousVarFile.add(new GeotiffFileIO(landString));
				} else
					throw new SPLFileIOException("The path "+args[i]+" does not represent a valid path");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SPLFileIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Name propertyName = sfAdmin.getGeoData().stream().findFirst().get().getProperties(args[1]).stream().findFirst().get().getName();

		System.out.println("["+Localisation.class.getSimpleName()+"] import data: done");

		ASPLMapperBuilder<SPLRawVariable, Double> mBuilder = new SPLAreaMapperBuilder(sfAdmin, propertyName, endogeneousVarFile);
		System.out.println("["+Localisation.class.getSimpleName()+"] setup MapperBuilder: done");

		SPLMapper<SPLRawVariable, Double> splMapper = null;
		try {
			splMapper = mBuilder.buildMapper();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(splMapper.getVariableSet().isEmpty()){
			System.out.println("["+Localisation.class.getSimpleName()+"] build mapper: failled");
			System.exit(1);
		} else {
			System.out.println("["+Localisation.class.getSimpleName()+"] build mapper: done");
			System.out.println("\t contains "+splMapper.getAttributes().size()+" attributes");
			System.out.println("\t contains "+splMapper.getVariableSet().stream().count()+" mapped variables");
		}

		// WARNING: often regression function can be customize using an "intercept", that is a specific value for coordinate [0;0]
		// e.g. when area of a specific endogeneous variable is null, the population must be null
		System.out.println("["+Localisation.class.getSimpleName()+"] start regression: ...");
		Map<SPLRawVariable, Double> coeffRegression = null;
		try {
			coeffRegression = splMapper.regression();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("["+Localisation.class.getSimpleName()+"] regression: done");

		// TODO: compute a feature specific coefficient of correction to exactly match the dependent variable of each feature
		// e.g. for each kwaeng we compute estimate population and then compute the ration of (real / estimate)
		System.out.println("["+Localisation.class.getSimpleName()+"] start correction coefficient computation: ...");
		Map<GSFeature, Double> coeffCorrection = null;
		try {
			coeffCorrection = splMapper.getCorrectionCoefficient();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("["+Localisation.class.getSimpleName()+"] correction coefficient computed");

		System.out.println("["+Localisation.class.getSimpleName()+"] "+coeffRegression.toString()+" of size "+coeffRegression.size());
		coeffRegression.entrySet().stream().forEach(e -> System.out.println(e.getKey().getName()+" reg coeff = "+e.getValue()));
		coeffCorrection.entrySet().stream().forEach(e -> System.out.println(e.getKey().getProperty(propertyName).getName()+" reg correction coeff = "+e.getValue()));

		// GeotiffFileIO geoOutput = computePopPerFeature(splMapper, coeffRegression, coeffCorrection);
		// System.out.println(geoOutput.toString());

	}

	// TODO: take 1.A) mapping between original features and variables 1.B) each variable coefficient from regression
	//			1.C) correction coefficient for each features and then 2) compute overall population per "variable defined space geography"
	// WARNING: from the beggining the output space geography must be define with variable space geography
	private static GeotiffFileIO computePopPerFeature(SPLMapper<SPLRawVariable, Double> splMapper,
			Map<SPLRawVariable, Double> coeffRegression, 
			Map<Feature, Double> coeffCorrection) {
		// TODO Auto-generated method stub
		return null;
	}

}
