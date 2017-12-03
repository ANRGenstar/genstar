package core.configuration.jackson;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import core.configuration.jackson.utilclass.Dico;
import core.configuration.jackson.utilclass.IAtt;
import core.configuration.jackson.utilclass.IVal;
import core.configuration.jackson.utilclass.RangeAtt;
import core.util.random.GenstarRandom;

public class SimpleAttTest {

	static ObjectMapper om;

	static File theFile;
	static IAtt<? extends IVal> ra;
	static List<String> ranges;
	
	static File collectionFile;
	static Dico<IAtt<? extends IVal>> dico;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		theFile = Files.createTempFile(Paths.get(System.getProperty("user.dir")), "", ".gns").toFile();
		collectionFile = Files.createTempFile(Paths.get(System.getProperty("user.dir")), "", ".gns").toFile();
		om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		
		ranges = List.of("0-14", "15-24", "25-39", "40-64", "64-120");
		
		ra = new RangeAtt("-");
		ranges.forEach(range -> ra.addValue(range));
		
		om.writeValue(theFile, ra);
		
		dico = new Dico<>();
		IntStream.range(0, 3).forEach(i -> dico.add(ra));
		
		om.writeValue(collectionFile, dico);
	}
	
	@AfterClass
	public static void setUpAfterClass() {
		theFile.delete();
		collectionFile.delete();
	}

	@Test
	public void testJsonMarshal() {
		String jsonOutput = null;
		
		try {
			jsonOutput = om.writeValueAsString(ra);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(jsonOutput, Matchers.containsString(ranges.get(
				GenstarRandom.getInstance().nextInt(ranges.size()))));
		
		System.out.println(jsonOutput);
	}
	
	@Test
	public void testJsonUnmarshal() {
		RangeAtt unRa = null;
		
		try {
			unRa = om.readerFor(RangeAtt.class).readValue(theFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(unRa, Matchers.is(ra));
	}
	
	@Test
	public void testJsonUnmarshalCollection() {
		Dico<IAtt<? extends IVal>> unmarshDico = null;
		
		try {
			System.out.println(om.writeValueAsString(dico));
			unmarshDico = om.readerFor(Dico.class).readValue(collectionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(dico.getTheCollection(), Matchers.contains(unmarshDico.getTheCollection()));
	}

}
