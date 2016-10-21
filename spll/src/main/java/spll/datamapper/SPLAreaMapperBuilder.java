package spll.datamapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import io.data.geo.GeotiffFile;
import io.data.geo.IGSGeofile;
import io.data.geo.ShapeFile;
import io.data.geo.attribute.GSFeature;
import io.data.geo.attribute.GSPixel;
import io.data.geo.attribute.IGeoGSAttribute;
import io.data.geo.attribute.IGeoValue;
import io.data.writers.GSExportFactory;
import io.util.GSBasicStats;
import io.util.GSPerformanceUtil;
import spll.algo.ISPLRegressionAlgorithm;
import spll.algo.LMRegressionOLSAlgorithm;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.SPLAreaMatcherFactory;
import spll.datamapper.variable.SPLVariable;

public class SPLAreaMapperBuilder extends ASPLMapperBuilder<SPLVariable, Double> {

	private static final boolean LOGSYSO = true;
	private SPLMapper<SPLVariable, Double> mapper;

	private static int pixelRendered = 0;

	public SPLAreaMapperBuilder(ShapeFile mainFile, Name propertyName,
			List<IGSGeofile> ancillaryFiles, Collection<IGeoValue> variables) {
		this(mainFile, propertyName, ancillaryFiles, variables, new LMRegressionOLSAlgorithm());
	}
	
	public SPLAreaMapperBuilder(ShapeFile mainFile, Name propertyName,
			List<IGSGeofile> ancillaryFiles, Collection<IGeoValue> variables, 
			ISPLRegressionAlgorithm<SPLVariable, Double> regAlgo) {
		super(mainFile, propertyName, ancillaryFiles);
		super.setRegressionAlgorithm(regAlgo);
		super.setMatcherFactory(new SPLAreaMatcherFactory(variables));
	}

	@Override
	public SPLMapper<SPLVariable, Double> buildMapper() throws IOException, TransformException, InterruptedException, ExecutionException {
		if(mapper == null){
			mapper = new SPLMapper<>();
			mapper.setMainSPLFile(mainFile);
			mapper.setMainProperty(propertyName);
			mapper.setRegAlgo(regressionAlgorithm);
			mapper.setMatcherFactory(matcherFactory);
			for(IGSGeofile file : ancillaryFiles)
				mapper.insertMatchedVariable(file);
		}
		return mapper;
	}

	@Override
	public GeotiffFile buildOutput(File outputFile, GeotiffFile outputFormat) throws IllegalRegressionException, TransformException, IndexOutOfBoundsException, IOException {
		if(mapper == null)
			throw new IllegalAccessError("Cannot create output before a SPLMapper has been built and regression done");
		if(!ancillaryFiles.contains(outputFormat))
			throw new IllegalArgumentException("output format file must be one of ancillary files use to proceed regression");

		GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regression data to output raster", LOGSYSO);

		// Define output format
		int rows = outputFormat.getRowNumber();
		int columns = outputFormat.getColumnNumber();
		float[][] pixels = new float[columns][rows];

		// Store regression result
		Map<SPLVariable, Double> regCoef = mapper.getRegression();
		Map<GSFeature, Double> corCoef = mapper.getCorrectionCoefficient();

		// Define utilities
		Collection<GSFeature> mainFeatures = super.mainFile.getGeoData(); 
		Collection<IGSGeofile> ancillaries = new ArrayList<>(super.ancillaryFiles);
		ancillaries.remove(outputFormat);

		// Iterate over pixels
		gspu.sysoStempPerformance("Start iterating over pixels", this);
		IntStream.range(0, columns).parallel().forEach(
				x -> IntStream.range(0, rows).forEach(
						y -> pixels[x][y] = (float) this.computePixelWithinOutput(x, y, outputFormat,  ancillaries,
								mainFeatures, regCoef, corCoef, gspu)
						)
				);
		pixelRendered = 0;
		
		gspu.sysoStempMessage("End processing pixels - start processing some statistics on them");
		
		GSBasicStats<Double> bs = new GSBasicStats<>(GSBasicStats.transpose(pixels));
		
		gspu.sysoStempMessage("Some statistics: \n"+bs.getStatReport());
		
		return GSExportFactory.createGeotiffFile(outputFile, pixels, outputFormat.getCoordRefSystem());
	}

	@Override
	public ShapeFile buildOutput(File outputFile, ShapeFile formatFile) {
		// TODO Auto-generated method stub
		return null;
	}


	/////////////////////////////////////////////////////////////////////////////
	// --------------------------- INNER UTILITIES --------------------------- //
	/////////////////////////////////////////////////////////////////////////////

