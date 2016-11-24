package spll.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import core.io.geo.GeoGSFileType;
import core.io.geo.IGSGeofile;
import core.io.geo.RasterFile;
import core.io.geo.entity.attribute.value.AGeoValue;

public class SpllUtil {

	/**
	 * Return values of meaningful geographical purpose (i.e. exclude raster noData) contains 
	 * in given files collection
	 * 
	 * @param vals
	 * @return
	 */
	public static Collection<AGeoValue> getMeaningfullValues(Collection<String> vals, Collection<IGSGeofile> files){
		Collection<AGeoValue> values = new HashSet<>();
		if(vals.isEmpty()){
			for(IGSGeofile file : files){
				if(file.getGeoGSFileType().equals(GeoGSFileType.RASTER))
					values.addAll(file.getGeoValues()
							.stream().filter(val -> !((RasterFile)file).isNoDataValue(val))
							.collect(Collectors.toSet()));
				else
					values.addAll(file.getGeoValues());
			}
		} else {
			values.addAll(files.stream()
					.flatMap(file -> file.getGeoValues().stream())
					.filter(var -> vals.stream().anyMatch(vName -> var.valueEquals(vName)))
					.collect(Collectors.toSet()));
		}
		return values;
	}

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

}
