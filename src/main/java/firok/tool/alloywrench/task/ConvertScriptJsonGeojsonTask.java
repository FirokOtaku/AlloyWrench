package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.Border4;
import firok.tool.alloywrench.bean.GeojsonData;
import firok.tool.alloywrench.bean.ScriptJsonData;

import java.math.BigDecimal;
import java.util.*;

/**
 * 把处理之后的 json 数据再转回 geojson
 * @deprecated 内部使用
 * */
@Deprecated
public class ConvertScriptJsonGeojsonTask
{
	public static GeojsonData convert(
			ScriptJsonData sjd,
			Border4 border4, // top,bottom,left,right
			Map<String, String> params)
	{
		try
		{
			var gjd = new GeojsonData();
			gjd.setType("FeatureCollection");
			gjd.setName(UUID.randomUUID().toString());
			final var features = new ArrayList<GeojsonData.Feature>();
			gjd.setFeatures(features);

			final int countFeature = sjd.countEntry();
			final var labels = sjd.getLabels();
			final var masks = sjd.getMasks(); // 所有的multi-polygon

			for(var step = 0; step < countFeature; step++)
			{
				var feature = new GeojsonData.Feature();
				feature.setType("Feature");
				var properties = new GeojsonData.Feature.Properties();
				var label = String.valueOf(labels[step]);
				properties.setName(params.getOrDefault(label, label));
				feature.setProperties(properties);
				features.add(feature);

				var sizeMaskMultiPolygon = masks[step].length; // 当前multi-polygon的部件数量
				var geometry = new GeojsonData.Feature.Geometry();
				geometry.setType("MultiPolygon");
				var geoCoor = new BigDecimal[sizeMaskMultiPolygon][][][];
				geometry.setCoordinates(geoCoor);
				feature.setGeometry(geometry);

				for(int stepPartMultiPolygon = 0; stepPartMultiPolygon < sizeMaskMultiPolygon; stepPartMultiPolygon++) // 遍历部件
				{
					var partCoor = new BigDecimal[sizeMaskMultiPolygon][][];
					geoCoor[stepPartMultiPolygon] = partCoor;
					var part = masks[step][stepPartMultiPolygon]; // 取出当前multi-polygon的第 stepPartMultiPolygon 个部件

					var sizePt = part.length;
					partCoor[stepPartMultiPolygon] = new BigDecimal[sizePt + 1][];
					for(var stepPt = 0; stepPt < sizePt; stepPt++)
					{
						var pt = part[stepPt];
//						double ptX = pt[0], ptY = pt[1];
						var x = border4.xOf(pt[0]);
						var y = border4.yOf(pt[1]);
						var coordinates3 = new BigDecimal[] { x, y }; // fixme low 精度损失
						partCoor[stepPartMultiPolygon][stepPt] = coordinates3;
					}
					partCoor[stepPartMultiPolygon][sizePt] = partCoor[stepPartMultiPolygon][0];
				}
			}

			return gjd;
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行发生错误");
			return null;
		}
	}
}
