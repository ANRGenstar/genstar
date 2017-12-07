package gospl.io.insee;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides test urls with data suitable for ... test
 * 
 * @author Samuel Thiriot
 *
 */
public class INSEETestURLs {

	public final static URL url_RGP2014_LocaliseRegion_ZoneD_dBase;
	
	// init these urls
	static {{
		try {
			url_RGP2014_LocaliseRegion_ZoneD_dBase = new URL(
					"https://www.insee.fr/fr/statistiques/fichier/2866269/rp2014_indregzd_dbase.zip");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		};
	}}
			
	

}
