package firok.tool.alloywrench.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.LabelmePolygonLabel;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

import static firok.tool.alloywrench.task.ConvertDotaYoloTask.*;

public class LabelmeReader
{
	public static List<LabelmePolygonLabel> read(String pathJson) throws Exception
	{
		var ret = new ArrayList<LabelmePolygonLabel>();
		var om = new ObjectMapper();
		var json = om.readTree(new File(pathJson)) instanceof ObjectNode obj ? obj : null;
		if(json == null) throw new IllegalArgumentException("无法读取指定文件");

		var shapes = json.get("shapes") instanceof ArrayNode arr ? arr : null;
		if(shapes == null)
			throw new IllegalArgumentException("无法读取图形列表");
		var imageHeight = json.get("imageHeight") instanceof NumericNode num ? num.intValue() : -1;
		var imageWidth = json.get("imageWidth") instanceof NumericNode num ? num.intValue() : -1;
		if(imageHeight == -1 || imageWidth == -1)
			throw new IllegalArgumentException("无法读取图片长宽");

		System.out.println("读取图形数据");
		IntStream.range(0, shapes.size()).parallel().forEach(index -> {
			READ_DATA: {
				var shape = shapes.get(index) instanceof ObjectNode obj ? obj : null;
				if(shape == null) break READ_DATA;
				var label = shape.get("label") instanceof TextNode text ? text.textValue() : null;
				if(label == null) break READ_DATA;
				var points = shape.get("points") instanceof ArrayNode arr ? arr : null;
				if(points == null || points.size() != 4) break READ_DATA;
				for(var point : points)
					if(!(point instanceof ArrayNode arr && arr.size() == 2)) break READ_DATA;
				if (!(shape.get("shape_type") instanceof TextNode text) || !"polygon".equals(text.textValue())) break READ_DATA;

				var point1 = (ArrayNode) points.get(0);
				var point2 = (ArrayNode) points.get(1);
				var point3 = (ArrayNode) points.get(2);
				var point4 = (ArrayNode) points.get(3);
				var pt1 = new DecimalPoint(point1.get(0).asText(), point1.get(1).asText());
				var pt2 = new DecimalPoint(point2.get(0).asText(), point2.get(1).asText());
				var pt3 = new DecimalPoint(point3.get(0).asText(), point3.get(1).asText());
				var pt4 = new DecimalPoint(point4.get(0).asText(), point4.get(1).asText());
				var pts = List.of(pt1, pt2, pt3, pt4);
				var data = new LabelmePolygonLabel(label, pts, imageWidth, imageHeight);

				synchronized (ret)
				{
					ret.add(data);
				}

				return;
			}
			System.out.println("图形数据错误, 跳过");
		});

		return ret;
	}
}
