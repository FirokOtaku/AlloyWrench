package firok.tool.alloywrench.bean;

import java.util.List;

/**
 * 一个 labelme 的多边形标签
 * */
public record LabelmePolygonLabel(
		String catalog,
		List<DecimalPoint> points,
		// 你问我支不支持这样写
		// 我当然是支持的
		int imageWidth,
		int imageHeight
)
{
}
