package firok.tool.alloywrench;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;

import java.util.*;

public class JtsTests
{
	@Test
	void test()
	{
		var facShape = JtsSpatialContext.GEO.getShapeFactory();
		var facGeo = facShape.getGeometryFactory();
		var p1pt = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(1, 0),
				new Coordinate(1, 1),
				new Coordinate(0, 1),
				new Coordinate(0, 0),
		};
		var p1 = facGeo.createPolygon(p1pt);

		var p2pt = new Coordinate[] {
				new Coordinate(0.5, 0),
				new Coordinate(1, 0),
				new Coordinate(1, 1),
				new Coordinate(0.5, 1),
				new Coordinate(0.5, 0),
		};
		var p2 = facGeo.createPolygon(p2pt);

		System.out.println("coors: " + Arrays.toString(p2.getCoordinates()));

		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p1.intersection(p2));

		var p3pt = new Coordinate[] {
				new Coordinate(2, 0),
				new Coordinate(3, 0),
				new Coordinate(3, 1),
				new Coordinate(2, 1),
				new Coordinate(2, 0),
		};
		var p3 = facGeo.createPolygon(p3pt);
		System.out.println(p3);
		var p3_p2 = p3.intersection(p2);
		System.out.println(p3_p2);

		System.out.println(p3_p2.isEmpty());
		System.out.println(p3_p2.isValid());
		System.out.println(p3_p2.isSimple());

	}
}
