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
	 * 闲的
	 * */
	private static int c(BigDecimal p1, BigDecimal p2)
	{
		return p1.compareTo(p2);
	}
}
