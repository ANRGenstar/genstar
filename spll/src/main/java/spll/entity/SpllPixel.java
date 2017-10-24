package spll.entity;

import java.util.Map;

import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.attribute.GeographicAttribute;
import core.metamodel.value.numeric.ContinuedValue;

public class SpllPixel extends AGeoEntity<ContinuedValue> {
	
	private Envelope2D pixel;
	
	private int gridX;
	private int gridY;
	
	protected SpllPixel(Map<GeographicAttribute<? extends ContinuedValue>, ContinuedValue> bandsData, Envelope2D pixel, int gridX, int gridY) {
		super(bandsData, "px ["+pixel.getCenterX()+";"+pixel.getCenterY()+"]");
		this.gridX = gridX;
		this.gridY = gridY;
		this.pixel = pixel;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Get pixel's area in square meter 
	 * 
	 */
	@Override
	public double getArea() {
		return Math.sqrt(pixel.getWidth() * pixel.getHeight());
	}

	@Override
	public Point getLocation() {
		return new GeometryBuilder().point(pixel.getCenterX(), pixel.getCenterY());
	}
	
	@Override
	public Geometry getGeometry() {
		return new GeometryBuilder().polygon(
				pixel.getMinX(), pixel.getMinY(),
				pixel.getMinX(), pixel.getMaxY(),
				pixel.getMaxX(), pixel.getMaxY(),
				pixel.getMaxX(), pixel.getMinY());
	}
	
	public int getGridX() {
		return gridX;
	}
	
	public int getGridY() {
		return gridY;
	}
	
	@Override
	public String toString() {
		return this.getGenstarName();
	}
	
}
