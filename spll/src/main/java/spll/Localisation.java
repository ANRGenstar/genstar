package spll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import io.data.geo.GeotiffFile;
import io.data.geo.IGSGeofile;
import io.data.geo.ShapeFile;
import io.data.geo.attribute.IGeoValue;
import io.data.readers.GSImportFactory;
import io.data.readers.exception.InvalidFileTypeException;
import io.util.GSPerformanceUtil;
import spll.algo.LMRegressionGLSAlgorithm;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.SPLMapper;
import spll.datamapper.variable.SPLVariable;

public class Localisation {

	/**
	 * args[0] = The path to available dir to put created files in
	 * args[1] = Main shape file that contains geometry for the dependent variable
	 * args[2] = The name (String) of the targeted dependent variable
	 * args[3] = String of variables to exclude from regression, using ';' to separate from one another
	 * args[4...] = Shape or raster files that contain explanatory variables (e.g. 30 x 30m raster image of land use or cover)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		String outputFileName = "spll_output.tif";
		
		///////////////////////
		// INIT VARS FROM ARGS
		///////////////////////
		
		String stringPath = args[0];
		String stringPathToMainShapefile = args[1];
		String stringOfMainProperty = args[2];
		Collection<String> regVarName = Arrays.asList(args[3].split(";"));
		Collection<String> stringPathToAncilaryGeofiles = new ArrayList<>();
		for(int i = 4; i < args.length; i++)
			stringPathToAncilaryGeofiles.add(args[i]);
		
		/////////////////////
		// IMPORT DATA FILES
		/////////////////////
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Localisation of people in Bangkok based on Kwaeng (district) population", true);
		ShapeFile sfAdmin = null;
		try {
			sfAdmin = GSImportFactory.getShapeFile(stringPathToMainShapefile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		List<IGSGeofile> endogeneousVarFile = new ArrayList<>();
		for(String path : stringPathToAncilaryGeofiles){
			try {
				endogeneousVarFile.add(GSImportFactory.getGeofile(path));
			} catch (IllegalArgumentException | TransformException | IOException | InvalidFileTypeException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		
		Name propertyName = sfAdmin.getGeoData().stream()
				.findFirst().get().getProperties(stringOfMainProperty)
				.stream().findFirst().get().getName();

		Collection<IGeoValue> regVariables;
		if(regVarName.isEmpty())
			regVariables = Collections.emptyList();
		else {
			// TODO: move to utility method in spll.util
			Collection<IGeoValue> vals = endogeneousVarFile.parallelStream()
					.flatMap(file -> file.getGeoValues().stream())
					.collect(Collectors.toSet());
			regVariables = vals.stream()
				.filter(var -> var.isNumericalData() ? 
						regVarName.stream().anyMatch(v -> Double.valueOf(v) == var.getNumericalValue().doubleValue()) 
						: regVarName.contains(var.getValue()))
				.collect(Collectors.toSet());
		}
		
		gspu.sysoStempPerformance("Input files data import: done\n", "Main");

		//////////////////////////////////
		// SETUP MAIN CLASS FOR REGRESSION
		//////////////////////////////////
		
		// Choice have been made to regress from areal data count
		ASPLMapperBuilder<SPLVariable, Double> mBuilder = new SPLAreaMapperBuilder(
				sfAdmin, propertyName, endogeneousVarFile, regVariables,
				new LMRegressionGLSAlgorithm());
		gspu.sysoStempPerformance("Setup MapperBuilder to proceed regression: done\n", "Main");

		// Setup main regressor class: SPLMapper
		SPLMapper<SPLVariable, Double> splMapper = null;
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
			//gspu.sysoStempMessage(splMapper.getVarMatrix().values().parallelStream().map(vM -> vM.toString()).reduce("", (s1, s2) -> s1+"\n"+s2));
		}
		
		////////////////////
		// START REGRESSION
		////////////////////
		
		Map<SPLVariable, Double> reg = null;
		try {
			reg = splMapper.getRegression();
		} catch (IllegalRegressionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		gspu.sysoStempMessage(reg.entrySet().stream().map(e -> "Var_"+e.getKey()+" = "+e.getValue()).reduce("", (s1, s2) -> s1+"\n"+s2));
			
		try { 
			mBuilder.buildOutput(new File(stringPath+File.separator+outputFileName), (GeotiffFile) endogeneousVarFile.get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
