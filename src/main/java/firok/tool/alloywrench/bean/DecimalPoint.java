package firok.tool.alloywrench.bean;

import java.math.BigDecimal;
import java.util.Objects;

import static firok.tool.alloywrench.task.ConvertDotaYoloTask._max;
import static firok.tool.alloywrench.task.ConvertDotaYoloTask._min;

public final class DecimalPoint
{
	public BigDecimal x, y;
	public DecimalPoint()
	{
		this(BigDecimal.ZERO, BigDecimal.ZERO);
	}
	public DecimalPoint(BigDecimal x, BigDecimal y)
	{
		this.x = Objects.requireNonNull(x);
		this.y = Objects.requireNonNull(y);
	}
	public DecimalPoint(String x, String y)
	{
		this.x = new BigDecimal(x);
		this.y = new BigDecimal(y);
	}

	/**
	 * 因为这个类不是 record, 所以返回的一个新的实例
	 * */
	public DecimalPoint subtractOf(BigDecimal offsetX, BigDecimal offsetY)
	{
		return new DecimalPoint(
				this.x.subtract(offsetX),
				this.y.subtract(offsetY)
		);
	}

	public void maxOf(BigDecimal x, BigDecimal y)
	{
		if(this.x.compareTo(x) < 0)
			this.x = x;
		if(this.y.compareTo(y) < 0)
			this.y = y;

	}
	public void minOf(BigDecimal x, BigDecimal y)
	{
		if(this.x.compareTo(x) > 0)
			this.x = x;
		if(this.y.compareTo(y) > 0)
			this.y = y;
	}

	public static void range(DecimalPoint min, DecimalPoint max, DecimalPoint... pts)
	{
		if(pts == null || pts.length <= 0) return;
		for(var pt : pts)
		{
			min.minOf(pt.x, pt.y);
			max.maxOf(pt.x, pt.y);
		}
	}

	/**
	 * 根据给定点 创建一个矩形
	 * */
	public static DecimalRect toRect(DecimalPoint... pts)
	{
		var min = new DecimalPoint(_min, _min);
		var max = new DecimalPoint(_max, _max);
		range(min, max, pts);
		return new DecimalRect(min.x, min.y, max.x, max.y);
	}
}
