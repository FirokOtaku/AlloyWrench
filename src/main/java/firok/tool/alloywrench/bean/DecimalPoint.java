package firok.tool.alloywrench.bean;

import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.Objects;

public final class DecimalPoint extends Point2D
{
	public BigDecimal x, y;
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

	public String getDecimalX()
	{
		return x.toString();
	}

	public String getDecimalY()
	{
		return y.toString();
	}

	@Override
	public double getX()
	{
		return x.doubleValue();
	}

	@Override
	public double getY()
	{
		return y.doubleValue();
	}

	@Override
	public void setLocation(double x, double y)
	{
		this.x = BigDecimal.valueOf(x);
		this.y = BigDecimal.valueOf(y);
	}
}
