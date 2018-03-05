package gospl.io.insee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

import org.junit.Test;

import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;

public class TestDownloadINSEEData {

	@Test
	public void testOpenDictionnaryRGP2014() throws MalformedURLException {
		
		DownloadINSEESampleData d = new DownloadINSEESampleData(
				INSEETestURLs.url_RGP2014_LocaliseRegion_ZoneD_dBase, "UTF-8");
		
		IGenstarDictionary<Attribute<? extends IValue>> dico = d.getDictionnary();
		
		assertEquals("wrong count of entities", 97, dico.size());
		
	}
	

	@Test
	public void testOpenDBaseRGP2014() throws MalformedURLException {
		
		DownloadINSEESampleData d = new DownloadINSEESampleData(
				INSEETestURLs.url_RGP2014_LocaliseRegion_ZoneD_dBase, "UTF-8");
		
		File dbaseFile = d.getSampleFile();
		
		assertNotNull(dbaseFile);
		assertTrue(dbaseFile.exists());
		assertTrue(dbaseFile.canRead());
		
		int maxToRead = 1000;

		GosplPopulation pop = d.getSamplePopulation(maxToRead);
				
		assertEquals("wrong count of entities", maxToRead, pop.size());
		
	}

}
