package spll.util;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class SpllUtil {

	/**
	 * Return a string representation of a 2D matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public static String getStringMatrix(float[][] matrix){
		String string = "";
		for(int x = 0; x < matrix.length; x++){
			for(int y = 0; y < matrix[x].length; y++){
				string += "["+matrix[x][y]+"]";
			}
			string += "\n";
		}
		return string;
	}
	
	/**
	 * Retrieve a CRS from a WKT encoding. If Geotools fails to initialize
	 * a CRS factory, then null is return together with a stack trace of
	 * failure
	 * 
	 * @param wktCRS
	 * @return
	 */
	public static CoordinateReferenceSystem getCRSfromWKT(String wktCRS) {
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.parseWKT(wktCRS);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return crs;
	}

}
