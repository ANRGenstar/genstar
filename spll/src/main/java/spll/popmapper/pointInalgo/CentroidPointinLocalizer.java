package spll.popmapper.pointInalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class CentroidPointinLocalizer implements PointInLocalizer{

	@Override
	public Point pointIn(Geometry geom) {
		return geom.getCentroid();
	}

	@Override
	public List<Point> pointIn(Geometry geom, int nb) {
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < nb; i++)
			points.add(pointIn(geom));
		return points;
	}

	@Override
	public void setRand(Random rand) {
		// do nothing
	}

	
}
