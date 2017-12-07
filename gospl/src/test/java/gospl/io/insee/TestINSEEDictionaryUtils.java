package gospl.io.insee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.RangeSpace;

public class TestINSEEDictionaryUtils {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadFromURL1() {
		
		IGenstarDictionary<DemographicAttribute<? extends IValue>> dictionnary = ReadINSEEDictionaryUtils
				.readFromWebsite("https://www.insee.fr/fr/statistiques/2863616?sommaire=2867837#dictionnaire");
		
		assertEquals("wrong count of attributes", 5, dictionnary.size());
		
		System.out.println(dictionnary);

		
		Map<String,DemographicAttribute<? extends IValue>> name2att = dictionnary.getAttributes()
							.stream()
							.collect(Collectors.toMap(a->a.getAttributeName(), a->a));
		
		assertNotNull("expecting variable AGE4", name2att.get("AGE4"));
		assertNotNull("variable AGE4 should contain 00", name2att.get("AGE4").getValueSpace().addValue("00"));
		assertTrue("AGE4 should be range", name2att.get("AGE4").getValueSpace() instanceof RangeSpace);
		
	} 

	@Test
	public void testLoadFromURL2() {
		
		IGenstarDictionary<DemographicAttribute<? extends IValue>> dictionnary = 
				ReadINSEEDictionaryUtils.readFromWebsite("https://www.insee.fr/fr/statistiques/2863607?sommaire=2867825#dictionnaire");
		
		assertEquals("wrong count of attributes", 10, dictionnary.size());
		
		System.out.println(dictionnary);

		
		Map<String,DemographicAttribute<? extends IValue>> name2att = dictionnary.getAttributes()
				.stream()
				.collect(Collectors.toMap(a->a.getAttributeName(), a->a));
		
		assertNotNull("expecting variable AGEMEN7", name2att.get("AGEMEN7"));
		assertNotNull("variable AGEMEN7 should contain 00", name2att.get("AGEMEN7").getValueSpace().addValue("00"));
		assertTrue("AGEMEN7 should be range", name2att.get("AGEMEN7").getValueSpace() instanceof RangeSpace);
		
	}
	
	@Test
	public void testLoadFromDictionaryMODFile() {
		
		IGenstarDictionary<DemographicAttribute<? extends IValue>> dictionnary = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/MOD_INDREG_2014.txt");
		
		assertEquals("wrong count of attributes", 97, dictionnary.size());
		
		System.out.println(dictionnary);

		Map<String,DemographicAttribute<? extends IValue>> name2att = dictionnary.getAttributes()
					.stream()
					.collect(Collectors.toMap(a->a.getAttributeName(), a->a));
		
		assertNotNull("expecting variable TYPMD", name2att.get("TYPMD"));
		assertEquals("TYPMD should contain 35 modalities", 35, name2att.get("TYPMD").getValueSpace().getValues().size());

		assertNotNull("expecting variable UR", name2att.get("UR"));
		assertEquals("UR should contain 2 modalities", 2, name2att.get("UR").getValueSpace().getValues().size());

		System.out.println(name2att.get("TYPMD").getValueSpace());
		
	}
	
}
