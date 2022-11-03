package firok.tool.alloywrench.bean;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;

public record IntRect(int fromX, int fromY, int toX, int toY)
{
	/**
	 * 当前矩形是否完全在指定矩形内
	 * */
	public boolean in(IntRect rect)
	{
		return fromX >= rect.fromX && fromY >= rect.fromY && toX <= rect.toX && toY <= rect.toY;
	}

	/**
	 * 判断矩形是否重叠
	 * https://blog.csdn.net/lanchunhui/article/details/50547837
	 * */
	public boolean isOverlap(IntRect rect)
	{
		return !(
				( (fromX < rect.toX) || (toY > rect.fromY) ) ||
				( (rect.fromX < toX) || (rect.toY > fromY) )
				);
	}

	private static final GeometryFactory facGeo = JtsSpatialContext.GEO.getShapeFactory().getGeometryFactory();

	/**
	 * 转换成几何图形
	 * */
	public Geometry toGeometry()
	{
		var pts = new Coordinate[5];
		pts[0] = pts[4] = new Coordinate(fromX, fromY);
		pts[1] = new Coordinate(toX, fromY);
		pts[2] = new Coordinate(toX, toY);
		pts[3] = new Coordinate(fromX, toY);
		return facGeo.createPolygon(pts);
	}

//	/**
//	 * 计算交集图形
//	 * */
//	public Geometry intersection(IntRect other)
//	{
//		var geoThis = toGeometry();
//		var geoOther = other.toGeometry();
//		return geoThis.intersection(geoOther);
//	}
}
