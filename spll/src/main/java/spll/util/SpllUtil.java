package spll.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;
import core.metamodel.geo.io.GeoGSFileType;
import core.metamodel.geo.io.IGSGeofile;
import spll.io.SPLRasterFile;

public class SpllUtil {

	/**
	 * Return values of meaningful geographical purpose (i.e. exclude raster noData) contains 
	 * in given files collection and that complains to given {@code vals} argument collection
	 * of attribute name
	 * 
	 * @param vals
	 * @return
	 */
	public static Collection<? extends AGeoValue> getValuesFor(Collection<String> vals, List<IGSGeofile<? extends AGeoEntity>> endogeneousVarFile){
		Collection<AGeoValue> values = new HashSet<>();
		if(vals.isEmpty()){
			for(IGSGeofile<? extends AGeoEntity> file : endogeneousVarFile){
				if(file.getGeoGSFileType().equals(GeoGSFileType.RASTER))
					values.addAll(file.getGeoValues()
							.stream().filter(val -> !((SPLRasterFile)file).isNoDataValue(val))
							.collect(Collectors.toSet()));
				else
					values.addAll(file.getGeoValues());
			}
		} else {
			values.addAll(endogeneousVarFile.stream()
					.flatMap(file -> file.getGeoValues().stream())
					.filter(var -> vals.stream().anyMatch(vName -> var.valueEquals(vName)))
					.collect(Collectors.toSet()));
		}
		return values;
	}

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
