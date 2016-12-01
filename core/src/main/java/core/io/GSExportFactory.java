package core.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.exception.InvalidFileTypeException;
import core.io.geo.GeofileFactory;
import core.io.geo.RasterFile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.GSFeature;
import core.io.survey.IGSSurvey;
import core.io.survey.SurveyFactory;
import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;

/**
 * Main class to export genstar files
 * 
 * @author kevinchapuis
 *
 */
public class GSExportFactory {
	
	// ----------------------------------------------------------------------- //
	// ---------------------- POPULATION EXPORT SECTION ---------------------- //
	// ----------------------------------------------------------------------- //

	public static IGSSurvey createSurvey(File surveyFile, 
			IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population) 
					throws IOException, InvalidFileTypeException{
		SurveyFactory sf = new SurveyFactory();
		final char csvSep = ';';
		try {
			int individual = 1;
			final BufferedWriter bw = Files.newBufferedWriter(surveyFile.toPath());
			final Collection<AGenstarAttribute> attributes = population.getPopulationAttributes();
			bw.write("Individual" + csvSep
					+ attributes.stream().map(att -> att.getAttributeName()).collect(Collectors.joining(String.valueOf(csvSep)))
					+ "\n");
			for (final AGenstarEntity e : population) {
				bw.write(String.valueOf(individual++));
				for (final AGenstarAttribute attribute : attributes)
					bw.write(csvSep + e.getValueForAttribute(attribute).getStringValue());
				bw.write("\n");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		return sf.getSurvey(surveyFile, csvSep);
	}

	// -------------------------------------------------------------------- //
	// ---------------------- SPATIAL EXPORT SECTION ---------------------- //
	// -------------------------------------------------------------------- //
	
	public static ShapeFile createShapeFile(File shapefile, 
			IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population, 
			CoordinateReferenceSystem crs) 
			throws IOException, SchemaException {
		return new GeofileFactory().createShapeFile(shapefile, population, crs);  
	}
	
	public static ShapeFile createShapeFile(File shapefile, Collection<GSFeature> features) 
			throws IOException, SchemaException {
		return new GeofileFactory().createShapeFile(shapefile, features);
	}
	
	public static RasterFile createGeotiffFile(File geotiffile, float[][] pixels, ReferencedEnvelope envelope, CoordinateReferenceSystem crs) 
			throws IllegalArgumentException, IOException, TransformException{
		return new GeofileFactory().createRasterfile(geotiffile, pixels, RasterFile.DEF_NODATA.floatValue(), envelope, crs);
	}
	
	// -------------------------------------------------------------------- //
	// ---------------------- NETWORK EXPORT SECTION ---------------------- //
	// -------------------------------------------------------------------- //
	
}