	private double computePixelWithinOutput(int x, int y, GeotiffFile geotiff, Collection<IGSGeofile> ancillaries,
			Collection<GSFeature> mainFeatures, Map<SPLVariable, Double> regCoef, Map<GSFeature, Double> corCoef,
			GSPerformanceUtil gspu){

		// Output progression
		int prop10for100 = Math.round(Math.round(geotiff.getRowNumber() * geotiff.getColumnNumber() * 0.1d));
		if((++pixelRendered+1) % prop10for100 == 0)
			gspu.sysoStempPerformance((pixelRendered+1) / (prop10for100 * 10.0), this);

		// Get the current pixel value
		GSPixel refPixel = null;
		try {
			refPixel = geotiff.getPixel(x, y);
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Retain info about pixel and his context
		Point pixPosition = refPixel.getPosition();
		Optional<GSFeature> feat = mainFeatures.stream().filter(ft -> ft.getGeometry().contains(pixPosition))
				.findFirst();
		if(!feat.isPresent())
			return 0d;
		Collection<IGeoValue> pixData = refPixel.getValues();
		double pixArea = refPixel.getArea();
		double outputCoeff = corCoef.get(feat.get());

		// Setup output value for the pixel based on pixels' band values
		double output = regCoef.entrySet().stream().filter(var -> pixData.contains(var.getKey().getValue()))
				.mapToDouble(var -> var.getValue() * pixArea).sum();

		// Iterate over other explanatory variables to update pixels value
		for(IGSGeofile otherExplanVarFile : ancillaries){
			Iterator<? extends IGeoGSAttribute> otherItt = otherExplanVarFile
					.getGeoAttributeIteratorWithin(refPixel.getGeometry());
			while(otherItt.hasNext()){
				IGeoGSAttribute other = otherItt.next();
				List<IGeoValue> otherData = other.getPropertiesAttribute()
						.stream().map(prop -> other.getValue(prop))
						.collect(Collectors.toList());
				Set<SPLVariable> otherValues = regCoef.keySet()
						.stream().filter(var -> otherData.contains(var.getValue()))
						.collect(Collectors.toSet());
				output += otherValues.stream().mapToDouble(val -> regCoef.get(val) * pixPosition.getArea()).sum();
			}
		}

		return output * outputCoeff;
	}

	@SuppressWarnings("unused")
	private double computePixelIntersectOutput(int x, int y, GeotiffFile geotiff, Collection<IGSGeofile> ancillaries,
			Collection<GSFeature> mainFeatures, Map<SPLVariable, Double> regCoef, Map<GSFeature, Double> corCoef,
			GSPerformanceUtil gspu) {

		// Output progression
		int prop10for100 = Math.round(Math.round(geotiff.getRowNumber() * geotiff.getColumnNumber() * 0.1d));
		if((++pixelRendered + 1) % prop10for100 == 0)
			gspu.sysoStempPerformance(pixelRendered+1 / (prop10for100 * 10.0), this);

		// Get the current pixel value
		GSPixel refPixel = null;
		try {
			refPixel = geotiff.getPixel(x, y);
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Retain main feature the pixel is within
		Geometry pixGeom = refPixel.getGeometry(); 
		List<GSFeature> feats = mainFeatures.stream()
				.filter(ft -> ft.getGeometry().intersects(pixGeom))
				.collect(Collectors.toList());
		if(feats.isEmpty())
			return 0f;

		// Get the values contain in the pixel bands
		Collection<IGeoValue> pixData = refPixel.getValues();
		double pixArea = refPixel.getArea();

		// Setup output value for the pixel based on pixels' band values
		double output = regCoef.entrySet().stream().filter(var -> pixData.contains(var.getKey().getValue()))
				.mapToDouble(var -> var.getValue() * pixArea).sum();

		// Iterate over other explanatory variables to update pixels value
		for(IGSGeofile otherExplanVarFile : ancillaries){
			Iterator<? extends IGeoGSAttribute> otherItt = otherExplanVarFile
					.getGeoAttributeIteratorIntersect(pixGeom);
			while(otherItt.hasNext()){
				IGeoGSAttribute other = otherItt.next();
				List<IGeoValue> otherData = other.getPropertiesAttribute()
						.stream().map(prop -> other.getValue(prop))
						.collect(Collectors.toList());
				Set<SPLVariable> otherValues = regCoef.keySet()
						.stream().filter(var -> otherData.contains(var.getValue()))
						.collect(Collectors.toSet());
				output += otherValues.stream().mapToDouble(val -> 
				regCoef.get(val) * other.getGeometry().intersection(pixGeom).getArea()).sum();
			}
		}

		// Compute corrected value based on output data (to balanced for unknown determinant information)
		// Intersection correction try /catch clause come from GAMA
		float correctedOutput = 0f; 
		for(GSFeature f : feats){
			Geometry fGeom = f.getGeometry();
			Geometry intersectGeom = null;
			try {
				intersectGeom = fGeom.intersection(pixGeom);
			} catch (final Exception ex) {
				try {
					final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
					intersectGeom = GeometryPrecisionReducer.reducePointwise(fGeom, pm)
							.intersection(GeometryPrecisionReducer.reducePointwise(pixGeom, pm));
				} catch (final Exception e) {
					// AD 12/04/13 : Addition of a third method in case of
					// exception
					try {
						intersectGeom = fGeom.buffer(0.01, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT)
								.intersection(pixGeom.buffer(0.01, BufferParameters.DEFAULT_QUADRANT_SEGMENTS,
										BufferParameters.CAP_FLAT));
					} catch (final Exception e2) {
						intersectGeom = null;
					}
				}
			}
			correctedOutput += Math.round(output * intersectGeom.getArea() / pixGeom.getArea() * corCoef.get(f));
		}
		return correctedOutput;
	}

}
