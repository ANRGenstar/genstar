package io.datareaders.georeader;

import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface ISPLFileIO<F extends Feature> {

	/**
	 * Retrieve main spatial component of the file: {@link Feature} from GeoTools api
	 * 
	 * @return
	 */
	public List<F> getFeatures();

	/**
	 * Says if geographical information of the two files are congruent in term of space.
	 * That implies that, if true, the two files share at least the same projection, coordinate system
	 * and some point in space (coordinate that are present in the two files) 
	 * 
	 * @param file
	 * @return
	 */
	public boolean isCoordinateCompliant(ISPLFileIO<F> file);

	/**
	 * The {@link CoordinateReferenceSystem} used by this file
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCoordRefSystem();
	
}
