package core.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import core.configuration.dictionary.DemographicDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.attribute.demographic.MappedDemographicAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class GenstarJsonUtilTest {

	private IGenstarDictionary<DemographicAttribute<? extends IValue>> dd;

	private GenstarJsonUtil sju;
	private Path path;

	@SuppressWarnings("unchecked")
	@Before
	public void setupBefore() throws GSIllegalRangedData {
		
		path = Paths.get(System.getProperty("user.dir")
				+"src"+File.separator+"test"+File.separator+"java"+File.separator
				+"core"+File.separator+"configuration"+File.separator
				+File.separator+"GS_json_config_test.gns");

		dd = new DemographicDictionary<>();

		// BOOLEAN 
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Boolean attribute", GSEnumDataType.Boolean));
		
		// NOMINAL
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Nominal attribute", GSEnumDataType.Nominal, 
						List.of("Célibataire", "En couple")));
		
		// ORDERED
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Ordered attribute", GSEnumDataType.Order, 
						List.of("Brevet", "Bac", "Licence", "Master et plus")));
		
		// COOL FIBONACCI LIST
		List<String> fiboList = Stream.iterate(new int[] {0,1}, 
				pair -> pair[0] < 20, 
				pair -> new int[] {pair[1], pair[0] + pair[1]})
			.map(pair -> String.valueOf(pair[0])).collect(Collectors.toList());
		
		// INTEGER
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Int attribute", GSEnumDataType.Integer, fiboList));
		
		// CONTINUE
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Double attribute", GSEnumDataType.Continue, 
						List.of("0,1", "0.3", "0,5", "0.7", "0.9")));
		
		// RANGE
		DemographicAttribute<RangeValue> rangeAttribute = DemographicAttributeFactory.getFactory()
				.createRangeAttribute("Range attribute", 
						List.of("moins de 14", "15 à 24", "25 à 34", "35 à 54", "55 et plus"),
						0, 120);
		dd.addAttributes(rangeAttribute);

		// AGGREGATED
		MappedDemographicAttribute<RangeValue, RangeValue> rangeAggAttribute = 
				DemographicAttributeFactory.getFactory()
					.createRangeAggregatedAttribute("Range aggregated attribute", rangeAttribute, 
						Map.of(
								"moins de 24 ans", Set.of("moins de 14", "15 à 24"),
								"25 à 54", Set.of("25 à 34", "35 à 54"),
								"55 et plus", Set.of("55 et plus")
								));
		dd.addAttributes(rangeAggAttribute);
		
		// MAPPED
		MappedDemographicAttribute<? extends IValue, RangeValue> nominalToRangeAttribute = 
				DemographicAttributeFactory.getFactory()
					.createMappedAttribute("Mapped attribute", GSEnumDataType.Order, rangeAttribute, 
							List.of(Map.entry(List.of("Bébé", "Enfant"), Set.of("moins de 14")),
									Map.entry(List.of("Adolescent", "Jeune"), Set.of("15 à 24")),
									Map.entry(List.of("Adulte"), Set.of("25 à 34", "35 à 54")),
									Map.entry(List.of("Vieux"), Set.of("55 et plus")))
							.stream().collect(Collectors.toMap(
									entry -> entry.getKey(), 
									entry -> entry.getValue(), 
									(e1, e2) -> e1,
									LinkedHashMap::new))
						);
		dd.addAttributes(nominalToRangeAttribute);
		
		// RECORD
		MappedDemographicAttribute<IntegerValue, ? extends IValue> intToBoolAttribute =
				DemographicAttributeFactory.getFactory()
					.createIntegerRecordAttribute("Record attribute", dd.getAttribute("Boolean attribute"),
							Map.of("1", "true", "2", "false"));
		dd.addAttributes(intToBoolAttribute);

		sju = new GenstarJsonUtil();
	}

	@Test
	public void test() throws JsonGenerationException, JsonMappingException, IOException {
		for(DemographicAttribute<? extends IValue> att : dd.getAttributes()) {
			
			sju.marshalToGenstarJson(path, att);
			
			// DEBUG
			//System.out.println(sju.genstarJsonToString(att));
			
			@SuppressWarnings("unchecked")
			DemographicAttribute<? extends IValue> unmarshalAtt = sju.unmarshalFromGenstarJson(path, att.getClass());
			
			assertEquals(att.getValueSpace(), unmarshalAtt.getValueSpace());
		}
	}

	@Test
	public void testDemographicDictionary() throws JsonGenerationException, JsonMappingException, IOException {
			sju.marshalToGenstarJson(path, dd);
			
			// DEBUG
			System.out.println(sju.genstarJsonToString(dd));
			
			IGenstarDictionary<DemographicAttribute<? extends IValue>> dd2 = 
					sju.unmarshalFromGenstarJson(path, dd.getClass());
			
			for(DemographicAttribute<? extends IValue> att : dd.getAttributes()) {
				assertEquals(att, dd2.getAttribute(att.getAttributeName()));
			}
	}

}
