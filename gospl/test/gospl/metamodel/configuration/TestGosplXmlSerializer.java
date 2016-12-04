package gospl.metamodel.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarXmlSerializer;
import core.metamodel.pop.APopulationAttribute;

public class TestGosplXmlSerializer {

	File filetest = new File("testdata/rouen1/GSC_RouenIndividual.xml");
	
	@Test
	public void testReadRouenSerializedTest() {
		
		if (!filetest.isFile())
			throw new IllegalArgumentException("invalid test environment: the file "+filetest+" is not available for test");
		
		GenstarXmlSerializer xs  = null;
		try {
			xs = new GenstarXmlSerializer();
		} catch (FileNotFoundException e) {
			fail("unable to initialize serializer");
		}
		GenstarConfigurationFile cf = null;
		try {
			cf = xs.deserializeGSConfig(filetest);
		} catch (FileNotFoundException e) {
			fail("unable to read test file");
		}
		
		// do we have the right count of attributes ?
		assertEquals(
				"wrong count of attributes", 
				6, 
				cf.getAttributes().size()
				);
		
		// do we have all the expected attributes ?
		final Set<String> expectedAttributesNames = Collections.unmodifiableSet(new HashSet<String>(
															Arrays.asList("CSP", "Age_2", "Age_3", "Couple", "Sexe", "Age"))
															);
		Set<String> foundAttributesNames = new HashSet<>();
		for (APopulationAttribute a : cf.getAttributes()) {
			foundAttributesNames.add(a.getAttributeName());
		}
		assertEquals(
				"did not found the expected attributes", 
				expectedAttributesNames,
				foundAttributesNames
				);
		
		
	}

}
