package gospl.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.GosplPopulationInDatabase;
import gospl.io.insee.DownloadINSEESampleData;
import gospl.io.insee.INSEETestURLs;

public class TestGosplPopulationInDatabase {

	TemporaryFolder tstFolder = new TemporaryFolder();

	
	private GosplPopulation getGoSPLPopulation() {
		DownloadINSEESampleData inseeData = new DownloadINSEESampleData(
				INSEETestURLs.url_RGP2014_LocaliseRegion_ZoneD_dBase,
				"UTF-8");
		
					
		return inseeData.getSamplePopulation(10000);
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
			if (count < 10)
				System.out.println(" * "+e);
			else if (count == 10)
				System.out.println(" * [...]");
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
			itEntities.next();
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
	

	@Test
	public void testAddAgent() {

		GosplPopulation o = getGoSPLPopulation();
		
		// create an empty population
		GosplPopulationInDatabase p = new GosplPopulationInDatabase();

		Iterator<ADemoEntity> itEntities = o.iterator();
		
		// one entity
		assertTrue("the entity should accept to be added", p.add(itEntities.next()));
		assertEquals("the count should be 1 after adding", 1, p.size());

		// add another entity
		assertTrue("the entity should accept to be added", p.add(itEntities.next()));
		assertEquals("the count should be lower after the removal", 2, p.size());

		
	}
	

	@Test
	public void testAddAgents() {

		GosplPopulation o = getGoSPLPopulation();
		
		// create an empty population
		GosplPopulationInDatabase p = new GosplPopulationInDatabase();

		Iterator<ADemoEntity> itEntities = o.iterator();
		List<ADemoEntity> fewEntities = new LinkedList<>();
		for (int i=0;i<10;i++) {
			ADemoEntity e = itEntities.next();
			fewEntities.add(e);
		}
		
		// one entity
		assertTrue("the entities should accept to be added", p.addAll(fewEntities));
		assertEquals("the count should be 10 after adding", 10, p.size());

		// add another entity
		fewEntities.add(itEntities.next());
		assertTrue("the entities should accept to be added", p.addAll(fewEntities));
		assertEquals("the count should 11 after adding", 11, p.size());

		// add the same 
		assertFalse("the entities should not accept to be added", p.addAll(fewEntities));
		assertEquals("the count should be the same after adding existing objects", 11, p.size());
		
	}
	

	@Ignore
	@Test
	public void testCountEntitiesHavingOneValue() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		for (Attribute<? extends IValue> a : o.getPopulationAttributes()) {
		
			int total = 0;
			System.out.println(a.getAttributeName()+":");
			for (IValue v: a.getValueSpace().getValues()) {
			
				int c = p.getCountHavingValues(a, v);
				
				if (total < 5)
					System.out.println("* "+v.toString()+" : "+c);
				else if (total == 5)
					System.out.println("* [...]");
				
				total += c;
			}

			assertEquals("the count of each value should sum up to the population size", o.size(), total);

		}
				
	}
	
	@Ignore
	@Test
	public void testCountEntitiesHavingTwoValues() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		int totalCombinationsTested = 0;
		
		Attribute<? extends IValue> previousAttribute = null;
		for (Attribute<? extends IValue> a : o.getPopulationAttributes()) {
		
			if (previousAttribute == null) {
				previousAttribute = a;
				continue;
			}
			
			int total = 0;
			System.out.println(a.getAttributeName()+" and "+previousAttribute.getAttributeName()+":");
			for (IValue vPrevious: previousAttribute.getValueSpace().getValues()) {

				for (IValue v: a.getValueSpace().getValues()) {
				
					Map<Attribute<? extends IValue>,Collection<IValue>> a2vv = new HashMap<>();
					a2vv.put(a, Arrays.asList(v));
					a2vv.put(previousAttribute, Arrays.asList(vPrevious));
					
					int c = p.getCountHavingValues(a2vv);
					
					if (total < 5)
						System.out.println("* "+a2vv+" : "+c);
					else if (total == 10)
						System.out.println("* [...]");
					
					total += c;
				}
				
			}

			assertEquals("the count of each value should sum up to the population size", o.size(), total);

			if (++totalCombinationsTested >= 10)
				break;
		}
				
	}
	
	@Ignore
	@Test
	public void testIterateEntitiesHavingOneValue() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		for (Attribute<? extends IValue> a : o.getPopulationAttributes()) {
		
			int total = 0;
			System.out.println(a.getAttributeName()+":");
			for (IValue v: a.getValueSpace().getValues()) {
			
				System.out.println("testing "+a.getAttributeName()+"="+v);
				
				Iterator<ADemoEntity> it = p.getEntitiesHavingValues(a, v);
				int i=0;
				while (it.hasNext()) {
					ADemoEntity e = it.next();
					if (i < 30)
						System.out.println("* "+e);
					else if (i==30)
						System.out.println("* [...]");
					
					assertEquals(
							"the values should be filtered according to our demand",
							v,
							e.getValueForAttribute(a)
							);
					total++;
					i++;
					
				}
			}

			assertEquals("the count of each value should sum up to the population size", o.size(), total);

		}
				
	}
	
	@Ignore
	@Test
	public void testIterateEntitiesHavingTwoValues() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		Attribute<? extends IValue> previousAttribute = null;
		int totalCombinationsTested = 0;
		for (Attribute<? extends IValue> a : o.getPopulationAttributes()) {
		
			if (previousAttribute == null) {
				previousAttribute = a;
				continue;
			}
			
			int total = 0;
			System.out.println(a.getAttributeName()+" and "+previousAttribute.getAttributeName()+":");
			for (IValue vPrevious: previousAttribute.getValueSpace().getValues()) {

				for (IValue v: a.getValueSpace().getValues()) {
				
					Map<Attribute<? extends IValue>,Collection<IValue>> a2vv = new HashMap<>();
					a2vv.put(a, Arrays.asList(v));
					a2vv.put(previousAttribute, Arrays.asList(vPrevious));
					
					System.out.println("testing "+a.getAttributeName()+"="+v+", "+previousAttribute+"="+vPrevious);
					
					Iterator<ADemoEntity> it = p.getEntitiesHavingValues(a2vv);
					int i=0;
					while (it.hasNext()) {
						ADemoEntity e = it.next();
						if (i < 5)
							System.out.println("* "+e);
						else if (i==10)
							System.out.println("* [...]");
						
						assertEquals(
								"the values should be filtered according to our demand",
								v,
								e.getValueForAttribute(a)
								);
						assertEquals(
								"the values should be filtered according to our demand",
								vPrevious,
								e.getValueForAttribute(previousAttribute)
								);
						total++;
						i++;
						
					}
										
				}
				
			}

			assertEquals("the count of each value should sum up to the population size", o.size(), total);

			if (++totalCombinationsTested >= 10)
				break;
		}
				
	}
	

	@Test
	public void testContains() {
		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		ADemoEntity e = p.iterator().next();
		
		assertTrue(p.contains(e));
		
	}
	
	@Test
	public void testGetEntityWithId() {
		
		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);

		ADemoEntity e = p.iterator().next();
		
		assertNotNull(p.getEntityForId(e.getEntityId()));
		// TODO check attributes ?

		assertNull(p.getEntityForId("this does not exists"));

	
	}

}
