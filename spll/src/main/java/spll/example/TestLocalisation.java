package spll.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.GSExportFactory;
import core.io.GSImportFactory;
import core.io.exception.InvalidFileTypeException;
import core.io.geo.GeoGSFileType;
import core.io.geo.IGSGeofile;
import core.io.geo.RasterFile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.GSFeature;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.util.GSBasicStats;
import core.util.GSPerformanceUtil;
import gospl.GosplSPTemplate;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.algo.IndependantHypothesisAlgo;
import gospl.algo.sampler.GosplBasicSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.metamodel.GosplEntity;
import gospl.metamodel.GosplPopulation;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.ASPLMapperBuilder;
import spll.datamapper.SPLAreaMapperBuilder;
import spll.datamapper.SPLMapper;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.variable.SPLVariable;
import spll.popmapper.SPUniformLocalizer;
import spll.popmapper.normalizer.SPLUniformNormalizer;

public class TestLocalisation {

	
	private static GosplPopulation generatePopulation(int targetPopulation ) {
		// INPUT ARGS
		
		Path confFile = Paths.get("sample/Rouen/Rouen_insee_indiv/GSC_RouenIndividual.xml");
		
		// THE POPULATION TO BE GENERATED
		GosplPopulation population = null;

		// INSTANCIATE FACTORY
		GosplDistributionFactory df = null; 
		try {
			df = new GosplDistributionFactory(confFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS 
		try {
			df.buildDistributions();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			e.printStackTrace();
		} 
		
		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator
		
		// so we collapse all distribution build from the data
		INDimensionalMatrix<ASurveyAttribute, AValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (IllegalControlTotalException e1) {
			e1.printStackTrace();
		}
		
		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		IDistributionInferenceAlgo<ASurveyAttribute, AValue> distributionInfAlgo = new IndependantHypothesisAlgo(true);
		ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(distribution, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}
		
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start generating synthetic population of size "+targetPopulation, true);
		
		// BUILD THE GENERATOR
		ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator(sampler);
		
		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulation);
			gspu.sysoStempPerformance("End generating synthetic population: elapse time", GosplSPTemplate.class.getName());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return population;
	}
	
	public static void main(String[] args) {
		int targetPopulation = 1000;
		IPopulation population = generatePopulation(targetPopulation);
		
		///////////////////////
		// INIT VARS FROM ARGS
		///////////////////////
		
		String stringPathToMainShapefile = "sample/Rouen/Rouen_shp/Rouen_iris.shp";
		String stringOfMainProperty = "P13_POP";
		
		String stringPathToBuildingFile = "sample/Rouen/Rouen_shp/buildings.shp";
		
		List<String> atts = Arrays.asList();
		
		/////////////////////
		// IMPORT DATA FILES
		/////////////////////
		
		core.util.GSPerformanceUtil gspu = new GSPerformanceUtil("Localisation of people in Rouen based on Iris population", true);
		ShapeFile sfAdmin = null;
		ShapeFile sfBuildings = null;
		try {
			sfAdmin = GSImportFactory.getShapeFile(stringPathToMainShapefile);
			sfBuildings = GSImportFactory.getShapeFile(stringPathToMainShapefile, atts);
			List<String> att = new ArrayList<String>();
			att.add("P13_POP");
			sfAdmin.addAttributes(new File("sample/Rouen/Rouen_insee_indiv/Rouen_iris.csv"), ',', "CODE_IRIS", "IRIS", att);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Collection<String> stringPathToAncilaryGeofiles = new ArrayList<>();
		stringPathToAncilaryGeofiles.add("sample/Rouen/Rouen_raster/CLC12_D076_RGF_S.tif");
	//	stringPathToAncilaryGeofiles.add("sample/Rouen/Rouen_raster/LC82000262015181LGN00_B2_rouen.tif");
	//	stringPathToAncilaryGeofiles.add("sample/Rouen/Rouen_raster/LC82000262015181LGN00_B3_rouen.tif");
	//	stringPathToAncilaryGeofiles.add("sample/Rouen/Rouen_raster/LC82000262015181LGN00_B4_rouen.tif");
		

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
		gspu.sysoStempPerformance("Input files data import: done\n", "Main");

		//////////////////////////////////
		// SETUP MAIN CLASS FOR REGRESSION
		//////////////////////////////////
		
		// Choice have been made to regress from areal data count
		ISPLRegressionAlgo<SPLVariable, Double> regressionAlgo = new LMRegressionOLS();
		
		ASPLMapperBuilder<SPLVariable, Double> spllBuilder = new SPLAreaMapperBuilder(
				sfAdmin, stringOfMainProperty, endogeneousVarFile, new ArrayList<>(),
				regressionAlgo);
		gspu.sysoStempPerformance("Setup MapperBuilder to proceed regression: done\n", "Main");
 
		// Setup main regressor class: SPLMapper
		SPLMapper<SPLVariable,Double> spl = null;
		boolean syso = false;
		try {
			spl = spllBuilder.buildMapper();
			if(syso){
				Map<SPLVariable, Double> regMap = spl.getRegression();
				gspu.sysoStempMessage("Regression parameter: \n"+Arrays.toString(regMap.entrySet().stream().map(e -> e.getKey()+" = "+e.getValue()+"\n").toArray()));
				gspu.sysoStempMessage("Intersect = "+spl.getIntercept());
			}
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
		} catch (IllegalRegressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ---------------------------------
		// Apply regression function to output
		// ---------------------------------
		
		// WARNING: not generic at all - or define 1st ancillary data file to be the one for output format
		RasterFile outputFormat = (RasterFile) endogeneousVarFile
				.stream().filter(file -> file.getGeoGSFileType().equals(GeoGSFileType.RASTER))
				.findFirst().get();
		spllBuilder.setNormalizer(new SPLUniformNormalizer(0, RasterFile.DEF_NODATA));
		float[][] pixelOutput = null;
		try { 
			pixelOutput = spllBuilder.buildOutput(outputFormat, false, true, new Double(targetPopulation));
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
		} catch (GSMapperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Double> outList = GSBasicStats.transpose(pixelOutput);
		GSBasicStats<Double> bs = new GSBasicStats<>(outList, Arrays.asList(RasterFile.DEF_NODATA.doubleValue()));
		gspu.sysoStempMessage("\nStatistics on output:\n"+bs.getStatReport());
		

		IGSGeofile outputFile = null;
		try {
			ReferencedEnvelope env = new ReferencedEnvelope( endogeneousVarFile.get(0).getEnvelope());
			
			outputFile = GSExportFactory.createGeotiffFile(new File("sample/Rouen/result.tif"), pixelOutput, env,outputFormat.getCoordRefSystem());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		
		///////////////////////
		// MATCH TO POPULATION
		///////////////////////
		
 		@SuppressWarnings("unchecked")
 		SPUniformLocalizer localizer = new SPUniformLocalizer(population, null/*sfAdmin*/, sfBuildings, "Band_0", "IRIS", "CODE_IRIS");
		
		// Normal used, based on the regression grid
 		//SPUniformLocalizer localizer = new SPUniformLocalizer(population, null/*sfAdmin*/, outputFile, "Band_0", "IRIS", "CODE_IRIS");
		localizer.localisePopulation();
		try {
			GSExportFactory.createShapeFile(new File("sample/Rouen/result.shp"), population, outputFormat.getCoordRefSystem());
		} catch (IOException | SchemaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
