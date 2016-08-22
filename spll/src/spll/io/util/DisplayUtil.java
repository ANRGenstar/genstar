package spll.io.util;

import java.awt.Color;
import java.io.IOException;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

public class DisplayUtil {

	public static void displayData(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource){
		displayData(featureSource, null);
	}
	
	public static void displayData(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Style style) {

		// Create a map content and add our shapefile to it
		MapContent map = new MapContent();
		map.setTitle("StyleLab");
		try {
			System.out.println("featureSource: " + featureSource.getDataStore().getNames());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create a basic Style to render the features
		if(style == null)
			style = createLineStyle();

		// Add the features and the associated Style object to
		// the MapContent as a new Layer
		Layer layer = new FeatureLayer(featureSource, style);
		map.addLayer(layer);

		// Now display the map
		JMapFrame.showMap(map);
	}
	
	private static Style createLineStyle() {
		StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
		FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(Color.BLUE),
				filterFactory.literal(1));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geomettry of features
		 */
		LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);

		return style;
	}
	
}
