package core.metamodel.geo.io;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.crypto.dsig.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;

public interface IGSGeofile<Entity extends AGeoEntity> {

	/**
	 * Gives the geographic type of data within the file: either to be
	 * raster or vector
	 * 
	 * @see GeoGSFileType
	 * @return GeoGSFileType#RASTER or GeoGSFileType#VECTOR
	 */
	public GeoGSFileType getGeoGSFileType();
	
	/**
	 * Retrieve main spatial component of the file: the type of data implement {@link IGeoGSAttribute}.
	 * This method could leads to store huge amount of data into collection and then not be quite efficient
	 * 
	 * @return
	 * @throws TransformException 
	 */
	public Collection<Entity> getGeoEntity() throws IOException;
	
	/**
	 * Retrieve all possible variable within spatial component.
	 * This method could leads to store huge amount of data into collection and then not be quite efficient
	 * 
	 * @return 
	 */
	public Collection<AGeoValue> getGeoValues() ;
	
	/**
	 * Retrieve all possible attribute that geo entity can embody
	 * 
	 * @return
	 */
	public Collection<AGeoAttribute> getGeoAttributes();


	/**
	 * Says if geographical information of the two files are congruent in term of space.
	 * That implies that, if true, the two files share at least the same projection, coordinate system
	 * and some point in space (coordinate that are present in the two files) 
	 * 
	 * @param file
	 * @return
	 * @throws FactoryException 
	 */
	public boolean isCoordinateCompliant(IGSGeofile<? extends AGeoEntity> file);
	
	/**
	 * Access to file coordinate referent system through a WKT representation.
	 * 
	 * @return
	 */
	public String getWKTCoordinateReferentSystem();
	
	/**
	 * Access to file content without memory stored collection
	 * 
	 * @return
	 */
	public Iterator<Entity> getGeoEntityIterator() ;
	
	/**
	 * Access to file data but limited to geo data within the given Geometry.
	 * 
	 * @param feature
	 * @return Iterator 
	 */
	public Iterator<Entity> getGeoEntityIteratorWithin(Geometry geom);
	
	/**
	 * Access to file data but limited to geo data within the given Geometry.
	 * 
	 * @param geom
	 * @return Collection 
	 */
	public Collection<Entity> getGeoEntityWithin(Geometry geom);
	
	/**
	 * Access to file data but limited to geo data intersected with the given Geometry
	 * 
	 * @param feature
	 * @return Iterator 
	 */
	public Iterator<Entity> getGeoEntityIteratorIntersect(Geometry geom);

	/**
	 * Access to file data but limited to geo data intersected with the given Geometry
	 * 
	 * @param geom
	 * @return Collection 
	 */
	public Collection<Entity> getGeoEntityIntersect(Geometry geom);
	
	/**
	 * Access to file envelope as define in JTS 
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Envelope getEnvelope() throws IOException;
	
}
