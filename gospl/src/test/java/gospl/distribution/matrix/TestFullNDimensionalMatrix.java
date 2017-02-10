package gospl.distribution.matrix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.metamodel.pop.APopulationAttribute;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.entity.attribute.GSEnumAttributeType;
import gospl.entity.attribute.GosplAttributeFactory;

public class TestFullNDimensionalMatrix {

	@Before
	public void setUp() throws Exception {
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

			m.setValue(0.15/3, "Genre", "Homme", "Age", "0-5");
			m.setValue(0.20/3, "Genre", "Homme", "Age", "6-15");
			m.setValue(0.30/3, "Genre", "Homme", "Age", "16-25");
			m.setValue(0.20/3, "Genre", "Homme", "Age", "26-40");
			m.setValue(0.10/3, "Genre", "Homme", "Age", "40-55");
			m.setValue(0.05/3, "Genre", "Homme", "Age", "55 et plus");
			
			m.setValue(0.10*2/3, "Genre", "Femme", "Age", "0-5");
			m.setValue(0.20*2/3, "Genre", "Femme", "Age", "6-15");
			m.setValue(0.30*2/3, "Genre", "Femme", "Age", "16-25");
			m.setValue(0.25*2/3, "Genre", "Femme", "Age", "26-40");
			m.setValue(0.10*2/3, "Genre", "Femme", "Age", "40-55");
			m.setValue(0.05*2/3, "Genre", "Femme", "Age", "55 et plus");
			
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
			
			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);
			
			m.setValue(1.0, "Activite", "Sans emploi", "Age", "0-5");
			m.setValue(1.0, "Activite", "Sans emploi", "Age", "6-15");
			m.setValue(0.3, "Activite", "Sans emploi", "Age", "16-25");
			m.setValue(0.1, "Activite", "Sans emploi", "Age", "26-40");
			m.setValue(0.2, "Activite", "Sans emploi", "Age", "40-55");
			m.setValue(0.4, "Activite", "Sans emploi", "Age", "55 et plus");
			
			m.setValue(0.0, "Activite", "Précaire", "Age", "0-5");
			m.setValue(0.0, "Activite", "Précaire", "Age", "6-15");
			m.setValue(0.3, "Activite", "Précaire", "Age", "16-25");
			m.setValue(0.2, "Activite", "Précaire", "Age", "26-40");
			m.setValue(0.2, "Activite", "Précaire", "Age", "40-55");
			m.setValue(0.1, "Activite", "Précaire", "Age", "55 et plus");
			
			m.setValue(0.0, "Activite", "Employé", "Age", "0-5");
			m.setValue(0.0, "Activite", "Employé", "Age", "6-15");
			m.setValue(0.3, "Activite", "Employé", "Age", "16-25");
			m.setValue(0.7, "Activite", "Employé", "Age", "26-40");
			m.setValue(0.6, "Activite", "Employé", "Age", "40-55");
			m.setValue(0.5, "Activite", "Employé", "Age", "55 et plus");
						
			m.normalize();
			
