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
		this.borderHeight = top.subtract(bottom);
	}
	public Border4(
			double top,
			double bottom,
			double left,
			double right,
			double imageWidth,
			double imageHeight
	)
	{
		this(
				BigDecimal.valueOf(top),
				BigDecimal.valueOf(bottom),
				BigDecimal.valueOf(left),
				BigDecimal.valueOf(right),
				imageWidth,
				imageHeight
		);
	}

	/**
	 * 像素横坐标 -> 四至横坐标
	 * */
	public BigDecimal xOf(double x)
	{
		return BigDecimal.valueOf(x / imageWidth).multiply(borderWidth).add(left);
	}
	/**
	 * 像素纵坐标 -> 四至纵坐标
	 * */
	public BigDecimal yOf(double y)
	{
		return BigDecimal.ONE.subtract(BigDecimal.valueOf(y / imageHeight)).multiply(borderHeight).add(bottom);
	}

	/**
	 * 根据像素坐标 计算一个新的四至范围
	 * */
	public Border4 cut(double fromX, double fromY, double toX, double toY)
	{
		return new Border4(
				yOf(fromY), yOf(toY), xOf(fromX), xOf(toX),
				toX - fromX,
				toY - fromY
		);
	}
	public Border4 cut(IntRect rect)
	{
		return new Border4(
				yOf(rect.fromY()), yOf(rect.toY()), xOf(rect.fromX()), xOf(rect.toX()),
				rect.toX() - rect.fromX(),
				rect.toY() - rect.fromY()
		);
	}

	@Override
	public String toString()
	{
		return "B4[%f,%f-%f,%f](%f,%f)".formatted(left,top,right,bottom, imageWidth, imageHeight);
	}
}
