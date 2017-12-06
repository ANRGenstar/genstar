package gospl.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import core.metamodel.attribute.demographic.DemographicAttribute;
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
	public void testSize() {

		GosplPopulation o = getGoSPLPopulation();
		GosplPopulationInDatabase p = new GosplPopulationInDatabase(o);
		assertEquals("wrong size", o.size(), p.size());
		
	}

}
