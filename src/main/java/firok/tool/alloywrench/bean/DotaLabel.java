package firok.tool.alloywrench.bean;

import java.math.BigDecimal;

/**
 * 代表一个 DOTA 格式数据标签
 */
public record DotaLabel(DecimalPoint pt1, DecimalPoint pt2,
                        DecimalPoint pt3, DecimalPoint pt4,
                        String catalog, boolean isDifficult,
                        int imageWidth, int imageHeight)
{
	public DotaLabel subtract(BigDecimal offsetX, BigDecimal offsetY)
	{
		return new DotaLabel(
				pt1.subtractOf(offsetX, offsetY),
				pt2.subtractOf(offsetX, offsetY),
				pt3.subtractOf(offsetX, offsetY),
				pt4.subtractOf(offsetX, offsetY),
				catalog, isDifficult,
				imageWidth, imageHeight
		);
	}
	public String toLabelText()
	{
		return "%s %s %s %s %s %s %s %s %s %d".formatted(
				pt1.x, pt1.y,
				pt2.x, pt2.y,
				pt3.x, pt3.y,
				pt4.x, pt4.y,
				catalog,
				isDifficult ? 1 : 0
		);
	}
}
