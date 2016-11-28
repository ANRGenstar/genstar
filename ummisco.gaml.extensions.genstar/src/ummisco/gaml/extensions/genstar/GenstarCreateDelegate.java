/*********************************************************************************************
 *
 * 'GenstarCreateDelegate.java, in plugin ummisco.gaml.extensions.genstar, is part of the source code of the GAMA
 * modeling and simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gaml.extensions.genstar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.io.exception.InvalidFileTypeException;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.io.survey.attribut.value.RangeValue;
import core.io.survey.attribut.value.UniqueValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
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
import msi.gama.common.interfaces.ICreateDelegate;
import msi.gama.runtime.IScope;
import msi.gama.util.file.GamaXMLFile;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.CreateStatement;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

public class GenstarCreateDelegate implements ICreateDelegate {

	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof GamaXMLFile && ((GamaXMLFile) source).getRootTag(scope).equals("GosplConfiguration");
	}

	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object source, final Arguments init, final CreateStatement statement) {
		final GamaXMLFile file = (GamaXMLFile) source;

		// INPUT ARGS
		final int targetPopulation = max;
		final Path confFile = Paths.get(file.getPath(scope));

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
		} catch (final RuntimeException | IOException | InvalidFileTypeException e) {
			e.printStackTrace();
		}
		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (final RuntimeException | IOException | InvalidFileTypeException e) {
			e.printStackTrace();
		}

		// HERE IS A CHOICE TO MAKE BASED ON THE TYPE OF GENERATOR WE WANT:
		// Choice is made here to use distribution based generator

		// so we collapse all distribution build from the data
		INDimensionalMatrix<ASurveyAttribute, AValue, Double> distribution = null;
		try {
			distribution = df.collapseDistributions();
		} catch (final IllegalDistributionCreation | IllegalControlTotalException e1) {
			e1.printStackTrace();
		}

		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final IDistributionInferenceAlgo<ASurveyAttribute, AValue> distributionInfAlgo =
				new IndependantHypothesisAlgo(true);
		ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionSampler(distribution, new GosplBasicSampler());
		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}

		// BUILD THE GENERATOR
		final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator(sampler);

		// BUILD THE POPULATION
		try {
			population = ispGenerator.generate(targetPopulation);
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}

		final Collection<ASurveyAttribute> attributes = population.getPopulationAttributes();
		for (final GosplEntity e : population) {
			final Map<String, Object> agent = new HashMap<String, Object>();
			for (final ASurveyAttribute attribute : attributes) {
				final String name = attribute.getAttributeName();
				agent.put(name, getAttributeValue(scope, e, attribute));
			}
			statement.fillWithUserInit(scope, agent);
			inits.add(agent);
		}
		return true;

	}

	private Object getAttributeValue(final IScope scope, final GosplEntity entity, final ASurveyAttribute attribute) {
		final IType<?> type = getAttributeType(attribute);
		final AValue value = entity.getValueForAttribute(attribute);
		if (value instanceof UniqueValue) {
			return type.cast(scope, value.getStringValue(), null, false);
		} else if (value instanceof RangeValue) {
			return drawValue(scope, type, (RangeValue) value);
		} else
			return value.getStringValue();

	}

	private Object drawValue(final IScope scope, final IType<?> type, final RangeValue value) {
		final GSDataParser parser = new GSDataParser();
		switch (type.id()) {
			case IType.INT: {
				try {
					final List<Integer> bounds = parser.getRangedIntegerData(value.getInputStringValue(), false);
					return scope.getRandom().between(bounds.get(0), bounds.get(1));
				} catch (final GSIllegalRangedData e) {
					return 0;
				}
			}
			case IType.FLOAT: {
				try {
					final List<Double> bounds = parser.getRangedDoubleData(value.getInputStringValue(), false);
					return scope.getRandom().between(bounds.get(0), bounds.get(1));
				} catch (final GSIllegalRangedData e) {
					return 0d;
				}
			}
			default:
				return null;
		}
	}

	private IType<?> getAttributeType(final ASurveyAttribute attribute) {
		final GSEnumDataType gsType = attribute.getDataType();
		switch (gsType) {
			case Boolean:
				return Types.BOOL;
			case String:
				return Types.STRING;
			case Integer:
				return Types.INT;
			case Double:
				return Types.FLOAT;
		}
		return Types.STRING;
	}

	@Override
	public IType<?> fromFacetType() {
		return Types.FILE;
	}

}
