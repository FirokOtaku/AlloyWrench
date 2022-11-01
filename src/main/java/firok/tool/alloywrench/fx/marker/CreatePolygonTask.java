package firok.tool.alloywrench.fx.marker;

import javafx.scene.shape.Polygon;

/**
 * 创建多边形
 * */
public class CreatePolygonTask implements ITask
{
	public final Polygon polygon;
	public CreatePolygonTask(Polygon polygon)
	{
		this.polygon = polygon;
	}

	@Override
	public String getDisplayName()
	{
		return "创建多边形";
	}

	@Override
	public long getProgressTotal()
	{
		return -1;
	}

	@Override
	public long getProgressNow()
	{
		return -1;
	}
}
