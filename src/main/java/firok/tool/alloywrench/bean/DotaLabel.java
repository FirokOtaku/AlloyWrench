package firok.tool.alloywrench.bean;

/**
 * 代表一个 DOTA 格式数据标签
 */
public record DotaLabel(DecimalPoint pt1, DecimalPoint pt2,
                        DecimalPoint pt3, DecimalPoint pt4,
                        String catalog, boolean isDifficult,
                        int imageWidth, int imageHeight)
{
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
