package firok.tool.alloywrench.bean;

import static firok.tool.alloywrench.util.Numbers.subnum;

/**
 * 代表一个 YOLO 格式数据标签
 */
public record YoloLabel(int orderClass, DecimalPoint center, DecimalPoint range)
{
	public String toLabelText()
	{
		return "%d %s %s %s %s".formatted(
				orderClass,
				subnum(center.x),
				subnum(center.y),
				subnum(range.x),
				subnum(range.y)
		);
	}
}
