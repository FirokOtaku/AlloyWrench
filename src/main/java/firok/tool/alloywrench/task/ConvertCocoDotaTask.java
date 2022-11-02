package firok.tool.alloywrench.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.DotaLabel;
import firok.topaz.Files;
import firok.topaz.Ref;
import firok.topaz.SimpleMultiThread;

import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 把 COCO 标签转成 DOTA 标签
 * */
public class ConvertCocoDotaTask
{
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public static void execute(String pathLabel, String pathLabelTarget)
	{
		final var fileLabel = new File(pathLabel);
		final var folderLabelTarget = new File(pathLabelTarget);
		try
		{
			Files.assertExist(fileLabel, true, "标签文件必须存在");
			Files.assertNoExist(folderLabelTarget, "目标标签目录已经存在");
			folderLabelTarget.mkdirs();

			var om = new ObjectMapper();
			om.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

			var jsonLabel = om.readTree(fileLabel) instanceof ObjectNode obj ? obj : null;
			if(jsonLabel == null)
			{
				System.out.println("无法读取标签文件, 可能是格式错误");
				return;
			}

			var refMapCategory = new Ref<Map<Integer, String>>(); // category id -> category name
			var refMapImage = new Ref<Map<Integer, String>>(); // image id -> filename

			var smtRead = new SimpleMultiThread(
					true,
					() -> {
						var arrCategory = jsonLabel.get("categories") instanceof ArrayNode arr ? arr : null;
						if(arrCategory == null || arrCategory.isEmpty())
							return;

						refMapCategory.entry = firok.topaz.Collections.mappingKeyValue(
								arrCategory,
								cate -> cate.get("id").asInt(),
								cate -> cate.get("name").asText()
						);
					},
					() -> {
						var arrImage = jsonLabel.get("images") instanceof ArrayNode arr ? arr : null;
						if(arrImage == null || arrImage.isEmpty())
							return;

						refMapImage.entry = firok.topaz.Collections.mappingKeyValue(
								arrImage,
								anno -> anno.get("id").asInt(),
								anno -> anno.get("file_name").asText()
						);
					}
			);
			smtRead.start();
			smtRead.waitEnd();
			smtRead.throwAnyException();

			if(refMapImage.entry == null || refMapImage.entry.isEmpty())
			{
				System.out.println("无法读取标注或标注为空");
				return;
			}
			if(refMapCategory.entry == null || refMapCategory.entry.isEmpty())
			{
				System.out.println("无法读取类型或类型为空");
				return;
			}
			var arrAnnotation = jsonLabel.get("annotations") instanceof ArrayNode arr ? arr : null;
			if(arrAnnotation == null || arrAnnotation.isEmpty())
			{
				System.out.println("无法读取标签或标签为空");
				return;
			}

			var mapOfs = new HashMap<Integer, PrintStream>(); // image id -> ofs
			for(var entry : refMapImage.entry.entrySet())
			{
				var imageId = entry.getKey();
				var imageFilename = entry.getValue();
				var fileLabelTarget = new File(
						folderLabelTarget,
						imageFilename.substring(0, imageFilename.lastIndexOf('.')) + ".txt"
				);
				var ofs = new PrintStream(fileLabelTarget, StandardCharsets.UTF_8);

				mapOfs.put(imageId, ofs);
			}

			var mapImageLabel = new HashMap<Integer, List<DotaLabel>>(); // image id -> label[]

			IntStream.range(0, arrAnnotation.size()).parallel().forEach(index -> {
				var anno = arrAnnotation.get(index) instanceof ObjectNode obj ? obj : null;
				if(anno == null) return;

				try
				{
					var arrSeg = (ArrayNode) anno.get("segmentation");
					var seg = (ArrayNode) arrSeg.get(0);
					var cateId = anno.get("category_id").asInt();
					var imgId = anno.get("image_id").asInt();

					List<DotaLabel> listLabel;
					synchronized (mapImageLabel)
					{
						listLabel = mapImageLabel.computeIfAbsent(imgId, k -> new ArrayList<>());
					}

					var ofs = mapOfs.get(imgId);
					if(ofs == null)
					{
						System.out.println("找不到输出文件, 跳过一个标签");
						return;
					}

					BigDecimal[] nums;
					synchronized (om)
					{
						nums = om.convertValue(seg, BigDecimal[].class);
					}
					var pt1 = new DecimalPoint(nums[0], nums[1]);
					var pt2 = new DecimalPoint(nums[2], nums[3]);
					var pt3 = new DecimalPoint(nums[4], nums[5]);
					var pt4 = new DecimalPoint(nums[6], nums[7]);

					var category = refMapCategory.entry.get(cateId);

					var label = new DotaLabel(pt1, pt2, pt3, pt4, category, false, 0, 0);
					synchronized (listLabel) { listLabel.add(label); }
				}
				catch (Exception any)
				{
					any.printStackTrace(System.err);
					System.out.println("读取标签发生错误");
				}
			});

			mapImageLabel.entrySet().stream().parallel().forEach(entry -> {
				var imgId = entry.getKey();
				var listLabel = entry.getValue();
				var ofs = mapOfs.get(imgId);
				if(ofs == null)
				{
					System.out.println("找不到输出文件, 跳过");
					return;
				}

				for(var label : listLabel)
				{
					ofs.println(label.toLabelText());
				}
			});

			mapOfs.values().forEach(ofs -> {
				ofs.flush();
				ofs.close();
			});

			System.out.println("一段微小的旅程结束了");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行过程发生错误");
		}
	}
}
