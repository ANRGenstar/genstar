package core.configuration.jackson;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.tools.javac.util.List;

import core.configuration.jackson.utilclass.dummy.Cage;
import core.configuration.jackson.utilclass.dummy.IAnimal;
import core.configuration.jackson.utilclass.dummy.Lion;

public class DummyZooTest {
	static ObjectMapper om;

	static File theFile;
	static Cage<IAnimal> cage;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cage = new Cage<>();
		om = new ObjectMapper();
		
		Lion lion = new Lion("michel");
		Collection<IAnimal> animals = List.of(lion, lion);
		cage.setAnimals(animals);
		
		theFile = Files.createTempFile(Paths.get(System.getProperty("user.dir")), "_", ".tmp").toFile();
		
		om.writerFor(Cage.class).writeValue(theFile, cage);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		theFile.delete();
	}

	@Test
	public void test() {
		Cage<IAnimal> unmarshalCage = null;
		try {
			System.out.println(om.writeValueAsString(cage));
			unmarshalCage = om.readerFor(Cage.class).readValue(theFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(cage, Matchers.is(unmarshalCage));
	}

}
