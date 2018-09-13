package gospl.io.ipums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandomUtils;

public class ReadIPUMSDictionaryUtilsTest {

	private File sample;
	private File dictionary;
	private Path resourceFolder = FileSystems.getDefault().getPath("src", "test", "resources", "ipums_data");
	
	private IGenstarDictionary<Attribute<? extends IValue>> dd;
	
	private static final Set<String> ATTRIBUTE_NAMES = new HashSet<>(
			Arrays.asList("AGE", "SEX", "MARST", "MARSTD", "EDUCVN", "EMPSTAT", "EMPSTATD"));
	
	@Before
	public void setUp() throws Exception {
		sample = resourceFolder.resolve("ipumsi_00002.csv").toFile();
		dictionary = resourceFolder.resolve("ipumsi_00002.rtf").toFile();
		
		GenstarConfigurationFile gcf = new GenstarConfigurationFile();
		gcf.setSurveyWrappers(Arrays.asList(new GSSurveyWrapper(sample.toPath(), GSSurveyType.Sample, ';', 1, 1)));
		
	}
	
	@After
	public void tearDown() throws Exception {
		GenstarJsonUtil gsu = new GenstarJsonUtil();
		gsu.marshalToGenstarJson(resourceFolder.resolve("ipums_dictionary.gns"), dd, false);
	}
	
	@Test
	public void testFiles() {
		assertNotNull(sample.exists() && dictionary.exists());
	}
	
	@Test
	public void testReadFile() {
		ReadIPUMSDictionaryUtils ipumsReader = new ReadIPUMSDictionaryUtils();
		try {
			dd = ipumsReader.readDictionaryFromRTF(dictionary);
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> an = ipumsReader.getAttributeNames();
		assertEquals(an.keySet(), ATTRIBUTE_NAMES);
		System.out.println(dd.getAttributes().stream().map(a -> a.getAttributeName()).collect(Collectors.joining("; ")));
		assertNotNull(dd.getAttribute(an.get(GenstarRandomUtils.oneOf(ATTRIBUTE_NAMES))));
	}

}
