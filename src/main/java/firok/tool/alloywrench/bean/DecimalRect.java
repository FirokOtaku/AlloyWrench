package firok.tool.alloywrench.bean;

import java.math.BigDecimal;

public record DecimalRect(BigDecimal fromX, BigDecimal fromY, BigDecimal toX, BigDecimal toY)
{
	/**
	 * 当前矩形偏移某段距离
	 * */
	public DecimalRect subtract(BigDecimal offsetX, BigDecimal offsetY)
	{
		return new DecimalRect(
				fromX.subtract(offsetX),
				fromY.subtract(offsetY),
				toX.subtract(offsetX),
				toY.subtract(offsetY)
		);
	}

	/**
	 * 当前矩形是否完全在指定矩形内
	 * */
	public boolean in(DecimalRect rect)
	{
		return c(fromX, rect.fromX) >= 0 &&
		       c(fromY, rect.fromY) >= 0 &&
		       c(toX, rect.toX) <= 0 &&
		       c(toY, rect.toY) <= 0;
	}

	public boolean in(IntRect rect)
	{
		return fromX.doubleValue() >= rect.fromX() &&
		       fromY.doubleValue() >= rect.fromY() &&
		       toX.doubleValue() <= rect.toX() &&
		       toY.doubleValue() <= rect.toY();
	}

	/**
	 * 判断矩形是否重叠
	 * https://blog.csdn.net/lanchunhui/article/details/50547837
	 * */
	public boolean isOverlap(DecimalRect rect)
	{
		return !(
				( fromX.compareTo(rect.toX) < 0 || toY.compareTo(rect.fromY) > 0 ) ||
				( rect.fromX.compareTo(toX) < 0 || rect.toY.compareTo(fromY) > 0 )
		);
	}

	/**
	 * 计算跟指定矩形的叠盖部分
	 * @return 如果没有叠盖则返回 null
	 * */
	public DecimalRect overlapOf(DecimalRect rect)
	{
		if(!isOverlap(rect)) return null;

		var fromX = this.fromX;
		var fromY = this.fromY;
		var toX = this.toX;
		var toY = this.toY;

		if(fromX.compareTo(rect.fromX) < 0) fromX = rect.fromX;
		if(fromY.compareTo(rect.fromY) < 0) fromY = rect.fromY;
		if(toX.compareTo(rect.toX) > 0) toX = rect.toX;
		if(toY.compareTo(rect.toY) > 0) toY = rect.toY;

		return new DecimalRect(fromX, fromY, toX, toY);
	}

	public DecimalRect overlapOf(IntRect rect)
	{
		// fixme 偷个懒 反正也就那样吧
		var rectDecimal = new DecimalRect(
				new BigDecimal(rect.fromX()),
				new BigDecimal(rect.fromY()),
				new BigDecimal(rect.toX()),
				new BigDecimal(rect.toY())
		);
		return overlapOf(rectDecimal);
	}

	/**
	 * 闲的
	 * */
	private static int c(BigDecimal p1, BigDecimal p2)
	{
		return p1.compareTo(p2);
	}
}
