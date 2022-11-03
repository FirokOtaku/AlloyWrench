package firok.tool.alloywrench.util;

import firok.tool.alloywrench.bean.DecimalRect;
import firok.tool.alloywrench.bean.IntRect;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;

import java.math.BigDecimal;

public class Geos
{
	private static final GeometryFactory facGeo = JtsSpatialContext.GEO.getShapeFactory().getGeometryFactory();

	/**
	 * [x1, y1, x2, y2, ...] -> polygon
	 * (方法内部会处理曲线闭合)
	 * */
	public static Geometry toPolygon(BigDecimal[] numbers)
	{
		var pts = new Coordinate[numbers.length / 2 + 1];

		for(int step = 0; step < numbers.length / 2; step++)
		{
			var ptx = numbers[step * 2].doubleValue();
			var pty = numbers[step * 2 + 1].doubleValue();
			var coor = new Coordinate(ptx, pty);

			if(step == 0)
				pts[0] = pts[pts.length - 1] = coor;
			else
				pts[step] = coor;
		}

		return facGeo.createPolygon(pts);
	}

	/**
	 * 把图形转换成坐标点, 忽略最后一个点(闭合点)
	 * */
	public static BigDecimal[] toPoints(Geometry geometry)
	{
		var pts = geometry.getCoordinates();
		var ret = new BigDecimal[Math.max(pts.length * 2 - 2, 0)];
		if(ret.length == 0) return ret;

		for(int step = 0; step < pts.length - 1; step++)
		{
			var pt = pts[step];
			ret[step * 2] = new BigDecimal(pt.x);
			ret[step * 2 + 1] = new BigDecimal(pt.y);
		}

		return ret;
	}

	/**
	 * 将指定多边形移动指定距离
	 * */
	public static Geometry move(Geometry origin, double offsetX, double offsetY)
	{
		var ptsOrigin = origin.getCoordinates();
		var ptsNew = new Coordinate[ptsOrigin.length];
		for(var step = 0; step < ptsOrigin.length; step++)
		{
			var ptOrigin = ptsOrigin[step];
			ptsNew[step] = new Coordinate(
					ptOrigin.x + offsetX,
					ptOrigin.y + offsetY
			);
		}
		return facGeo.createPolygon(ptsNew);
	}
}
