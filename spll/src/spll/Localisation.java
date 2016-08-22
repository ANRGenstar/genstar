package spll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import spll.algo.ISPLRegressionAlgorithm;
import spll.algo.LMRegressionAlgorithm;
import spll.algo.variable.SPLMapper;
import spll.algo.variable.SPLMatcherFactory;
import spll.algo.variable.SPLRawVariable;
import spll.io.exception.IllegalRegressionException;
import spll.io.file.GeotiffFileIO;
import spll.io.file.ISPLFileIO;
import spll.io.file.ShapeFileIO;
import spll.io.file.exception.SPLFileIOException;

public class Localisation {
	
	static ShapeFileIO sfAdmin = null;
	static List<ISPLFileIO<Feature>> endogeneousVarFile = new ArrayList<>();

	/**
	 * args[0] = shape file of administrative & demographic information
	 * args[1] = The name (String) of the targeted dependant variable
	 * args[2...] = shape file or raster of the inference info (e.g. 30 x 30m of land use or cover)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			getLayersAndDisplay();
		} catch (Exception e1) {
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
		} 
		
		ISPLRegressionAlgorithm<SPLRawVariable> splRegFunction = new LMRegressionAlgorithm();
		Name propertyName = sfAdmin.getFeatures()
				.stream().filter(feat -> !feat.getProperties((String) args[1]).isEmpty()).findFirst()
				.get().getProperty((String) args[1]).getName();
		
		// TODO: create a feature / variable mapper
		SPLMapper<Feature, SPLRawVariable, Double> splMapper = new SPLMapper<>(sfAdmin, propertyName, new SPLMatcherFactory<>(), splRegFunction);
		
		// TODO: compute the area of each endogeneous variable through all features of the sfAdmin file
		// e.g. in bkk compute land use area for each kwaeng
		// WARNING: How to match patches (e.g. pixel of rasters) to demographic input geography (e.g. kwaeng) -> centroïde
		// HINT: see how to match Feature from shp file and *** (see geotools to find the correct object) from tif file to ISPLVariable 
		splMapper.insertMatchedVariable(endogeneousVarFile);
		
		// WARNING: often regression function can be customize using an "intercept", that is a specific value for coordinate [0;0]
		// e.g. when area of a specific endogeneous variable is null, the population must be null
		Map<SPLRawVariable, Double> coeffRegression = null;
		try {
			coeffRegression = splMapper.regression();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: compute a feature specific coefficient of correction to exactly match the dependent variable of each feature
		// e.g. for each kwaeng we compute estimate population and then compute the ration of (real / estimate)
		Map<Feature, Double> coeffCorrection = null;
		try {
			coeffCorrection = splMapper.getCorrectionCoefficient();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GeotiffFileIO geoOutput = computePopPerFeature(splMapper, coeffRegression, coeffCorrection);
		
		System.out.println(geoOutput.toString());
		
	}

	// TODO: take 1.A) mapping between original features and variables 1.B) each variable coefficient from regression
	//			1.C) correction coefficient for each features and then 2) compute overall population per "variable defined space geography"
	// WARNING: from the beggining the output space geography must be define with variable space geography
	private static GeotiffFileIO computePopPerFeature(SPLMapper<Feature, SPLRawVariable, Double> splMapper,
			Map<SPLRawVariable, Double> coeffRegression, 
			Map<Feature, Double> coeffCorrection) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static void getLayersAndDisplay() throws Exception {
        List<Parameter<?>> list = new ArrayList<Parameter<?>>();
        list.add(new Parameter<File>("regressor", File.class, "Image",
                "GeoTiff regressors",
                new KVP( Parameter.EXT, "tif", Parameter.EXT, "jpg")));
        list.add(new Parameter<File>("admin", File.class, "Shapefile",
                "Basic administrativ info", new KVP(Parameter.EXT, "shp")));

        JParameterListWizard wizard = new JParameterListWizard("Image Lab",
                "Fill in the following layers", list);
        int finish = wizard.showModalDialog();

        if (finish != JWizard.FINISH) {
            System.exit(0);
        }
        sfAdmin = new ShapeFileIO(((File) wizard.getConnectionParameters().get("admin")).getAbsolutePath());
        endogeneousVarFile.add(new GeotiffFileIO(((File) wizard.getConnectionParameters().get("regressor")).getAbsolutePath()));
        
    }
	
}
