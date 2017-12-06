package gospl.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.GosplPopulationInDatabase;
import gospl.distribution.GosplInputDataManager;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.io.insee.ReadINSEEDictionaryUtils;

public class TestGosplPopulationInDatabase {

	TemporaryFolder tstFolder = new TemporaryFolder();

	
	private GosplPopulation getGoSPLPopulation() {
		
		Collection<DemographicAttribute<? extends IValue>> attributes = ReadINSEEDictionaryUtils
				.readDictionnaryFromMODFile("src/test/resources/MOD_GERLAND.txt");
				
		GosplSurveyFactory gsf = new GosplSurveyFactory();
		IGSSurvey survey = null;
		try {
			survey = gsf.getSurvey("src/test/resources/gerland_sample_incomplete.csv",0,';',1,0, GSSurveyType.Sample);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			e.printStackTrace();
			fail("error in format" + e);
		}
		
		GosplPopulation pop = null;
		try {
			pop = GosplInputDataManager.getSample(
					survey, 
					attributes, 
					10000,
					Collections.emptyMap() // TODO parameters for that
					);
		} catch (IOException | InvalidSurveyFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return pop;
	}
	
	@Test
	public void testFromGoSPLPopulationInMemory() {

		new GosplPopulationInDatabase(getGoSPLPopulation());
		
	}
	

	@Test
	public void testFromGoSPLInFileSize() throws IOException {

		
		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(
				File.createTempFile( "junit", "hsqdlb"),
				o);
		assertEquals("wrong size", o.size(), p.size());
		
	}
	
	@Test
	public void testFromGoSPLInMemorySize() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);
		assertEquals("wrong size", o.size(), p.size());
		
	}
	
	@Test
	public void testIterateType() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);
		Iterator<ADemoEntity> itEntities = p.iterator(GosplPopulationInDatabase.DEFAULT_ENTITY_TYPE);
		int count = 0;
		while (itEntities.hasNext()) {
			ADemoEntity e = itEntities.next();
			count++;
			System.out.println(e);
		}
		assertEquals("wrong size", o.size(), count);
		
	}
	

	@Test
	public void testIterateAll() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);
		Iterator<ADemoEntity> itEntities = p.iterator();
		int count = 0;
		while (itEntities.hasNext()) {
			ADemoEntity e = itEntities.next();
			count++;
			System.out.println(e);
		}
		assertEquals("wrong size", o.size(), count);
		
	}
	
	@Test
	public void testEmptyTrueAfterEmpty() {

		GosplPopulationInDatabase p = new GosplPopulationInDatabase();
		assertTrue("the table should be empty", p.isEmpty());
		
	}
	

	@Test
	public void testClearEmptySize() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);
		p.clear();
		assertTrue("the table should be empty", p.isEmpty());

		assertEquals("the size should be 0", 0, p.size());
		
		Iterator<ADemoEntity> itEntities = p.iterator();
		int count = 0;
		while (itEntities.hasNext()) {
			ADemoEntity e = itEntities.next();
			count++;
		}
		assertEquals("the cound should be 0", 0, count);

	}
	

	@Test
	public void testDelete() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		// keep a few entities aside
		List<ADemoEntity> fewEntities = new LinkedList<>();
		Iterator<ADemoEntity> itEntities = p.iterator();
		int count = 0;
		while (itEntities.hasNext()) {
			ADemoEntity e = itEntities.next();
			fewEntities.add(e);
			if (count++ >= 5)
				break;
		}
		
		// remove one entity
		assertTrue("the entity should accept to be removed", p.remove(fewEntities.get(0)));
		assertEquals("the count should be lower after the removal", o.size()-1, p.size());

		// remove an entity not there entity
		assertFalse("the entity should not accept to be removed", p.remove(fewEntities.get(0)));
		assertEquals("the count should be the same after the removal failure", o.size()-1, p.size());

	}
	

	@Test
	public void testDeleteMany() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		// keep a few entities aside
		List<ADemoEntity> fewEntities = new LinkedList<>();
		Iterator<ADemoEntity> itEntities = p.iterator();
		int count = 0;
		while (itEntities.hasNext()) {
			ADemoEntity e = itEntities.next();
			fewEntities.add(e);
			if (count++ >= 300)
				break;
		}
		
		// remove one entity
		assertTrue("the entity should accept to be removed", p.removeAll(fewEntities));
		assertEquals("the count should be lower after the removal", o.size()-fewEntities.size(), p.size());

		// remove an entity not there entity
		assertFalse("the entity should not accept to be removed", p.remove(fewEntities.get(0)));
		assertEquals("the count should be the same after the removal failure",  o.size()-fewEntities.size(), p.size());

	}

}
