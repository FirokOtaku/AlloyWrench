package firok.tool.alloywrench.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.LabelmeData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.Files;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static firok.tool.alloywrench.task.ConvertDotaYoloTask._max;
import static firok.tool.alloywrench.task.ConvertDotaYoloTask._min;

public class ConvertLabelmeCocoTask
{
	private static final GeometryFactory facGeo = JtsSpatialContext.GEO.getShapeFactory().getGeometryFactory();

	public static void execute(
			String pathLabelmeInput,
			String pathCocoOutput
	)
	{
		var fileLabelmeInput = new File(pathLabelmeInput);
		var fileCocoOutput = new File(pathCocoOutput);
		final var om = Jsons.omDecimal();
		try
		{
			Files.assertExist(fileLabelmeInput, true, "labelme 标签文件不存在");
			Files.assertNoExist(fileCocoOutput, "目标文件已经存在");

			System.out.println("读取 labelme 数据");
			var labelme = om.readValue(fileLabelmeInput, LabelmeData.class);
			final var shapes = labelme.getShapes();
			if(firok.topaz.Collections.sizeOf(shapes) <= 0)
			{
				System.out.println("无图形");
				return;
			}

			var images = new ArrayList<CocoData.Image>();
			var image = new CocoData.Image();
			image.setId(1);
			image.setFilename(labelme.getImagePath());
			image.setWidth(labelme.getImageWidth());
			image.setHeight(labelme.getImageHeight());
			images.add(image);

			var mapCategory = new HashMap<String, Integer>();
			var categories = new ArrayList<CocoData.Category>();
			for(var shape : shapes)
			{
				var label = shape.getLabel();
				if(mapCategory.containsKey(label))
					continue;
				var categoryIndex = mapCategory.size() + 1;
				mapCategory.put(label, categoryIndex);
				var category = new CocoData.Category();
				category.setId(categoryIndex);
				category.setSuperCategory(label);
				category.setName(label);
				categories.add(category);
			}

			var annotations = new ArrayList<CocoData.Annotation>();
			var annoId = new AtomicInteger(1);
			shapes.stream().parallel().forEach(shape -> {
				if(!"polygon".equals(shape.getShapeType()))
				{
					System.out.println("跳过一个非多边形");
					return;
				}

				var category = shape.getLabel();
				var categoryIndex = mapCategory.get(category);

				var segmentations = new ArrayList<List<BigDecimal>>();
				var segmentation = new ArrayList<BigDecimal>();
				var points = shape.getPoints();
				if(firok.topaz.Collections.sizeOf(points) <= 0)
				{
					System.out.println("多边形点数量不足");
					return;
				}
				var min = new DecimalPoint(_min, _min);
				var max = new DecimalPoint(_max, _max);
				var coors = new ArrayList<Coordinate>();
				for(var point : points)
				{
					if(point == null || point.length != 2)
					{
						System.out.println("跳过一个错误格式点");
						continue;
					}
					segmentation.add(point[0]);
					segmentation.add(point[1]);
					var dp = new DecimalPoint(point[0], point[1]);
					DecimalPoint.range(min, max, dp);
					var coor = new Coordinate(point[0].doubleValue(), point[1].doubleValue());
					coors.add(coor);
				}
				coors.add(coors.get(0));
				segmentations.add(segmentation);
				var bbox = new ArrayList<BigDecimal>();
				bbox.add(min.x);
				bbox.add(min.y);
				bbox.add(max.x);
				bbox.add(max.y);

				Polygon geo;
				synchronized (facGeo)
				{
					geo = facGeo.createPolygon(coors.toArray(new Coordinate[0]));
				}
				var area = geo.getArea();

				var annotation = new CocoData.Annotation();
				annotation.setId(annoId.getAndIncrement());
				annotation.setImageId(1);
				annotation.setCategoryId(categoryIndex);
				annotation.setIsCrowd(0);
				annotation.setArea(BigDecimal.valueOf(area));
				annotation.setBbox(bbox);
				annotation.setSegmentation(segmentations);

				synchronized (annotations)
				{
					annotations.add(annotation);
				}
			});

			var coco = new CocoData();
			coco.setImages(images);
			coco.setCategories(categories);
			coco.setAnnotations(annotations);

			System.out.println("写入 COCO 数据");
			om.writeValue(fileCocoOutput, coco);
			System.out.println("一段平凡的旅程结束了");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行发生错误");
		}
	}
}
