package io.datareaders.georeader;

import java.io.IOException;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.geodat.IGeoGSAttribute;

public interface IGeoGSFileIO<A, D> {

	public GeoGSFileType getGeoGSFileType();
	
	/**
	 * Retrieve main spatial component of the file: of type {@code <G>} define as generics
	 * 
	 * @return
	 * @throws TransformException 
	 * @throws IOException 
	 */
	public List<? extends IGeoGSAttribute<A, D>> getGeoData() throws IOException, TransformException;

	/**
	 * Says if geographical information of the two files are congruent in term of space.
	 * That implies that, if true, the two files share at least the same projection, coordinate system
	 * and some point in space (coordinate that are present in the two files) 
	 * 
	 * @param file
	 * @return
	 */
	public boolean isCoordinateCompliant(IGeoGSFileIO<A, D> file);

	/**
	 * The {@link CoordinateReferenceSystem} used by this file
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCoordRefSystem();
	
}
