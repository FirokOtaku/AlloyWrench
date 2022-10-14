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
}
