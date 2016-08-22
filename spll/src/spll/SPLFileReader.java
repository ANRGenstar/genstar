package spll;

import java.io.IOException;
import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.TransformException;

import spll.io.file.GeotiffFileIO;
import spll.io.file.ShapeFileIO;

public class SPLFileReader {

	public static void main(String[] args) {

		// check args content to avoid null or empty args shapefile path string
		if(args.length == 0 || args[0] == null){
			System.out.println("shape file name muste be specified as first args: it can not be empty or null");
			System.exit(0);
		}

		String filePath = args[0];
		String fileExtension = filePath.substring(filePath.length() - 4);

		ShapeFileIO shapeFile = null;
		GeotiffFileIO tiffFile = null;

		try {
			if(fileExtension.equals(".shp"))
				shapeFile = new ShapeFileIO(filePath);
			else if(fileExtension.equals(".tif"))
				tiffFile = new GeotiffFileIO(filePath);
			//DisplayUtil.displayData(shapeFile.getFeatureSource());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(fileExtension.equals(".shp")){
			String[] shapePathSplit = args[0].split("/");
			List<Feature> featureList = shapeFile.getFeatures();

			try {
				System.out.println("Shape file named "+shapePathSplit[shapePathSplit.length-1]
						+" contains "+featureList.size()
						+"\nFeature of type: "+shapeFile.getFeatureType().getTypeName()+" with "+shapeFile.getFeatureType().getAttributeCount()+" attributes each");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(Feature feature : featureList){
				SimpleFeature sF = (SimpleFeature) feature;
				System.out.println(feature.getName());
				for(Property prop : feature.getProperties()){
					Name name = prop.getName();
					Object value = sF.getAttribute( prop.getName() );
					System.out.println( name+"="+value+"," );
				}
			}
		} else if(fileExtension.equals(".tif")){
			String[] tiffPathSplit = args[0].split("/");
			List<Feature> featureList = tiffFile.getFeatures();
			System.out.println("Shape file named "+tiffPathSplit[tiffPathSplit.length-1]
					+" contains "+featureList.size()
					+"\nCRS: "+tiffFile.getCoordRefSystem()+" | number of band "+tiffFile.getBandId().length+" with names "+tiffFile.getBandId());
		}
	}

}
