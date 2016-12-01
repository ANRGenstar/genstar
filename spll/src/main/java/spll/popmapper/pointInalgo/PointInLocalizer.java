package spll.popmapper.pointInalgo;

import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public interface PointInLocalizer {

	public Point pointIn(Geometry geom);

	public List<Point> pointIn(Geometry geom, int nb);
	
	public void setRand(Random rand);
}
