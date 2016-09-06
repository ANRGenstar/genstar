package template.rouen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

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
import gospl.metamodel.IPopulation;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatrixCoordinateException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalControlTotalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MatrixCoordinateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		IDistributionInferenceAlgo<IAttribute, IValue> distributionInfAlgo = new IndependantHypothesisAlgo(true);
		ISampler<ACoordinate<IAttribute, IValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(distribution, new GosplBasicSampler());
		} catch (IllegalDistributionCreation e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (GosplSamplerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GosplSamplerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(population);

	}

}
