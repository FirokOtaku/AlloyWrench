package firok.tool.alloywrench.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import firok.topaz.Files;
import firok.topaz.Maths;
import firok.topaz.SimpleMultiThread;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 切分单个大图片
 * */
public class CutImageBlockByCocoInstanceTask
{
	public static final int OVER_X = 50, OVER_Y = 50;

	final static class AnnotationRecord
	{
		@JsonProperty("id")
		int id;
		@JsonProperty("image_id")
		int imageId;
		@JsonProperty("bbox")
		BigDecimal[] bbox;
		@JsonProperty("segmentation")
		BigDecimal[][] segmentation;
		@JsonProperty("category_id")
		int categoryId;
		@JsonProperty("area")
		BigDecimal area;
		@JsonProperty("iscrowd")
		int isCrowd;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void execute(String _pathImageBase, String pathLabel, String pathTargetImage, String pathTargetLabel)
	{
		final var pathImageBase = Paths.get(_pathImageBase).normalize();
		final var fileLabel = new File(pathLabel);
		final var folderTargetImage = new File(pathTargetImage);
		final var fileTargetLabel = new File(pathTargetLabel);

		try
		{
			Files.assertExist(fileLabel, true, "标签文件不存在 [%s]");
			Files.assertNoExist(folderTargetImage, "目标图片目录已存在 [%s]");
			Files.assertNoExist(fileTargetLabel, "目标标签文件已存在 [%s]");

			folderTargetImage.mkdirs();
			fileTargetLabel.createNewFile();
		}
		catch (Exception any)
		{
			System.out.println(any.getMessage());
			return;
		}

		final var om = new ObjectMapper();
		om.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

		try
		{
			var jsonLabel = om.readTree(fileLabel) instanceof ObjectNode obj ? obj : null;
			if(jsonLabel == null)
			{
				System.out.println("标签文件格式错误");
				return;
			}

			// 根据标签加载相关数据
			record ImageRecord(
					int id,
					int width,
					int height,
					String filename,
					BufferedImage data
			) {}
			var mapImage = new HashMap<Integer, ImageRecord>(); // image id -> record
			var mapAnnotation = new HashMap<Integer, AnnotationRecord>(); // record id -> record

			var smtInit = new SimpleMultiThread(
					true,
					// 加载图片
					() -> {
						System.out.println("开始加载图片数据");
						var images = jsonLabel.get("images") instanceof ArrayNode arr ? arr : null;
						for(var step = 0; step < images.size(); step++)
						{
							var image = images.get(step) instanceof ObjectNode obj ? obj : null;
							if(image == null)
							{
								System.out.println("图片数据格式错误, 不为 JSON 对象");
								continue;
							}

							var height = image.get("height") instanceof NumericNode num ? num.intValue() : -1;
							var width = image.get("width") instanceof NumericNode num ? num.intValue() : -1;
							var id = image.get("id") instanceof NumericNode num ? num.intValue() : -1;
							var filename = image.get("file_name") instanceof TextNode text ? text.asText() : null;
							if(height == -1 || width == -1 || id == -1 || filename == null)
							{
								System.out.println("图片数据格式错误, 缺少项");
								continue;
							}

							// 根据数据加载图片
							var pathImage = pathImageBase.resolve(filename);
							var fileImage = pathImage.toFile();
							BufferedImage data;
							try
							{
								data = ImageIO.read(fileImage);
							}
							catch (Exception any)
							{
								any.printStackTrace(System.err);
								System.out.println("无法读取图片, 跳过");
								continue;
							}

							if(data.getHeight() != height)
								System.out.printf("警告, 图片高度不匹配 实际 [%d] 配置 [%d]\n", data.getHeight(), height);
							if(data.getWidth() != width)
								System.out.printf("警告, 图片宽度不匹配 实际 [%d] 配置 [%d]\n", data.getWidth(), width);

							System.out.printf("图片读取完成 [%s] [%d × %d]\n", filename, width, height);
							mapImage.put(
									id,
									new ImageRecord(id, width, height, filename, data)
							);
						}
						System.out.println("完成加载图片数据");
					},

					// 处理 COCO 标签
					() -> {
						System.out.println("开始加载 COCO 标签数据");
						var annotations = jsonLabel.get("annotations") instanceof ArrayNode arr ? arr : null;
						if(annotations == null)
						{
							System.out.println("格式错误, 找不到标记");
							return;
						}

						final var size = annotations.size();
						IntStream.range(0, size).parallel().forEach(num -> {
							System.out.printf("加载标签 [%d / %d]%n", num, size);
							var annotation = annotations.get(num) instanceof ObjectNode obj ? obj : null;
							if(annotation == null)
							{
								System.out.println("跳过一个格式错误标签");
								return;
							}

							AnnotationRecord annoRecord;
							synchronized (om) { annoRecord = om.convertValue(annotation, AnnotationRecord.class); }
							System.out.println("加载一个实体标签: " + annoRecord.id);

							if(annoRecord.isCrowd == 1)
							{
								System.out.println("跳过一个集群标签");
								return;
							}

							mapAnnotation.put(annoRecord.id, annoRecord);
						});

						System.out.println("完成加载 COCO 标签数据");
					}
			);

			smtInit.start();
			smtInit.waitEnd();
			smtInit.throwAnyException();

			var arrImageNew = om.createArrayNode();
			var arrAnnoNew = om.createArrayNode();

			System.out.println("根据 COCO 标签切分图片");
			var atomicImageOrder = new AtomicInteger(0);
			mapAnnotation.entrySet().stream().parallel().map(Map.Entry::getValue).forEach(anno -> {
				var imageRecord = mapImage.get(anno.imageId);

				var imageWidth = new BigDecimal(imageRecord.width);
				var imageHeight = new BigDecimal(imageRecord.height);

				// 根据图片长宽 计算最后切分范围
				var originX = anno.bbox[0];
				var originY = anno.bbox[1];
				var originWidth = anno.bbox[2];
				var originHeight = anno.bbox[3];
				var pieceWidth = originWidth.intValue();
				var pieceHeight = originHeight.intValue();

				var imageSub = new BufferedImage(
						pieceWidth,
						pieceHeight,
						BufferedImage.TYPE_INT_RGB
				);
				var gra = imageSub.getGraphics();
				gra.drawImage(
						imageRecord.data,
						originX.negate().intValue(),
						originY.negate().intValue(),
						(img, infoflags, x, y, width, height) -> false
				);
				gra.dispose();

				var filenameSub = "%d-%d-%s-%s-%d-%d.png".formatted(
						anno.id,
						anno.imageId,
						originX,
						originY,
						pieceWidth,
						pieceHeight
				); // id imageId x y width height
				var fileSub = new File(folderTargetImage, filenameSub);

				try // 写入数据
				{
					ImageIO.write(
							imageSub,
							"png",
							fileSub
					);
				}
				catch (Exception any)
				{
					any.printStackTrace(System.err);
					System.out.println("写入子图片时发生错误");
					return;
				}

				// 创建新的标签
				var imageOrder = atomicImageOrder.incrementAndGet();

				ObjectNode objImageNew;
				synchronized (om) { objImageNew = om.createObjectNode(); }
				objImageNew.put("width", pieceWidth);
				objImageNew.put("height", pieceHeight);
				objImageNew.put("id", imageOrder);
				objImageNew.put("file_name", filenameSub);
				synchronized (arrImageNew) { arrImageNew.add(objImageNew); }

				var annoNew = new AnnotationRecord();
				annoNew.id = anno.id;
				annoNew.imageId = imageOrder;
				annoNew.isCrowd = anno.isCrowd;
				annoNew.area = new BigDecimal(imageSub.getWidth() * imageSub.getHeight());
				annoNew.bbox = new BigDecimal[] {
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						originWidth,
						originHeight
				};
				annoNew.segmentation = new BigDecimal[anno.segmentation.length][];
				for(var stepOut = 0; stepOut < anno.segmentation.length; stepOut++)
				{
					annoNew.segmentation[stepOut] = new BigDecimal[anno.segmentation[stepOut].length];
					// 把所有坐标减去起始点坐标
					for(var stepIn = 0; stepIn < anno.segmentation[stepOut].length / 2; stepIn++)
					{
						var segX = anno.segmentation[stepOut][stepIn * 2];
						var segY = anno.segmentation[stepOut][stepIn * 2 + 1];
						annoNew.segmentation[stepOut][stepIn * 2] = Maths.range(
								segX.subtract(originX),
								BigDecimal.ZERO,
								imageWidth
						);
						annoNew.segmentation[stepOut][stepIn * 2 + 1] = Maths.range(
								segY.subtract(originY),
								BigDecimal.ZERO,
								imageHeight
						);
					}
				}
				annoNew.categoryId = anno.categoryId;

				JsonNode jsonAnnoNew;
				synchronized (om) { jsonAnnoNew = om.valueToTree(annoNew); }
				synchronized (arrAnnoNew) { arrAnnoNew.add(jsonAnnoNew); }

				System.out.printf("完成切分 [%s]%n", filenameSub);
			});

			System.out.println("切分完成 写入新的标签数据");
			jsonLabel.set("annotations", arrAnnoNew);
			jsonLabel.set("images", arrImageNew);
			om.writeValue(fileTargetLabel, jsonLabel);

			System.out.println("一段惊险的旅程结束了");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("处理数据时发生错误");
		}
	}
}
