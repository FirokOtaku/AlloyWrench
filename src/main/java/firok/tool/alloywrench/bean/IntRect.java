package firok.tool.alloywrench.bean;

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
}