			return m;
			
		} catch (GSIllegalRangedData e) {
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
			
			m.setValue(1.0, "Activite", "Sans emploi", "Age2", "moins de 15");
			m.setValue(0.3, "Activite", "Sans emploi", "Age2", "16-25");
			m.setValue(0.3, "Activite", "Sans emploi", "Age2", "26-55");
			m.setValue(0.4, "Activite", "Sans emploi", "Age2", "55 et plus");
			
			m.setValue(0.0, "Activite", "Précaire", "Age2", "moins de 15");
			m.setValue(0.3, "Activite", "Précaire", "Age2", "16-25");
			m.setValue(0.4, "Activite", "Précaire", "Age2", "26-55");
			m.setValue(0.1, "Activite", "Précaire", "Age2", "55 et plus");
			
			m.setValue(0.0, "Activite", "Employé", "Age2", "moins de 15");
			m.setValue(0.3, "Activite", "Employé", "Age2", "16-25");
			m.setValue(0.7, "Activite", "Employé", "Age2", "26-55");
			m.setValue(0.5, "Activite", "Employé", "Age2", "55 et plus");
						
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
			
			AFullNDimensionalMatrix<Double> mAgeGender = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attAge,attGenre);
			
			
			mAgeGender.setValue(0.15/3, "Genre", "Homme", "Age", "0-5");
			mAgeGender.setValue(0.20/3, "Genre", "Homme", "Age", "6-15");
			mAgeGender.setValue(0.30/3, "Genre", "Homme", "Age", "16-25");
			mAgeGender.setValue(0.20/3, "Genre", "Homme", "Age", "26-40");
			mAgeGender.setValue(0.10/3, "Genre", "Homme", "Age", "40-55");
			mAgeGender.setValue(0.05/3, "Genre", "Homme", "Age", "55 et plus");
			
			mAgeGender.setValue(0.10*2/3, "Genre", "Femme", "Age", "0-5");
			mAgeGender.setValue(0.20*2/3, "Genre", "Femme", "Age", "6-15");
			mAgeGender.setValue(0.30*2/3, "Genre", "Femme", "Age", "16-25");
			mAgeGender.setValue(0.25*2/3, "Genre", "Femme", "Age", "26-40");
			mAgeGender.setValue(0.10*2/3, "Genre", "Femme", "Age", "40-55");
			mAgeGender.setValue(0.05*2/3, "Genre", "Femme", "Age", "55 et plus");
			
			mAgeGender.normalize();
			
			// age2 X csp

			AFullNDimensionalMatrix<Double> mAge2CSP = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attAge2,attCSP);
			
			mAge2CSP.setValue(1.0, "Activite", "Sans emploi", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3, "Activite", "Sans emploi", "Age2", "16-25");
			mAge2CSP.setValue(0.3, "Activite", "Sans emploi", "Age2", "26-55");
			mAge2CSP.setValue(0.4, "Activite", "Sans emploi", "Age2", "55 et plus");
			
			mAge2CSP.setValue(0.0, "Activite", "Précaire", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3, "Activite", "Précaire", "Age2", "16-25");
			mAge2CSP.setValue(0.4, "Activite", "Précaire", "Age2", "26-55");
			mAge2CSP.setValue(0.1, "Activite", "Précaire", "Age2", "55 et plus");
			
			mAge2CSP.setValue(0.0, "Activite", "Employé", "Age2", "moins de 15");
			mAge2CSP.setValue(0.3, "Activite", "Employé", "Age2", "16-25");
			mAge2CSP.setValue(0.7, "Activite", "Employé", "Age2", "26-55");
			mAge2CSP.setValue(0.5, "Activite", "Employé", "Age2", "55 et plus");
						
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
		assertEquals("wrong storage", new Double(0.15/3), agePyramid.getVal("Genre", "Homme", "Age", "0-5").getValue());
		assertEquals("wrong storage", new Double(0.2*2/3), agePyramid.getVal("Genre", "Femme", "Age", "6-15").getValue());
		assertEquals("wrong storage", new Double(0.05/3), agePyramid.getVal("Genre", "Homme", "Age", "55 et plus").getValue());

	}
	
	@Test
	public void testGetValOneDimensionAgePyramid() {
		
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		
		assertEquals("wrong computation of the column", new Double(1.0), agePyramid.getVal().getValue());
		assertEquals("wrong computation of the column", new Double(1./3), agePyramid.getVal("Genre","Homme").getValue());
		assertEquals("wrong computation of the column", new Double(2./3), agePyramid.getVal("Genre","Femme").getValue());

		assertEquals("wrong computation of the column", new Double(0.15/3+0.10*2/3), agePyramid.getVal("Age","0-5").getValue());

	}
	
	@Test
	public void testGetValOneDimensionCSP() {
		
		AFullNDimensionalMatrix<Double> ageCSP = generateGlobalFrequencyAgeCSP();

		assertEquals("wrong computation of the total", new Double(1.0), ageCSP.getVal().getValue());

	}

	@Test
	public void testGetValSubSetAgePyramid() {
		
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		
		assertEquals("wrong computation of the probas", new Double(0.15/3+0.20/3), agePyramid.getVal("Genre","Homme","Age","0-5", "Age", "6-15").getValue());

		assertEquals("wrong computation of the probas", agePyramid.getVal("Genre","Homme","Genre","Femme","Age","0-5", "Age", "6-15").getValue(), agePyramid.getVal("Age","0-5", "Age", "6-15").getValue());

	}
	
	@Test
	public void testGetValSegmentedNoMappingAll() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		assertEquals("joined probability does not sum to the count of inner matrixes", seg.jointDistributionSet.size(), seg.getVal().getValue().doubleValue(), 0.01);
		
	}
	

	@Test
	public void testGetValSegmentedNoMappingJoined() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		
		assertEquals(
				"wrong joined distribution", 
				0.0048d, 
				seg.getVal("Age","26-40","Genre","Homme","Activite","Sans emploi").getValue().doubleValue(), 
				0.001
				);
		
	}


	@Test
	public void testGetValSegmentedWithMapping() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedWithMappingAgePyramidAndCSP();
		
		assertEquals(
				"wrong joined distribution", 
				0.0048d, 
				seg.getVal("Age2","26-55","Genre","Homme","Activite","Sans emploi").getValue().doubleValue(), 
				0.001
				);

		assertEquals("joined probability does not sum to 1", new Double(1.0), seg.getVal());
	}

}
