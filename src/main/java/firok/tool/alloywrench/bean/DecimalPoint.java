package firok.tool.alloywrench.bean;

import java.math.BigDecimal;
import java.util.Objects;

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
}
