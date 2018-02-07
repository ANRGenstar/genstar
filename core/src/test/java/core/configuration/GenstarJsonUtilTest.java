package core.configuration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import core.configuration.dictionary.DemographicDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.attribute.demographic.MappedDemographicAttribute;
import core.metamodel.attribute.emergent.EmergentAttributeFactory;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.GSUtilAttribute;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class GenstarJsonUtilTest {

	private IGenstarDictionary<DemographicAttribute<? extends IValue>> dd;

	private GenstarJsonUtil sju;
	private Path path;

	@SuppressWarnings("unchecked")
	@Before
	public void setupBefore() throws GSIllegalRangedData {
		
		path = FileSystems.getDefault().getPath("src","test","resources","json_test.gns");

		dd = new DemographicDictionary<>();

		// BOOLEAN 
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Boolean attribute", GSEnumDataType.Boolean));
		
		// NOMINAL
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Nominal attribute", GSEnumDataType.Nominal, 
						Arrays.asList("Célibataire", "En couple")));
		
		// ORDERED
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Ordered attribute", GSEnumDataType.Order, 
						Arrays.asList("Brevet", "Bac", "Licence", "Master et plus")));
		
		// COOL FIBONACCI LIST
		List<String> fiboList = new LinkedList<>();
		int a = 1;
		int b = 0;
		int n = 1;
		while (n++ <= 20) {  // input being from the user
		    a += b;
		    b = a - b;
		    char[] fib = Integer.toString(b).toCharArray();
		    for (int i = 0; i < fib.length; i++) {
		    	fiboList.add(Integer.toString(fib[i] - '0'));
		    }
		}

		// INTEGER
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Int attribute", GSEnumDataType.Integer, fiboList));
		
		// CONTINUE
		dd.addAttributes(DemographicAttributeFactory.getFactory()
				.createAttribute("Double attribute", GSEnumDataType.Continue, 
						Arrays.asList("0,1", "0.3", "0,5", "0.7", "0.9")));
		
		// RANGE
		DemographicAttribute<RangeValue> rangeAttribute = DemographicAttributeFactory.getFactory()
				.createRangeAttribute("Range attribute", 
						Arrays.asList("moins de 14", "15 à 24", "25 à 34", "35 à 54", "55 et plus"),
						0, 120);
		dd.addAttributes(rangeAttribute);

		// AGGREGATED
		Map<String,Collection<String>> value2values = new HashMap<>();
		value2values.put("moins de 24 ans", Arrays.asList("moins de 14", "15 à 24"));
		value2values.put("25 à 54", Arrays.asList("25 à 34", "35 à 54"));
		value2values.put("55 et plus", Arrays.asList("55 et plus"));
		
		MappedDemographicAttribute<RangeValue, RangeValue> rangeAggAttribute = 
				DemographicAttributeFactory.getFactory()
					.createRangeAggregatedAttribute("Range aggregated attribute", rangeAttribute, 
						value2values);
		dd.addAttributes(rangeAggAttribute);
		
		// MAPPED
		Map<Collection<String>,Collection<String>> value2value = new LinkedHashMap<>();
		value2value.put(Arrays.asList("Bébé", "Enfant"), Arrays.asList("moins de 14"));
		value2value.put(Arrays.asList("Adolescent", "Jeune"), Arrays.asList("15 à 24"));
		value2value.put(Arrays.asList("Adulte"), Arrays.asList("25 à 34", "35 à 54"));
		value2value.put(Arrays.asList("Vieux"), Arrays.asList("55 et plus"));
		
		MappedDemographicAttribute<? extends IValue, RangeValue> nominalToRangeAttribute = 
				DemographicAttributeFactory.getFactory()
					.createMappedAttribute("Mapped attribute", GSEnumDataType.Order, 
							rangeAttribute, 
							value2value
						);
		dd.addAttributes(nominalToRangeAttribute);
		
		// RECORD (CODE)
		Map<String,String> value2value2 = new HashMap<>();
		value2value2.put("1", "true");
		value2value2.put("2", "false");
		MappedDemographicAttribute<IntegerValue, ? extends IValue> intToBoolAttribute =
				DemographicAttributeFactory.getFactory()
					.createIntegerRecordAttribute("Record attribute", dd.getAttribute("Boolean attribute"),
							value2value2);
		dd.addAttributes(intToBoolAttribute);
		
		// RECORD
		dd.addRecords(DemographicAttributeFactory.getFactory()
				.createRecordAttribute("Population", GSEnumDataType.Integer, 
						dd.getAttribute("Range aggregated attribute")));
		
		// EMERGENT
		IValue[] matches = GSUtilAttribute.getIValues(rangeAttribute, "25 à 34", "35 à 54", "55 et plus").toArray(new IValue[3]);
		dd.addAttributes(EmergentAttributeFactory.getFactory()
				.getValueOfAttribute("Age du référent du ménage", rangeAttribute, EChildFilter.OneOf.getFilter(), 
						matches));
		
		IValue[] matchesTwo = GSUtilAttribute.getIValues(rangeAggAttribute, "moins de 24 ans").toArray(new IValue[1]);
		dd.addAttributes(EmergentAttributeFactory.getFactory()
				.getAggregatedRangeAttribute("Age cumulé des enfants", rangeAggAttribute, 
						EChildFilter.OneOf.getFilter(), matchesTwo));

		sju = new GenstarJsonUtil();
		
	}

	@Test
	@Ignore
	public void test() throws JsonGenerationException, JsonMappingException, IOException {
		for(DemographicAttribute<? extends IValue> att : dd.getAttributes()) {
			
			sju.marshalToGenstarJson(path, att, false);
			
			@SuppressWarnings("unchecked")
			DemographicAttribute<? extends IValue> unmarshalAtt = sju.unmarshalFromGenstarJson(path, att.getClass());
			
			assertEquals(att.getValueSpace(), unmarshalAtt.getValueSpace());
		}
	}

	@Test
	public void testDemographicDictionary() throws JsonGenerationException, JsonMappingException, IOException {
			sju.marshalToGenstarJson(path, dd, false);
			
			// DEBUG
			System.out.println(sju.genstarJsonToString(dd));
			
			@SuppressWarnings("unchecked")
			IGenstarDictionary<DemographicAttribute<? extends IValue>> dd2 = 
					sju.unmarshalFromGenstarJson(path, dd.getClass());
			
			for(DemographicAttribute<? extends IValue> att : dd.getAttributes())
				assertEquals(att, dd2.getAttribute(att.getAttributeName()));
	}

}
