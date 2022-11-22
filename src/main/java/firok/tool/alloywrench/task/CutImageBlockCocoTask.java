package firok.tool.alloywrench.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.bean.IntRect;
import firok.tool.alloywrench.util.Geos;
import firok.tool.alloywrench.util.Images;
import firok.topaz.Files;
import firok.topaz.SimpleMultiThread;
import org.locationtech.jts.geom.Geometry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 把单一的 COCO 标签图按照矩形范围切割成小图 COCO 数据集
 * */
public class CutImageBlockCocoTask
{
	public static final int PIECE_X = 800;
	public static final int PIECE_Y = 800;
	public static final int OVERLYING_X = 150;
	public static final int OVERLYING_Y = 150;

	/**
	 * 裁剪出标签的最小面积
	 * */
	public static final int LIMIT_AREA_ANNOTATION = 16;

	/**
	 * 裁剪出标签图形点之间的最小距离
	 * */
	public static final int LIMIT_DISTANCE_COOR = 2;

	public static final boolean CHECK_FS = false;
	public static final boolean SHOULD_READ_IMAGE = true;
	public static final boolean SHOULD_WRITE_SUB_IMAGE = true;

	public static void execute(
			String pathLabel,
			String pathImageBase,
			String pathLabelTarget,
			String pathImageTarget
	)
	{
		final File fileLabel = new File(pathLabel),
				folderImage = new File(pathImageBase),
				fileLabelTarget = new File(pathLabelTarget),
				folderImageTarget = new File(pathImageTarget);
		try
		{
			if(CHECK_FS)
			{
				Files.assertExist(fileLabel, true, "标签文件不存在");
				Files.assertExist(folderImage, false, "图片目录不存在");
				Files.assertNoExist(fileLabelTarget, "目标标签已经存在");
				Files.assertNoExist(folderImageTarget, "目标图片目录已经存在");
			}
			folderImageTarget.mkdirs();
//
//			folderImageTarget.mkdirs();
//
			var om = new ObjectMapper();
			om.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
//			// 读取 COCO 数据
			var data = om.readValue(fileLabel, CocoData.class);
			var images = data.getImages();
			if(images == null || images.isEmpty())
				throw new IllegalArgumentException("COCO 数据文件未声明任何图片资源");
			var annotations = data.getAnnotations();
			if(annotations == null || annotations.isEmpty())
				throw new IllegalArgumentException("COCO 数据文件未声明任何标签");

//			var categories = data.getCategories();
//			if(categories == null || categories.isEmpty())
//				throw new IllegalArgumentException("COCO 数据文件未声明任何种类");

			var mapImageOrigin = new HashMap<Integer, BufferedImage>(); // image id -> image data
			var mapImageRanges = new HashMap<Integer, List<IntRect>>(); // image id -> cut range []
			var mapImageLabels = new HashMap<Integer, List<CocoData.Annotation>>(); // image id -> label []
			var mapLabelGeo = new HashMap<Integer, Geometry>(); // label id -> geometry

			var smt1 = new SimpleMultiThread(
					true,
					() -> { // 根据 COCO 数据集的描述计算每张图片的裁剪范围
						System.out.println("计算裁剪范围");
						for (var img : images)
						{
							var ranges = Images.calcCutRanges(
									img.getWidth(),
									img.getHeight(),
									PIECE_X,
									PIECE_Y,
									OVERLYING_X,
									OVERLYING_Y
							);
							mapImageRanges.put(img.getId(), ranges);
						}
						System.out.println("裁剪范围计算完成");
					},
					() -> { // 把不同图片 id 对应的标签提取出来
						System.out.println("提取图片标签");
						var mapping = firok.topaz.Collections.mappingKeyMultiEntityList(
								annotations,
								CocoData.Annotation::getImageId
						);
						mapImageLabels.putAll(mapping);

						System.out.println("计算标签多边形");
						// 计算每个标签的多边形
						for(var label : annotations)
						{
							var id = label.getId();
							var seg = label.getSegmentation().get(0).toArray(new BigDecimal[0]);
							var geoLabel = Geos.toPolygon(seg);
							mapLabelGeo.put(id, geoLabel);
						}
					},
					() -> { // 加载图片数据
						System.out.println("读取图片");
						images.stream().parallel().forEach(img -> {
							var filename = img.getFilename();
							var fileImage = new File(folderImage, filename);
							System.out.println("加载图片: " + filename);

							if(!fileImage.exists())
								throw new RuntimeException("找不到图片文件: " + filename);

							BufferedImage image;
							if(SHOULD_READ_IMAGE)
							{
								try
								{
									image = ImageIO.read(fileImage);
								}
								catch (Exception any)
								{
									throw new RuntimeException("无法读取图片数据: " + filename, any);
								}
							}
							else
							{
								image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
							}

							synchronized (mapImageOrigin)
							{
								mapImageOrigin.put(img.getId(), image);
							}
						});
						System.out.println("完成所有图片加载");
					}
			);
			smt1.start();
			smt1.waitEnd();
			smt1.throwAnyException();

			// 稍微检查一下
			System.out.println("检查图片数据");
			for(var img : images)
			{
				var id = img.getId();
				var image = mapImageOrigin.get(id);
				var filename = img.getFilename();
				if(image.getWidth() != img.getWidth())
					System.out.println("图片宽度不匹配, 这可能导致问题: " + filename);
				if(image.getHeight() != img.getHeight())
					System.out.println("图片高度不匹配, 这可能导致问题: " + filename);
			}
			System.out.println("图片检查完成");

			var IMAGE_ID = new AtomicInteger(1);
			var LABEL_ID = new AtomicInteger(1);

			var labelCuts = new ArrayList<CocoData.Annotation>(); // 计算叠盖之后的标签
			var imageCuts = new ArrayList<CocoData.Image>(); // 裁剪之后的图片

			// 现在开始照着数据裁剪图片
			System.out.println("开始裁剪图片");
			images.stream().parallel().forEach(img -> {
				var id = img.getId();
				var filename = img.getFilename();
				var filenameShort = filename.substring(0, filename.lastIndexOf('.'));

				var image = mapImageOrigin.get(id); // 原始图片数据
				var ranges = mapImageRanges.get(id); // 裁剪范围
				var labels = mapImageLabels.get(id); // 相关标签

				for(var range : ranges) // 遍历裁剪范围 里面就不需要再套一层多线程了
				{
					final var imageSubId = IMAGE_ID.getAndIncrement(); // 当前图片 id
					var imageSub = Images.cutImage(image, range); // 裁剪之后的子图片
					var filenameSub = "%s(%d,%d-%d,%d).png".formatted(
							filenameShort,
							range.fromX(), range.fromY(),
							range.toX(), range.toY()
					);
					var fileSub = new File(folderImageTarget, filenameSub);

					var imageNew = new CocoData.Image();
					imageNew.setId(imageSubId);
					imageNew.setFilename(filenameSub);
					imageNew.setWidth(range.toX() - range.fromX());
					imageNew.setHeight(range.toY() - range.fromY());
					synchronized (imageCuts)
					{
						imageCuts.add(imageNew);
					}

					if(SHOULD_WRITE_SUB_IMAGE)
					{
						try
						{
							ImageIO.write(imageSub, "png", fileSub);
							System.out.println("写入切分图片: " + filenameSub);
						} catch (Exception any)
						{
							throw new RuntimeException("写入切分图片时发生错误: " + filenameSub, any);
						}
					}

					var cutGeo = range.toGeometry();
					// 遍历标签 计算在这个范围内的标签
					LOOP_LABEL: for(var label : labels)
					{
						var labelId = label.getId();
						var labelGeo = mapLabelGeo.get(labelId);
						var intersectionGeo = cutGeo.intersection(labelGeo);
						if(intersectionGeo.isEmpty()) // 不相交 下一个
							continue LOOP_LABEL;
						if(intersectionGeo.getArea() < LIMIT_AREA_ANNOTATION) // 太小了 忽略
							continue LOOP_LABEL;
						CHECK_LINE: {
							var coors = intersectionGeo.getCoordinates();
							var coor = coors[0];
							for(int step = 1; step < coors.length; step++)
							{
								var coorNow = coors[step];
								if(coorNow.distance(coor) < LIMIT_DISTANCE_COOR) // 两个点离得太近 忽略
									continue LOOP_LABEL;
								coor = coorNow;
							}
						}

						final var annoNewId = LABEL_ID.getAndIncrement();

						var annoNew = new CocoData.Annotation();
						annoNew.setId(annoNewId);
						annoNew.setIsCrowd(label.getIsCrowd());
						annoNew.setCategoryId(label.getCategoryId());
						annoNew.setImageId(imageSubId);

						var area = intersectionGeo.getArea();
						annoNew.setArea(new BigDecimal(area));

						// 把重叠区域按照当前裁剪区域重新计算新坐标
						var intersectionOfCutGeo = Geos.move(intersectionGeo, -range.fromX(), -range.fromY());
						var segNew = new ArrayList<List<BigDecimal>>();
						segNew.add(Arrays.asList(Geos.toPoints(intersectionOfCutGeo)));
						annoNew.setSegmentation(segNew);

						// bbox 也得算
						double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
						for(var coor : intersectionOfCutGeo.getCoordinates())
						{
							if(coor.x > maxX) maxX = coor.x;
							if(coor.y > maxY) maxY = coor.y;
							if(coor.x < minX) minX = coor.x;
							if(coor.y < minY) minY = coor.y;
						}
						var bboxNew = new ArrayList<BigDecimal>();
						bboxNew.add(new BigDecimal(minX));
						bboxNew.add(new BigDecimal(minY));
						bboxNew.add(new BigDecimal(maxX - minX));
						bboxNew.add(new BigDecimal(maxY - minY));
						annoNew.setBbox(bboxNew);

						// 把移动之后的交集区域加到结果里面
						synchronized (labelCuts)
						{
							labelCuts.add(annoNew);
						}
					}
				}
			});

			// 把更新之后的数据写回去
			data.setAnnotations(labelCuts);
			data.setImages(imageCuts);
			// 写入磁盘
			System.out.println("写入切分标签文件");
			om.writeValue(fileLabelTarget, data);

			System.out.println("一段破碎的旅程结束了");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行过程出现错误");
		}
	}
}
