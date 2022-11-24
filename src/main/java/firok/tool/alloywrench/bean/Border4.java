package firok.tool.alloywrench.bean;

import java.math.BigDecimal;

/**
 * 四至计算
 * */
public class Border4
{
	public final BigDecimal top, bottom, left, right;
	public final double imageWidth, imageHeight;
	public final BigDecimal borderWidth, borderHeight;

	public Border4(BigDecimal[] border4, int imageWidth, int imageHeight)
	{
		this(border4[0], border4[1], border4[2], border4[3], imageWidth, imageHeight);
	}
	public Border4(
			BigDecimal top,
			BigDecimal bottom,
			BigDecimal left,
			BigDecimal right,
			double imageWidth,
			double imageHeight
	)
	{
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.borderWidth = right.subtract(left);
		this.borderHeight = bottom.subtract(top);
	}

	public BigDecimal xOf(double x)
	{
		return BigDecimal.valueOf(x / imageWidth).multiply(borderWidth).add(left);
	}
	public BigDecimal yOf(double y)
	{
		return BigDecimal.valueOf(y / imageHeight).multiply(borderHeight).add(top);
	}

	public Border4 cut(double fromX, double fromY, double toX, double toY)
	{
		return new Border4(
				yOf(fromY), yOf(toY), xOf(fromX), xOf(toX),
				toX - fromX,
				toY - fromX
		);
	}
}
