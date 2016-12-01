package gospl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import core.io.GSExportFactory;
import core.io.exception.InvalidFileTypeException;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import core.util.GSPerformanceUtil;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.algo.IndependantHypothesisAlgo;
import gospl.algo.sampler.GosplBasicSampler;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.metamodel.GosplPopulation;

public class GosplSPTemplate {

	public static void main(final String[] args) {

		// INPUT ARGS
		final int targetPopulation = Integer.parseInt(args[0]);
		final Path confFile = Paths.get(args[1].trim());

		// THE POPULATION TO BE GENERATED
		GosplPopulation population = null;

		// INSTANCIATE FACTORY
		GosplDistributionFactory df = null;
		try {
			df = new GosplDistributionFactory(confFile);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}

		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS
		try {
			df.buildDistributions();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InvalidFileTypeException e) {
			e.printStackTrace();
		}

		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (final RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator

		// so we collapse all distribution build from the data
		INDimensionalMatrix<ASurveyAttribute, ASurveyValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		} catch (final IllegalControlTotalException e1) {
			e1.printStackTrace();
		}

		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final IDistributionInferenceAlgo<IDistributionSampler> distributionInfAlgo = new IndependantHypothesisAlgo();
		ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(distribution, new GosplBasicSampler());
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}

		final GSPerformanceUtil gspu =
				new GSPerformanceUtil("Start generating synthetic population of size " + targetPopulation);

		// BUILD THE GENERATOR
		final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator(sampler);

		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulation);
			gspu.sysoStempPerformance("End generating synthetic population: elapse time",
					GosplSPTemplate.class.getName());
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}

		// MAKE REPORT
		final String pathFolder = confFile.getParent().toString() + File.separator;
		final String report = "PopReport.csv";
		final String export = "PopExport.csv";
		
		try {
			GSExportFactory.createSurvey(new File(pathFolder+export), population);
		} catch (IOException | InvalidFileTypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			gspu.sysoStempMessage("\nStart processing population to output files");
			Files.write(Paths.get(pathFolder + report), population.csvReport(";").getBytes());
			gspu.sysoStempPerformance("\treport done: " + pathFolder + report, GosplSPTemplate.class.getName());
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

}
