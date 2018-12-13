package gospl.io.ipums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
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
import gospl.distribution.GosplInputMultiLayerDataManager;
import gospl.io.exception.InvalidSurveyFormatException;

public class ReadIPUMSMultiLevelDictionaryUtilsTest {

	private File sample;
	private File dictionary;
	private Path resourceFolder = FileSystems.getDefault().getPath("src", "test", "resources", "ipums_data");
	
	private Set<IGenstarDictionary<Attribute<? extends IValue>>> dds;
	
	
	private GenstarConfigurationFile gcf;
	private GosplInputMultiLayerDataManager gimldm;
	
	private static final Set<String> HH_ATTRIBUTES = new HashSet<>(
			Arrays.asList("HHTYPE", "NMOTHERS", "NFATHERS"));
	
	private static final Set<String> INDIV_ATTRIBUTE = new HashSet<>(
			Arrays.asList("RELATE", "RELATED", "AGE", "SEX", "MARST", "MARSTD"));
	
	private static final Map<String, String> firstHouseholdEntity =
			Stream.of(new SimpleEntry<>("HHTYPE", "3"),
					new SimpleEntry<>("NMOTHERS", "1"),
					new SimpleEntry<>("NFATHER", "1"))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	
	private static final Map<String, String> firstIndivEntity = 
			Stream.of(new SimpleEntry<>("RELATE", "1"),
					new SimpleEntry<>("RELATED", "1000"),
					new SimpleEntry<>("AGE", "41"),
					new SimpleEntry<>("SEX", "2"),
					new SimpleEntry<>("MARST", "2"),
					new SimpleEntry<>("MARSTD", "200"))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	
	@Before
	public void setUp() throws Exception {
		sample = resourceFolder.resolve("ipumsi_multi_test.csv").toFile();
		dictionary = resourceFolder.resolve("ipumsi_multi_test.txt").toFile();
		
		gcf = new GenstarConfigurationFile();
		gcf.addSurveyWrapper(new GSSurveyWrapper(sample.toPath(), GSSurveyType.Sample, ',', 1, 1));
	}

	@After
	public void tearDown() throws Exception {
		GenstarJsonUtil gsu = new GenstarJsonUtil();
		gsu.marshalToGenstarJson(resourceFolder.resolve("ipums_multi_dictionary.gns"), dds, false);
	}

	@Test
	public void test() {
		assertNotNull(sample.exists() && dictionary.exists());
	}
	
	@Test
	public void testIPUMSReader() throws InvalidFormatException, IOException, InvalidSurveyFormatException {
		ReadIPUMSDictionaryUtils ipumsReader = new ReadIPUMSDictionaryUtils();
		try {
			dds = ipumsReader.readDictionariesFromIPUMSDescription(dictionary);
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(2, dds.size());
		
		assertEquals(ipumsReader.getAttributeNames().keySet(), 
				Stream.of(HH_ATTRIBUTES.stream(), INDIV_ATTRIBUTE.stream())
					.collect(Collectors.toSet()));
	
		gimldm = new GosplInputMultiLayerDataManager(gcf);
		
		gimldm.buildSamples();
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> indivPopulation = gimldm.getRawSample(0);
		
		ADemoEntity firstPeople = indivPopulation.stream().filter(e -> e.getEntityId().endsWith("_1")).findFirst().get();
		Attribute<? extends IValue> targetAtt = GenstarRandomUtils.oneOf(indivPopulation.getPopulationAttributes());
		
		assertEquals(firstPeople.getValueForAttribute(targetAtt).getStringValue(), firstIndivEntity.get(targetAtt.getAttributeName()));
		

		IPopulation<ADemoEntity, Attribute<? extends IValue>> hhPopulation = gimldm.getRawSample(1);
		
		ADemoEntity firstHousehold = hhPopulation.stream().filter(e -> e.getEntityId().endsWith("_1")).findFirst().get();
		Attribute<? extends IValue> hhAtt = GenstarRandomUtils.oneOf(hhPopulation.getPopulationAttributes());
		
		assertEquals(firstHousehold.getValueForAttribute(hhAtt).getStringValue(), firstHouseholdEntity.get(hhAtt.getAttributeName()));
		
	}

}
