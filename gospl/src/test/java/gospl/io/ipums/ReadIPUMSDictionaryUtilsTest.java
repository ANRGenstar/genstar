package gospl.io.ipums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.GenstarConfigurationFile;
import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandomUtils;
import gospl.distribution.GosplInputDataManager;
import gospl.io.exception.InvalidSurveyFormatException;

public class ReadIPUMSDictionaryUtilsTest {

	private File sample;
	private File dictionary;
	private Path resourceFolder = FileSystems.getDefault().getPath("src", "test", "resources", "ipums_data");
	
	private IGenstarDictionary<Attribute<? extends IValue>> dd;
	private GenstarConfigurationFile gcf;
	private GosplInputDataManager gidm;
	
	private static final Set<String> ATTRIBUTE_NAMES = new HashSet<>(
			Arrays.asList("AGE", "SEX", "MARST", "MARSTD", "EDUCVN", "EMPSTAT", "EMPSTATD"));
	
	private static final Map<String, String> firstEntity = 
			Stream.of(new SimpleEntry<>("AGE", "41"),
					new SimpleEntry<>("SEX", "2"),
					new SimpleEntry<>("MARST", "2"),
					new SimpleEntry<>("MARSTD", "200"),
					new SimpleEntry<>("EDUCVN", "231"),
					new SimpleEntry<>("EMPSTAT", "1"),
					new SimpleEntry<>("EMPSTATD", "110"))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())); 
	
	@Before
	public void setUp() throws Exception {
		sample = resourceFolder.resolve("ipumsi_00002.csv").toFile();
		dictionary = resourceFolder.resolve("ipumsi_00002.rtf").toFile();
		
		gcf = new GenstarConfigurationFile();
		gcf.addSurveyWrapper(new GSSurveyWrapper(sample.toPath(), GSSurveyType.Sample, ',', 1, 1));
		
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
		
		assertEquals(ipumsReader.getAttributeNames().keySet(), ATTRIBUTE_NAMES);
		
		gcf.setDictionary(dd);
		
		gidm = new GosplInputDataManager(gcf);
		try {
			gidm.buildSamples();
		} catch (InvalidFormatException | IOException | InvalidSurveyFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> pop = gidm.getRawSamples().iterator().next();
		
		assertEquals(pop.getPopulationAttributes(),dd.getAttributes());
		
		ADemoEntity firstPeople = pop.stream().filter(e -> e.getEntityId().endsWith("_1")).findFirst().get();
		Attribute<? extends IValue> targetAtt = GenstarRandomUtils.oneOf(pop.getPopulationAttributes());
		
		assertEquals(firstPeople.getValueForAttribute(targetAtt).getStringValue(), firstEntity.get(targetAtt.getAttributeName()));
		
	}
	

}
