package gospl.io.insee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.IGSSurvey;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.distribution.GosplInputDataManager;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;

public class TestDBaseConnection {

	static final public String databaseFilename = "/home/sam/Téléchargements/FD_INDREGZA_2014.dbf";
	
	public static final String dictionaryFilename = "src/test/resources/MOD_INDREG_2014.txt";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadContent() {
		
		GosplSurveyFactory gsf = new GosplSurveyFactory();
		IGSSurvey survey = null;
		try {
			survey = gsf.getSurvey(databaseFilename, GSSurveyType.Sample);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		}
		assertEquals("first column of a DBF is always 0", 0, survey.getFirstColumnIndex());
		assertEquals("first row of a DBF is always 0", 0, survey.getFirstRowIndex());
		List<String> row0 = survey.readLine(0);
		System.out.println(survey.readLine(10));
		System.out.println(survey.readLine(100));
		System.out.println(survey.readLine(10000));

		System.out.println(row0);
		assertEquals("column count should match lines count", row0.size(), survey.getLastColumnIndex());
		
		//List<String> column0 = survey.readColumn(0);
		//assertEquals("column content should match lines count", survey.getLastRowIndex(), column0.size());
	}
	

	@Test
	public void testDecodeData() {
		
		GosplSurveyFactory gsf = new GosplSurveyFactory();
		IGSSurvey survey = null;
		try {
			survey = gsf.getSurvey(databaseFilename, GSSurveyType.Sample);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		}
		
		
		Collection<DemographicAttribute<? extends IValue>> attributes = ReadINSEEDictionaryUtils.readDictionnaryFromMODFile(
																dictionaryFilename
																);

		GosplPopulation pop = null;
		try {
			Map<String,String> keepOnlyEqual = new HashMap<>();
			keepOnlyEqual.put("DEPT", "75");
			//keepOnlyEqual.put("NAT13", "Marocains");
			
			pop = GosplInputDataManager.getSample(
					survey, 
					new HashSet<>(attributes), 
					100,
					keepOnlyEqual
					);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			throw new RuntimeException(e);
		}
		
		for (ADemoEntity e: pop) {
		
			System.err.println(e);
		}
		
		try {
			new GosplSurveyFactory().createSummary(new File("/tmp/test1.csv"), GSSurveyType.Sample, pop);
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidSurveyFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		assertEquals("first column of a DBF is always 0", 0, survey.getFirstColumnIndex());
		assertEquals("first row of a DBF is always 0", 0, survey.getFirstRowIndex());
		List<String> row0 = survey.readLine(0);
		System.out.println(survey.readLine(10));
		System.out.println(survey.readLine(100));
		System.out.println(survey.readLine(10000));

		System.out.println(row0);
		assertEquals("column count should match lines count", row0.size(), survey.getLastColumnIndex());
		
		*/
		
	}

}
