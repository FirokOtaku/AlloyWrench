package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.GeojsonData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.general.Collections;
import firok.topaz.resource.Files;
import firok.topaz.thread.Ref;
import firok.topaz.thread.SimpleMultiThread;
import org.locationtech.jts.geom.*;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 传入的 geojson 中所有图形必须是 MultiPolygon
 * @deprecated 内部使用
 * */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class ConvertMultiPolygonGeojsonCocoTask
{
	private static final JtsShapeFactory facShape = JtsSpatialContext.GEO.getShapeFactory();
	private static final GeometryFactory facGeo = facShape.getGeometryFactory();

	public static void execute(
			String pathGeojsonInput,
			String pathCocoOutput,
			String pathImage,
			String[] rawBorder4 // 四至 top,bottom,left,right
	) throws Exception
	{
		var fileGeojsonInput = new File(pathGeojsonInput);
		var fileCocoOutput = new File(pathCocoOutput);
		var fileImage = new File(pathImage);
		final BigDecimal top, bottom, left, right;
		final BigDecimal borderWidth, borderHeight;
		final var om = Jsons.omDecimal();
		Files.assertExist(fileGeojsonInput, true, "geojson 文件不存在");
		Files.assertNoExist(fileCocoOutput, "目标标签文件已经存在");
		Files.assertExist(fileImage, true, "图片文件不存在");
		if(rawBorder4 == null || rawBorder4.length != 4)
			throw new IllegalArgumentException("四至错误");
		top = new BigDecimal(rawBorder4[0]);
		bottom = new BigDecimal(rawBorder4[1]);
		left = new BigDecimal(rawBorder4[2]);
		right = new BigDecimal(rawBorder4[3]);
		borderWidth = right.subtract(left);
		borderHeight = top.subtract(bottom);

		var refImage = new Ref<BufferedImage>();
		var refGeojson = new Ref<GeojsonData>();

		var smt = new SimpleMultiThread(
				true,
				() -> {
					System.out.println("读取图片数据");
					refImage.entry = ImageIO.read(fileImage);
					System.out.println("图片数据读取完成");
				},
				() -> {
					System.out.println("读取 geojson 数据");
					refGeojson.entry = om.readValue(fileGeojsonInput, GeojsonData.class);
					System.out.println("geojson 数据读取完成");
				}
		);
		smt.start();
		smt.waitEnd();
		smt.throwAnyException();

		final int imageHeight = refImage.entry.getHeight(), imageWidth = refImage.entry.getWidth();
		final var iw = new BigDecimal(imageWidth);
		final var ih = new BigDecimal(imageHeight);
		final var factorX = iw.divide(borderWidth, RoundingMode.HALF_UP);
		final var factorY = ih.divide(borderHeight, RoundingMode.HALF_UP);

		var features = refGeojson.entry.getFeatures();
		if(Collections.isEmpty(features))
		{
			System.out.println("图形列表为空");
			return;
		}

		var images = new ArrayList<CocoData.Image>();
		var categories = new ArrayList<CocoData.Category>();
		var annotations = new ArrayList<CocoData.Annotation>();

		var image = new CocoData.Image();
		image.setId(1);
		image.setWidth(imageWidth);
		image.setHeight(imageHeight);
		image.setFilename(fileImage.getName());
		images.add(image);

		var _annoId = new AtomicInteger(1);
		var mapCategory = new HashMap<String, Integer>(); // feature name -> category id

		features.stream().parallel().forEach(feature -> {
			var type = feature.getType();
			if(!"Feature".equals(type))
			{
				System.out.println("跳过不支持的类型: " + type);
				return;
			}
			var name = Optional.ofNullable(feature.getProperties())
					.map(GeojsonData.Feature.Properties::getName)
					.orElse(null);
			if(name == null)
			{
				System.out.println("跳过名称为空标签");
				return;
			}
			// 处理 segment
			var segmentations = new ArrayList<List<BigDecimal>>();
			var geometry = feature.getGeometry();
			var polygons = geometry.getCoordinates();
			final DecimalPoint min = DecimalPoint.startMin(), max = DecimalPoint.startMax();
			// 理论上后面要对多个多边形的情况做专门处理
			// 目前手里的数据只有单个多边形的情况 先不管了

			var polygon1 = polygons[0]; // 获取当前第一个多边形
			LinearRing ringMain = null;
			var ringHoles = new LinearRing[polygon1.length - 1]; // 准备用来打洞

			for (int stepShape = 0; stepShape < polygon1.length; stepShape++)
			{
				BigDecimal[][] shape = polygon1[stepShape];
				var coordinates = new Coordinate[shape.length - 1]; // 这个是用来计算面积的
				// 遍历这个多边形每一个点
				// 跳过最后一个闭合点
				var segmentation = new ArrayList<BigDecimal>();
				for (int stepPoint = 0; stepPoint < shape.length - 1; stepPoint++)
				{
					BigDecimal[] point = shape[stepPoint];
					if (point == null || point.length != 2)
					{
						System.out.println("多边形存在错误点数据");
						return;
					}
					// 根据四至信息 把坐标点数据转换为像素数据
					// 四至坐标
					var geoX = point[0];
					var geoY = point[1];
					// 像素坐标
					// (geoX - left) * imageWidth / borderWidth
					// (geoY - bottom) * imageHeight / borderHeight
					var pixelX = geoX.subtract(left).multiply(factorX);
					var pixelY = ih.subtract(
							geoY.subtract(bottom).multiply(factorY)
					);

					segmentation.add(pixelX);
					segmentation.add(pixelY);
					DecimalPoint.range(min, max, new DecimalPoint(pixelX, pixelY));
					coordinates[stepPoint] = new Coordinate(pixelX.doubleValue(), pixelY.doubleValue());
				}
				segmentations.add(segmentation);

				coordinates[coordinates.length - 1] = coordinates[0]; // 完成多边形闭合
				var ls = facGeo.createLinearRing(coordinates); // 创建一条曲线
				if(stepShape == 0)
				{
					ringMain = ls;
				}
				else
				{
					ringHoles[stepShape - 1] = ls;
				}
			}

			// info 这个地方计算出来的面积是像素面积
			if(ringMain == null)
			{
				System.out.println("曲线为空");
				return;
			}
			var polygonArea = facGeo.createPolygon(ringMain, ringHoles);
			var area = BigDecimal.valueOf(polygonArea.getArea());


			var bbox = new ArrayList<BigDecimal>();
			bbox.add(min.x);
			bbox.add(min.y);
			bbox.add(max.x);
			bbox.add(max.y);

			var annotation = new CocoData.Annotation();
			annotation.setImageId(1);
			var annoId = _annoId.getAndIncrement();
			annotation.setId(annoId);
			annotation.setSegmentation(segmentations);
			annotation.setBbox(bbox);
			annotation.setArea(area);
			annotation.setIsCrowd(0);

			synchronized (mapCategory)
			{
				var categoryId = mapCategory.get(name);
				if(categoryId == null)
				{
					var category = new CocoData.Category();
					categoryId = mapCategory.size() + 1;
					category.setId(categoryId);
					category.setName(name);
					category.setSuperCategory(name);
					categories.add(category);
					mapCategory.put(name, categoryId);
				}
				annotation.setCategoryId(categoryId);
			}

			synchronized (annotations)
			{
				annotations.add(annotation);
			}
		});

		System.out.println("写入 COCO 标签数据");
		var coco = new CocoData();
		coco.setImages(images);
		coco.setCategories(categories);
		coco.setAnnotations(annotations);
		om.writeValue(fileCocoOutput, coco);
		System.out.println("一段牵线的旅程结束了");
	}
}
