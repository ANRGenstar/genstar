package io.datareaders.georeader;

import java.io.IOException;
import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.geodat.IGeoGSAttributes;

public interface IGeoGSFileIO<G extends IGeoGSAttributes<A, D>, A, D> {

	public GeoGSFileType getGeoGSFileType();
	
	/**
	 * Retrieve main spatial component of the file: {@link Feature} from GeoTools api
	 * 
	 * @return
	 * @throws TransformException 
	 * @throws IOException 
	 */
	public List<G> getGeoData() throws IOException, TransformException;

	/**
	 * Says if geographical information of the two files are congruent in term of space.
	 * That implies that, if true, the two files share at least the same projection, coordinate system
	 * and some point in space (coordinate that are present in the two files) 
	 * 
	 * @param file
	 * @return
	 */
	public boolean isCoordinateCompliant(IGeoGSFileIO<G, A, D> file);

	/**
	 * The {@link CoordinateReferenceSystem} used by this file
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCoordRefSystem();
	
}
