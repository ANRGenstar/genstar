package gospl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.exception.InvalidFileTypeException;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.util.GSPerformanceUtil;
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

public class GosplSPTemplate {

	public static void main(String[] args) {
		
		// INPUT ARGS
		int targetPopulation = Integer.parseInt(args[0]);
		Path confFile = Paths.get(args[1].trim());
		
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
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFileTypeException e) {
			e.printStackTrace();
		} 
		
		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		// MAKE REPORT
		String pathFolder = confFile.getParent().toString()+File.separator;
		String report = "PopReport.csv";
		String export = "PopExport.csv";
		try {
			gspu.sysoStempMessage("\nStart processing population to output files");
			Files.write(Paths.get(pathFolder+report), population.csvReport(";").getBytes());
			gspu.sysoStempPerformance("\treport done: "+pathFolder+report, GosplSPTemplate.class.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: move to core io => generic method to export report of IPopolution or any other IEntity collection
		try {
			CharSequence csvSep = ";";
			int individual = 1;
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathFolder+export));
			Collection<ASurveyAttribute> attributes = population.getPopulationAttributes();
			bw.write("Individual"+csvSep+attributes.stream().map(att -> att.getAttributeName()).collect(Collectors.joining(csvSep))+"\n");
			for(GosplEntity e : population){
				bw.write(String.valueOf((individual++)));
				for(ASurveyAttribute attribute : attributes)
					bw.write(csvSep+e.getValueForAttribute(attribute).getStringValue());
				bw.write("\n");
			}
			gspu.sysoStempPerformance("\texport done: "+pathFolder+export, GosplSPTemplate.class.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
