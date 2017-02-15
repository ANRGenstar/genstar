package gospl.distribution.matrix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.metamodel.pop.APopulationAttribute;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.control.AControl;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;

public class TestFullNDimensionalMatrix {

	private double probaHomme;
	private double probaFemme;
	private double delta;
	
	@Before
	public void setUp() throws Exception {
		probaHomme = 0.47;
		probaFemme = 0.53;
		delta = 0.001;
	}	

	@After
	public void tearDown() throws Exception {
	}
	
	
	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAgeGender() {
		
		try {
			final GosplAttributeFactory gaf = new GosplAttributeFactory();
			
			Set<APopulationAttribute> attributes = new HashSet<>();
			attributes.add(gaf.createAttribute("Genre", GSEnumDataType.String, 
					Arrays.asList("Homme", "Femme"), GSEnumAttributeType.unique));
			attributes.add(gaf.createAttribute("Age", GSEnumDataType.Integer, 
					Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus"), GSEnumAttributeType.range));
	
			
			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);
			
			m.setValue(0.15*probaHomme, "Genre", "Homme", "Age", "0-5");
			m.setValue(0.20*probaHomme, "Genre", "Homme", "Age", "6-15");
			m.setValue(0.30*probaHomme, "Genre", "Homme", "Age", "16-25");
			m.setValue(0.20*probaHomme, "Genre", "Homme", "Age", "26-40");
			m.setValue(0.10*probaHomme, "Genre", "Homme", "Age", "40-55");
			m.setValue(0.05*probaHomme, "Genre", "Homme", "Age", "55 et plus");
			
			m.setValue(0.10*probaFemme, "Genre", "Femme", "Age", "0-5");
			m.setValue(0.20*probaFemme, "Genre", "Femme", "Age", "6-15");
			m.setValue(0.30*probaFemme, "Genre", "Femme", "Age", "16-25");
			m.setValue(0.25*probaFemme, "Genre", "Femme", "Age", "26-40");
			m.setValue(0.10*probaFemme, "Genre", "Femme", "Age", "40-55");
			m.setValue(0.05*probaFemme, "Genre", "Femme", "Age", "55 et plus");
			
		return m;
		} catch (GSIllegalRangedData e) {
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}
	

	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAgeCSP() {
		
		try {
			final GosplAttributeFactory gaf = new GosplAttributeFactory();
			
			Set<APopulationAttribute> attributes = new HashSet<>();
			attributes.add(gaf.createAttribute("Age", GSEnumDataType.Integer, 
					Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus"), GSEnumAttributeType.range));
			attributes.add(gaf.createAttribute("Activite", GSEnumDataType.String, 
					Arrays.asList("Sans emploi", "Précaire", "Employé"), GSEnumAttributeType.unique));
			
			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory()
					.createEmptyDistribution(attributes);
			
			AFullNDimensionalMatrix<Double> ageSexe = this.generateGlobalFrequencyAgeGender();
			double age05 = ageSexe.getVal("Age", "0-5").getValue();
			double age615 = ageSexe.getVal("Age", "6-15").getValue();
			double age1625 = ageSexe.getVal("Age", "16-25").getValue();
			double age2640 = ageSexe.getVal("Age", "26-40").getValue();
			double age4055 = ageSexe.getVal("Age", "40-55").getValue();
			double age55plus = ageSexe.getVal("Age", "55 et plus").getValue();
			
			m.setValue(age05, "Activite", "Sans emploi", "Age", "0-5");
			m.setValue(age615, "Activite", "Sans emploi", "Age", "6-15");
			m.setValue(0.3*age1625, "Activite", "Sans emploi", "Age", "16-25");
			m.setValue(0.1*age2640, "Activite", "Sans emploi", "Age", "26-40");
			m.setValue(0.2*age4055, "Activite", "Sans emploi", "Age", "40-55");
			m.setValue(0.4*age55plus, "Activite", "Sans emploi", "Age", "55 et plus");
			
			m.setValue(0.0, "Activite", "Précaire", "Age", "0-5");
			m.setValue(0.0, "Activite", "Précaire", "Age", "6-15");
			m.setValue(0.3*age1625, "Activite", "Précaire", "Age", "16-25");
			m.setValue(0.2*age2640, "Activite", "Précaire", "Age", "26-40");
			m.setValue(0.2*age4055, "Activite", "Précaire", "Age", "40-55");
			m.setValue(0.1*age55plus, "Activite", "Précaire", "Age", "55 et plus");
			
			m.setValue(0.0, "Activite", "Employé", "Age", "0-5");
			m.setValue(0.0, "Activite", "Employé", "Age", "6-15");
			m.setValue(0.3*age1625, "Activite", "Employé", "Age", "16-25");
			m.setValue(0.7*age2640, "Activite", "Employé", "Age", "26-40");
			m.setValue(0.6*age4055, "Activite", "Employé", "Age", "40-55");
			m.setValue(0.5*age55plus, "Activite", "Employé", "Age", "55 et plus");
						
			m.normalize();
			
			return m;
			
		} catch (GSIllegalRangedData e) {
			// In any case, do not kill the dev. 
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}
	

	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAge2CSP() {
		
		try {
			final GosplAttributeFactory gaf = new GosplAttributeFactory();
			
			Set<APopulationAttribute> attributes = new HashSet<>();
			attributes.add(gaf.createAttribute("Age2", GSEnumDataType.Integer, 
					Arrays.asList("moins de 15", "16-25", "26-55", "55 et plus"), GSEnumAttributeType.range));
			attributes.add(gaf.createAttribute("Activite", GSEnumDataType.String, 
					Arrays.asList("Sans emploi", "Précaire", "Employé"), GSEnumAttributeType.unique));
			
			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);
			AFullNDimensionalMatrix<Double> ageSexe = this.generateGlobalFrequencyAgeGender();
			double age05 = ageSexe.getVal("Age", "0-5").getValue();
			double age615 = ageSexe.getVal("Age", "6-15").getValue();
			double age1625 = ageSexe.getVal("Age", "16-25").getValue();
			double age2640 = ageSexe.getVal("Age", "26-40").getValue();
			double age4055 = ageSexe.getVal("Age", "40-55").getValue();
			double age55plus = ageSexe.getVal("Age", "55 et plus").getValue();
			
			m.setValue(age05+age615, "Activite", "Sans emploi", "Age2", "moins de 15");
			m.setValue(0.3*age1625, "Activite", "Sans emploi", "Age2", "16-25");
			m.setValue(0.3*(age2640+age4055), "Activite", "Sans emploi", "Age2", "26-55");
			m.setValue(0.4*age55plus, "Activite", "Sans emploi", "Age2", "55 et plus");
			
			m.setValue(0.0, "Activite", "Précaire", "Age2", "moins de 15");
			m.setValue(0.3*age1625, "Activite", "Précaire", "Age2", "16-25");
			m.setValue(0.4*(age2640+age4055), "Activite", "Précaire", "Age2", "26-55");
			m.setValue(0.1*age55plus, "Activite", "Précaire", "Age2", "55 et plus");
			
			m.setValue(0.0, "Activite", "Employé", "Age2", "moins de 15");
			m.setValue(0.3*age1625, "Activite", "Employé", "Age2", "16-25");
			m.setValue(0.7*(age2640+age4055), "Activite", "Employé", "Age2", "26-55");
			m.setValue(0.5*age55plus, "Activite", "Employé", "Age2", "55 et plus");
						
			m.normalize();
			
			return m;
			
		} catch (GSIllegalRangedData e) {
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}
	
	/**
	 * generates a segmented matrix with no mapping, based on  age X gender and age X csp
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected ASegmentedNDimensionalMatrix<Double> generateSegmentedNoMappingAgePyramidAndCSP() {
		try {
			return GosplNDimensionalMatrixFactory.getFactory().createDistributionFromDistributions(
					generateGlobalFrequencyAgeGender(),
					generateGlobalFrequencyAgeCSP()
					);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("error in the construction of the test case", e);
		}
		
	}
	
	/**
	 * generates a segmented matrix with a mapping, based on  age X gender and age2 X csp
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected ASegmentedNDimensionalMatrix<Double> generateSegmentedWithMappingAgePyramidAndCSP() {
		
		
		try {
			final GosplAttributeFactory gaf = new GosplAttributeFactory();
			

			// map age2 to age
			Map<Set<String>, Set<String>> mapperAge2toAge = new HashMap<>();
			GenstarConfigurationFile.addMapper(mapperAge2toAge, Arrays.asList("moins de 15"),  Arrays.asList("0-5", "6-15"));
			GenstarConfigurationFile.addMapper(mapperAge2toAge, Arrays.asList("16-25"), Arrays.asList("16-25"));
			GenstarConfigurationFile.addMapper(mapperAge2toAge, Arrays.asList("26-55"), Arrays.asList("26-40","40-55"));
			GenstarConfigurationFile.addMapper(mapperAge2toAge, Arrays.asList("55 et plus"), Arrays.asList("55 et plus"));
			
			// age x genre 
			APopulationAttribute attGenre = gaf.createAttribute("Genre", GSEnumDataType.String, 
					Arrays.asList("Homme", "Femme"), GSEnumAttributeType.unique);
			APopulationAttribute attAge = gaf.createAttribute("Age", GSEnumDataType.Integer, 
					Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus"), GSEnumAttributeType.range);
			APopulationAttribute attAge2 = gaf.createAttribute("Age2", GSEnumDataType.Integer, 
					Arrays.asList("moins de 15", "16-25", "26-55", "55 et plus"), GSEnumAttributeType.range, attAge, mapperAge2toAge);
			APopulationAttribute attCSP = gaf.createAttribute("Activite", GSEnumDataType.String, 
					Arrays.asList("Sans emploi", "Précaire", "Employé"), GSEnumAttributeType.unique);
			
			AFullNDimensionalMatrix<Double> mAgeGender = GosplNDimensionalMatrixFactory.getFactory()
					.createEmptyDistribution(attAge,attGenre);
			
			mAgeGender.setValue(0.15*probaHomme, "Genre", "Homme", "Age", "0-5");
			mAgeGender.setValue(0.20*probaHomme, "Genre", "Homme", "Age", "6-15");
			mAgeGender.setValue(0.30*probaHomme, "Genre", "Homme", "Age", "16-25");
			mAgeGender.setValue(0.20*probaHomme, "Genre", "Homme", "Age", "26-40");
			mAgeGender.setValue(0.10*probaHomme, "Genre", "Homme", "Age", "40-55");
			mAgeGender.setValue(0.05*probaHomme, "Genre", "Homme", "Age", "55 et plus");
			
			mAgeGender.setValue(0.10*probaFemme, "Genre", "Femme", "Age", "0-5");
			mAgeGender.setValue(0.20*probaFemme, "Genre", "Femme", "Age", "6-15");
			mAgeGender.setValue(0.30*probaFemme, "Genre", "Femme", "Age", "16-25");
			mAgeGender.setValue(0.25*probaFemme, "Genre", "Femme", "Age", "26-40");
			mAgeGender.setValue(0.10*probaFemme, "Genre", "Femme", "Age", "40-55");
			mAgeGender.setValue(0.05*probaFemme, "Genre", "Femme", "Age", "55 et plus");
			
			mAgeGender.normalize();
			
			double age05 = mAgeGender.getVal("Age", "0-5").getValue();
			double age615 = mAgeGender.getVal("Age", "6-15").getValue();
			double age1625 = mAgeGender.getVal("Age", "16-25").getValue();
			double age2640 = mAgeGender.getVal("Age", "26-40").getValue();
			double age4055 = mAgeGender.getVal("Age", "40-55").getValue();
			double age55plus = mAgeGender.getVal("Age", "55 et plus").getValue();
			
			// age2 X csp

			AFullNDimensionalMatrix<Double> mAge2CSP = GosplNDimensionalMatrixFactory.getFactory()
					.createEmptyDistribution(attAge2,attCSP);
			
			mAge2CSP.setValue(age05+age615, "Activite", "Sans emploi", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3*age1625, "Activite", "Sans emploi", "Age2", "16-25");
			mAge2CSP.setValue(0.3*(age2640+age4055), "Activite", "Sans emploi", "Age2", "26-55");
			mAge2CSP.setValue(0.4*age55plus, "Activite", "Sans emploi", "Age2", "55 et plus");
			
			mAge2CSP.setValue(0.0, "Activite", "Précaire", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3*age1625, "Activite", "Précaire", "Age2", "16-25");
			mAge2CSP.setValue(0.4*(age2640+age4055), "Activite", "Précaire", "Age2", "26-55");
			mAge2CSP.setValue(0.1*age55plus, "Activite", "Précaire", "Age2", "55 et plus");
			
			mAge2CSP.setValue(0.0, "Activite", "Employé", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3*age1625, "Activite", "Employé", "Age2", "16-25");
			mAge2CSP.setValue(0.7*(age2640+age4055), "Activite", "Employé", "Age2", "26-55");
			mAge2CSP.setValue(0.5*age55plus, "Activite", "Employé", "Age2", "55 et plus");
						
			mAge2CSP.normalize();
			
			return GosplNDimensionalMatrixFactory.getFactory().createDistributionFromDistributions(
					mAge2CSP, mAgeGender
					);
		} catch (GSIllegalRangedData e) {
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("error in the construction of the test case", e);
		}
		
	}
	
	
	@Test
	public void testGetValOneAtoms() {
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		assertEquals("wrong storage", new Double(0.15*probaHomme), agePyramid.getVal("Genre", "Homme", "Age", "0-5").getValue());
		assertEquals("wrong storage", new Double(0.2*probaFemme), agePyramid.getVal("Genre", "Femme", "Age", "6-15").getValue());
		assertEquals("wrong storage", new Double(0.05*probaHomme), agePyramid.getVal("Genre", "Homme", "Age", "55 et plus").getValue());

	}
	
	@Test
	public void testGetValOneDimensionAgePyramid() {
		
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		
		assertEquals("wrong computation of the column", new Double(1.0), agePyramid.getVal().getValue());
		assertEquals("wrong computation of the column", new Double(probaHomme), 
				agePyramid.getVal("Genre","Homme").getValue(), delta);
		assertEquals("wrong computation of the column", new Double(probaFemme), 
				agePyramid.getVal("Genre","Femme").getValue(), delta);

		assertEquals("wrong computation of the column", new Double(0.15*probaHomme+0.10*probaFemme), 
				agePyramid.getVal("Age","0-5").getValue(), delta);

	}
	
	@Test
	public void testGetValOneDimensionCSP() {
		
		AFullNDimensionalMatrix<Double> ageCSP = generateGlobalFrequencyAgeCSP();

		assertEquals("wrong computation of the total", new Double(1.0), ageCSP.getVal().getValue(), delta);

	}

	@Test
	public void testGetValSubSetAgePyramid() {
		
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		
		assertEquals("wrong computation of the probas", new Double(0.15*probaHomme+0.20*probaHomme), 
				agePyramid.getVal("Genre","Homme","Age","0-5", "Age", "6-15").getValue(), delta);

		assertEquals("wrong computation of the probas", agePyramid.getVal("Genre","Homme","Genre","Femme","Age","0-5", "Age", "6-15").getValue(), 
				agePyramid.getVal("Age","0-5", "Age", "6-15").getValue(), delta);

	}
	
	@Test
	public void testGetValSegmentedNoMappingAll() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		assertEquals("joined probability does not sum to the count of inner matrixes", 
				seg.jointDistributionSet.size(), seg.getVal().getValue().doubleValue(), delta);
		
	}
	

	@Test
	public void testGetValSegmentedNoMappingJoined() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		
		AControl<Double> cAgeGender = seg.getVal("Age", "26-40", "Genre","Homme");
		AControl<Double> cAge = seg.getVal("Age", "26-40");
		AControl<Double> cAgeCSP = seg.getVal("Age", "26-40", "Activite","Sans emploi");
		
		AControl<Double> calculatedProba = cAgeGender.multiply(cAgeCSP.multiply(1 / cAge.getValue()));
		
		assertEquals(
				"wrong joined distribution", calculatedProba.getValue(), 
				seg.getVal("Age","26-40","Genre","Homme","Activite","Sans emploi").getValue(), 
				delta
				);
		
	}


	@Test
	public void testGetValSegmentedWithMapping() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		
		AControl<Double> cAgeGender = seg.getVal("Age", "26-40", "Age", "40-55", "Genre","Homme");
		AControl<Double> cAge = seg.getVal("Age", "26-40", "Age", "40-55");
		AControl<Double> cAgeCSP = seg.getVal("Age", "26-40", "Age", "40-55", "Activite","Sans emploi");
		
		AControl<Double> calculatedProba = cAgeGender.multiply(cAgeCSP.multiply(1 / cAge.getValue()));
		
		ASegmentedNDimensionalMatrix<Double> seg2 = generateSegmentedWithMappingAgePyramidAndCSP();
		
		assertEquals(
				"wrong joined distribution", calculatedProba.getValue(), 
				seg2.getVal("Age2","26-55","Genre","Homme","Activite","Sans emploi").getValue(), 
				delta
				);

		assertEquals("joined probability does not sum to 1", new Double(1.0), seg2.getVal());
	}

}
