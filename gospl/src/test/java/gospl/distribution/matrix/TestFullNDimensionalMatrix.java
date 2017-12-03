package gospl.distribution.matrix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.control.AControl;

public class TestFullNDimensionalMatrix {

	private static double probaHomme = 0.47;
	private static double probaFemme = 0.53;
	private static double delta = 0.001;

	@Before
	public void setUp() throws Exception {
		
	}	

	@After
	public void tearDown() throws Exception {
	}


	/**
	 * generates a global frequency table 
	 * @return
	 */
	public AFullNDimensionalMatrix<Double> generateGlobalFrequencyAgeGender() {

		try {

			Set<DemographicAttribute<? extends IValue>> attributes = new HashSet<>();
			attributes.add(DemographicAttributeFactory.getFactory().createAttribute(
					"Genre", GSEnumDataType.Nominal, Arrays.asList("Homme", "Femme")));
			attributes.add(DemographicAttributeFactory.getFactory().createAttribute("Age", GSEnumDataType.Range, 
					Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus")));


			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);

			m.setValue(0.05*probaHomme, "Genre", "Homme", "Age", "0-5");
			m.setValue(0.20*probaHomme, "Genre", "Homme", "Age", "6-15");
			m.setValue(0.25*probaHomme, "Genre", "Homme", "Age", "16-25");
			m.setValue(0.30*probaHomme, "Genre", "Homme", "Age", "26-40");
			m.setValue(0.15*probaHomme, "Genre", "Homme", "Age", "40-55");
			m.setValue(0.05*probaHomme, "Genre", "Homme", "Age", "55 et plus");

			m.setValue(0.05*probaFemme, "Genre", "Femme", "Age", "0-5");
			m.setValue(0.20*probaFemme, "Genre", "Femme", "Age", "6-15");
			m.setValue(0.30*probaFemme, "Genre", "Femme", "Age", "16-25");
			m.setValue(0.25*probaFemme, "Genre", "Femme", "Age", "26-40");
			m.setValue(0.15*probaFemme, "Genre", "Femme", "Age", "40-55");
			m.setValue(0.05*probaFemme, "Genre", "Femme", "Age", "55 et plus");

			return m;
		} catch (GSIllegalRangedData e) {
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}


	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAgeCSP() {

		try {

			Set<DemographicAttribute<? extends IValue>> attributes = new HashSet<>();
			attributes.add(DemographicAttributeFactory.getFactory().createAttribute("Age", GSEnumDataType.Range, 
					Arrays.asList("0-5", "6-15", "16-25", "26-40", "40-55", "55 et plus")));
			attributes.add(DemographicAttributeFactory.getFactory().createAttribute(
					"Activite", GSEnumDataType.Nominal, Arrays.asList("Sans emploi", "Précaire", "Employé")));

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
			m.setValue(1/3d*age1625, "Activite", "Sans emploi", "Age", "16-25");
			m.setValue(0.2*age2640, "Activite", "Sans emploi", "Age", "26-40");
			m.setValue(0.2*age4055, "Activite", "Sans emploi", "Age", "40-55");
			m.setValue(0.3*age55plus, "Activite", "Sans emploi", "Age", "55 et plus");

			m.setValue(0.0, "Activite", "Précaire", "Age", "0-5");
			m.setValue(0.0, "Activite", "Précaire", "Age", "6-15");
			m.setValue(1/3d*age1625, "Activite", "Précaire", "Age", "16-25");
			m.setValue(0.3*age2640, "Activite", "Précaire", "Age", "26-40");
			m.setValue(0.2*age4055, "Activite", "Précaire", "Age", "40-55");
			m.setValue(0.2*age55plus, "Activite", "Précaire", "Age", "55 et plus");

			m.setValue(0.0, "Activite", "Employé", "Age", "0-5");
			m.setValue(0.0, "Activite", "Employé", "Age", "6-15");
			m.setValue(1/3d*age1625, "Activite", "Employé", "Age", "16-25");
			m.setValue(0.5*age2640, "Activite", "Employé", "Age", "26-40");
			m.setValue(0.6*age4055, "Activite", "Employé", "Age", "40-55");
			m.setValue(0.5*age55plus, "Activite", "Employé", "Age", "55 et plus");
			
			if(Math.abs(Arrays.stream(new double[]{m.getVal("Activite", "Sans emploi").getValue(), 
					m.getVal("Activite", "Précaire").getValue(), 
					m.getVal("Activite", "Employé").getValue()}).sum() - 1) > delta){
				throw new RuntimeException("ERROR INIT: sans emploi = "+m.getVal("Activite", "Sans emploi")+
						"; précaire = "+m.getVal("Activite", "Précaire")+
						"; employé = "+m.getVal("Activite", "Employé"));
				
			}
			
			return m;

		} catch (GSIllegalRangedData e) {
			// In any case, do not kill the dev. 
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}


	@SuppressWarnings("unchecked")
	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAge2CSP(boolean mapped) {

		try {

			AFullNDimensionalMatrix<Double> ageCSP = this.generateGlobalFrequencyAgeCSP();
			Set<DemographicAttribute<? extends IValue>> attributes = new HashSet<>();
			if(mapped){
				Map<String, Collection<String>> mapperAge2toAge = new HashMap<>();
				mapperAge2toAge.put("moins de 15",  Set.of("0-5", "6-15"));
				mapperAge2toAge.put("16-25", Set.of("16-25"));
				mapperAge2toAge.put("26-55", Set.of("26-40","40-55"));
				mapperAge2toAge.put("55 et plus", Set.of("55 et plus"));
				attributes.add(DemographicAttributeFactory.getFactory().createRangeAggregatedAttribute("Age2", 
						new GSDataParser().getRangeTemplate(mapperAge2toAge.keySet().stream().collect(Collectors.toList())), 
						(DemographicAttribute<RangeValue>) ageCSP.getDimension("Age"), mapperAge2toAge));
			} else {
				attributes.add(DemographicAttributeFactory.getFactory().createAttribute("Age2", GSEnumDataType.Range, 
						Arrays.asList("moins de 15", "16-25", "26-55", "55 et plus")));
			}
			attributes.add(ageCSP.getDimension("Activite"));

			AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);

			for(IValue value : ageCSP.getDimension("Activite").getValueSpace().getValues()){
				m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "0-5").getValue() + 
						ageCSP.getVal("Activite", value.getStringValue(), "Age", "6-15").getValue(), 
						"Activite", value.getStringValue(), "Age2", "moins de 15");
				m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "16-25").getValue(), 
						"Activite", value.getStringValue(), "Age2", "16-25");
				m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "26-40").getValue() + 
						ageCSP.getVal("Activite", value.getStringValue(), "Age", "40-55").getValue(), 
						"Activite", value.getStringValue(), "Age2", "26-55");
				m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "55 et plus").getValue(), 
						"Activite", value.getStringValue(), "Age2", "55 et plus");
			}

			return m;

		} catch (GSIllegalRangedData e) {
			throw new RuntimeException("the developer screwed up when writing unit tests. Just kill him, get rid of the body, and hire someone better.", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected AFullNDimensionalMatrix<Double> generateGlobalFrequencyAge3CSP() throws IllegalArgumentException, GSIllegalRangedData {

		AFullNDimensionalMatrix<Double> ageCSP = this.generateGlobalFrequencyAgeCSP();
		Set<DemographicAttribute<? extends IValue>> attributes = new HashSet<>();
			Map<String, Collection<String>> mapperAge2toAge = new HashMap<>();
			mapperAge2toAge.put("16-25", Set.of("16-25"));
			mapperAge2toAge.put("26-55", Set.of("26-40","40-55"));
			mapperAge2toAge.put("55 et plus", Set.of("55 et plus"));
			attributes.add(DemographicAttributeFactory.getFactory().createRangeAggregatedAttribute("Age3", 
					new GSDataParser().getRangeTemplate(mapperAge2toAge.keySet().stream().collect(Collectors.toList())), 
					(DemographicAttribute<RangeValue>) ageCSP.getDimension("Age"), mapperAge2toAge));
		attributes.add(ageCSP.getDimension("Activite"));

		AFullNDimensionalMatrix<Double> m = GosplNDimensionalMatrixFactory.getFactory().createEmptyDistribution(attributes);

		for(IValue value : ageCSP.getDimension("Activite").getValueSpace().getValues()){
			m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "16-25").getValue(), 
					"Activite", value.getStringValue(), "Age3", "16-25");
			m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "26-40").getValue() + 
					ageCSP.getVal("Activite", value.getStringValue(), "Age", "40-55").getValue(), 
					"Activite", value.getStringValue(), "Age3", "26-55");
			m.setValue(ageCSP.getVal("Activite", value.getStringValue(), "Age", "55 et plus").getValue(), 
					"Activite", value.getStringValue(), "Age3", "55 et plus");
		}

		m.normalize();
		
		return m;
	}

	/**
	 * generates a segmented matrix with no mapping, based on age X gender and age X csp
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

			return GosplNDimensionalMatrixFactory.getFactory().createDistributionFromDistributions(
					generateGlobalFrequencyAgeGender(), 
					generateGlobalFrequencyAge2CSP(true)
					);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("error in the construction of the test case", e);
		}

	}
	
	@SuppressWarnings("unchecked")
	private ASegmentedNDimensionalMatrix<Double> generateSegmentedWithPartialMappingAgePyramideAndCSP() 
			throws IllegalArgumentException, GSIllegalRangedData {
		try {

			return GosplNDimensionalMatrixFactory.getFactory().createDistributionFromDistributions(
					generateGlobalFrequencyAgeGender(), 
					generateGlobalFrequencyAge3CSP()
					);
		} catch (IllegalDistributionCreation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("error in the construction of the test case", e);
		}
	}



	@Test
	public void testGetValOneAtoms() {
		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();
		assertEquals("wrong storage", (0.05*probaHomme), agePyramid.getVal("Genre", "Homme", "Age", "0-5").getValue().doubleValue(), 0);
		assertEquals("wrong storage", (0.2*probaFemme), agePyramid.getVal("Genre", "Femme", "Age", "6-15").getValue().doubleValue(), 0);
		assertEquals("wrong storage", (0.05*probaHomme), agePyramid.getVal("Genre", "Homme", "Age", "55 et plus").getValue().doubleValue(), 0);

	}

	@Test
	public void testGetValOneDimensionAgePyramid() {

		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();

		assertEquals("wrong computation of the column", 1.0, 
				agePyramid.getVal().getValue(), delta);
		assertEquals("wrong computation of the column", probaHomme, 
				agePyramid.getVal("Genre","Homme").getValue(), delta);
		assertEquals("wrong computation of the column", probaFemme, 
				agePyramid.getVal("Genre","Femme").getValue(), delta);

		assertEquals("wrong computation of the column", (0.05*probaHomme+0.05*probaFemme), 
				agePyramid.getVal("Age","0-5").getValue(), delta);

	}

	@Test
	public void testGetValOneDimensionCSP() {

		AFullNDimensionalMatrix<Double> ageCSP = generateGlobalFrequencyAgeCSP();

		assertEquals("wrong computation of the total", 1.0, ageCSP.getVal().getValue(), delta);

	}

	@Test
	public void testGetValSubSetAgePyramid() {

		AFullNDimensionalMatrix<Double> agePyramid = generateGlobalFrequencyAgeGender();

		assertEquals("wrong computation of the probas", (0.05*probaHomme+0.20*probaHomme), 
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

		List<AControl<Double>> ageList = seg.getMatrices().stream()
				.map(mat -> mat.getVal("Age", "26-40")).collect(Collectors.toList());
		
		assertEquals("Segmented inner matrices do not fit", ageList.get(0).getValue(), ageList.get(1).getValue(), delta);
		
		AControl<Double> cAgeGender = seg.getVal("Age", "26-40", "Genre","Homme");
		AControl<Double> cAge = seg.getVal("Age", "26-40");
		AControl<Double> cAgeCSP = seg.getVal("Age", "26-40", "Activite","Sans emploi");

		AControl<Double> calculatedProba = cAgeGender.multiply(cAgeCSP.multiply(1 / cAge.getValue()));
		AControl<Double> proposedProba = seg.getVal("Age","26-40","Genre","Homme","Activite","Sans emploi");

		assertEquals("wrong joined distribution", calculatedProba.getValue(), proposedProba.getValue(), delta);

	}


	@Test
	public void testGetValSegmentedWithMapping() {
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		ASegmentedNDimensionalMatrix<Double> seg2 = generateSegmentedWithMappingAgePyramidAndCSP();

		AControl<Double> cAge = seg.getVal("Age", "26-40", "Age", "40-55");
		AControl<Double> cAge2 = seg2.getVal("Age2","26-55");

		assertEquals("Matrix have not been correctly init.", cAge.getValue(), 
				cAge2.getValue(), delta);

		AControl<Double> seg2Value = seg2.getVal("Age2","26-55","Genre","Homme","Activite","Sans emploi");
		AControl<Double> calculatedProba = seg.getVal("Age", "26-40", "Age", "40-55", "Genre","Homme")
				.multiply(seg.getVal("Age", "26-40", "Age", "40-55", "Activite","Sans emploi")
						.multiply(1 / cAge.getValue())); 

		assertEquals("wrong joined distribution", calculatedProba.getValue(), 
				seg2Value.getValue(), delta);

		assertEquals("joined probability does not sum to 1", seg.getMatrices().size(), seg2.getVal().getValue(), delta);
	}
	
	@Test
	public void testGetValSegementedWithPartialMapping() throws IllegalArgumentException, GSIllegalRangedData{
		ASegmentedNDimensionalMatrix<Double> seg = generateSegmentedNoMappingAgePyramidAndCSP();
		ASegmentedNDimensionalMatrix<Double> segPartial = generateSegmentedWithPartialMappingAgePyramideAndCSP();
		
		// Is segmented matrix composed of sum-to-one matrices ?
		assertEquals("joined probability does not sum to 1", 
				seg.getMatrices().size(), segPartial.getVal().getValue(), delta);
		
		// Test if no relationship between age under 15 and occupation have correctly been estimated by getVal()
		assertEquals("Partially mapped segmented matrix fail to identify empty coordinate",  segPartial.getNulVal().getValue(),
				segPartial.getVal("Age","0-5", "Age", "6-15", "Activite", "Précaire").getValue());
		
		AControl<Double> cAge = seg.getVal("Age", "26-40", "Age", "40-55");
		AControl<Double> cAge2 = segPartial.getVal("Age3","26-55");
		
		assertEquals("Matrix have not been correctly init.", cAge.getValue(), 
				cAge2.getValue(), delta);
		
		AControl<Double> segPartialValue = segPartial.getVal("Age3","26-55","Genre","Homme","Activite","Sans emploi");
		AControl<Double> givenProba = seg.getVal("Age", "26-40", "Age", "40-55", "Genre","Homme", "Activite","Sans emploi");
		AControl<Double> calculatedProba = seg.getVal("Age", "26-40", "Age", "40-55", "Genre","Homme")
				.multiply(seg.getVal("Age", "26-40", "Age", "40-55", "Activite","Sans emploi")
						.multiply(1 / cAge.getValue())); 

		assertEquals("Computed value is not equal to mapped-diverging-based-matrix computation", calculatedProba.getValue(), 
				segPartialValue.getValue(), delta);
		assertEquals("Given value is not equal to mapped-diverging-based-matrix computation", 
				givenProba.getValue(), segPartialValue.getValue(), delta);
		
	}
	
}
