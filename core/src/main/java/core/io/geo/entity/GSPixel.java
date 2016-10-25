package core.io.geo.entity;

import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.attribute.value.AGeoValue;

public class GSPixel extends AGeoEntity {
	
	private Envelope2D pixel;
	
	protected GSPixel(Set<AGeoValue> bandsData, Envelope2D pixel) {
		super(bandsData, "px ["+pixel.getCenterX()+";"+pixel.getCenterY()+"]");
		this.pixel = pixel;
	}
	
	@Override
	public double getArea() {
		return pixel.getWidth() * pixel.getHeight();
	}

	@Override
	public Point getPosition() {
		return new GeometryBuilder().point(pixel.getCenterX(), pixel.getCenterY());
		//return JTSFactoryFinder.getGeometryFactory().createPoint(new Coordinate(pixel.getCenterX(), pixel.getCenterY()));
	}
	
	@Override
	public Geometry getGeometry() {
		return new GeometryBuilder().polygon(
				pixel.getMinX(), pixel.getMinY(),
				pixel.getMinX(), pixel.getMaxY(),
				pixel.getMaxX(), pixel.getMinY(),
				pixel.getMaxX(), pixel.getMaxY());
	}
	
	public double getMaxX() {
		return pixel.getMaxX();
	}
	
	public double getMaxY() {
		return pixel.getMaxY();
	}
	
	@Override
	public String toString() {
		return this.getGenstarName();
	}
	
}
