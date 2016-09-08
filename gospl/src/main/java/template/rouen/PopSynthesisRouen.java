package template.rouen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gospl.algos.IDistributionInferenceAlgo;
import gospl.algos.IndependantHypothesisAlgo;
import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.GosplBasicSampler;
import gospl.algos.sampler.ISampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticPopGenerator;
import gospl.metamodel.IEntity;
import gospl.metamodel.IPopulation;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import io.util.GSPerformanceUtil;

public class PopSynthesisRouen {

	public static void main(String[] args) {
		// THE POPULATION TO BE GENERATED
		IPopulation population = null;

		// INSTANCIATE FACTORY
		GosplDistributionFactory df = null; 
		try {
			df = new GosplDistributionFactory(Paths.get(args[0].trim()));
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
		} catch (MatrixCoordinateException e) {
			e.printStackTrace();
		} 
		
		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to implement
		df.buildSamples();
		
		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator
		
		// so we collapse all distribution build from the data
		INDimensionalMatrix<IAttribute, IValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (IllegalControlTotalException e1) {
			e1.printStackTrace();
		} catch (MatrixCoordinateException e1) {
			e1.printStackTrace();
		}
		
		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		IDistributionInferenceAlgo<IAttribute, IValue> distributionInfAlgo = new IndependantHypothesisAlgo(true);
		ISampler<ACoordinate<IAttribute, IValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(distribution, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (GosplSamplerException e1) {
			e1.printStackTrace();
		}
		
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start generating synthetic population of size "+args[1], true);
		
		// BUILD THE GENERATOR
		ISyntheticPopGenerator ispGenerator = null;
		try {
			ispGenerator = new DistributionBasedGenerator(sampler);
		} catch (GosplSamplerException e) {
			e.printStackTrace();
		}
		
		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(Integer.parseInt(args[1]));
			gspu.sysoStempPerformance("End generating synthetic population: elapse time", PopSynthesisRouen.class.getName());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (GosplSamplerException e) {
			e.printStackTrace();
		}
		
		// MAKE REPORT
		String pathFolder = Paths.get(args[0].trim()).getParent().toString()+File.separator;
		String report = "RouenPopReport.csv";
		String export = "RouenPopExport.csv";
		try {
			gspu.sysoStempMessage("Start processing population to output files");
			Files.write(Paths.get(pathFolder+report), population.csvReport(";").getBytes());
			gspu.sysoStempPerformance("\treport done: "+pathFolder+report, PopSynthesisRouen.class.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: export to util class or into io project (with the problem of import IPopulation)
		try {
			CharSequence csvSep = ";";
			int individual = 1;
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathFolder+export));
			Collection<IAttribute> attributes = population.getPopulationAttributes();
			bw.write("Individual"+csvSep+attributes.stream().map(att -> att.getName()).collect(Collectors.joining(csvSep))+"\n");
			for(IEntity e : population){
				bw.write(String.valueOf((individual++)));
				for(IAttribute attribute : attributes)
					bw.write(csvSep+e.getValueForAttribute(attribute).getInputStringValue());
				bw.write("\n");
			}
			gspu.sysoStempPerformance("\texport done: "+pathFolder+export, PopSynthesisRouen.class.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
