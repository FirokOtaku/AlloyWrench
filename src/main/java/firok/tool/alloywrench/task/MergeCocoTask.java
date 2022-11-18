package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.Files;
import firok.topaz.TriConsumer;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MergeCocoTask
{
	public static void execute(
			String pathLabel1, String pathLabel2, String pathLabelOut
	)
	{
		var fileLabel1 = new File(pathLabel1);
		var fileLabel2 = new File(pathLabel2);
		var fileLabelOut = new File(pathLabelOut);
		final var om = Jsons.omDecimal();
		try
		{
			Files.assertExist(fileLabel1, true, "标签文件不存在");
			Files.assertExist(fileLabel2, true, "标签文件不存在");
//			Files.assertNoExist(fileLabelOut, "目标标签文件已经存在");

			var coco1 = om.readValue(fileLabel1, CocoData.class);
			var coco2 = om.readValue(fileLabel2, CocoData.class);

			var images1 = coco1.getImages();
			var images2 = coco2.getImages();
			var annotations1 = coco1.getAnnotations();
			var annotations2 = coco2.getAnnotations();
			var categories1 = coco1.getCategories();
			var categories2 = coco2.getCategories();
			for(List<?> list : new List[] { // 小小的检查一下数据
					images1, images2,
					annotations1, annotations2,
					categories1, categories2,
			})
			{
				if(firok.topaz.Collections.sizeOf(list) <= 0)
				{
					System.out.println("标签数据错误, 停止执行");
					return;
				}
			}

			// 合并图片
			System.out.println("合并图片数据");
			var imageId = new AtomicInteger(1);
			var cocoImages = new ArrayList<CocoData.Image>();
			BiConsumer<List<CocoData.Image>, Map<Integer, Integer>> mergeImage = (images, map) -> {
				for(var imageOld : images)
				{
					var idOld = imageOld.getId();
					var idNew = imageId.getAndIncrement();
					map.put(idOld, idNew);

					var imageNew = new CocoData.Image();
					imageNew.setId(idNew);
					imageNew.setFilename(imageOld.getFilename());
					imageNew.setWidth(imageOld.getWidth());
					imageNew.setHeight(imageOld.getHeight());
					cocoImages.add(imageNew);
				}
			};
			var mapImage1 = new HashMap<Integer, Integer>();
			var mapImage2 = new HashMap<Integer, Integer>();
			mergeImage.accept(images1, mapImage1);
			mergeImage.accept(images2, mapImage2);

			// 合并种类
			System.out.println("合并类型数据");
			var mapCategoryNew = new HashMap<String, Integer>(); // label string -> category id
			var cocoCategories = new ArrayList<CocoData.Category>();
			Function<CocoData.Category, String> combineKey = (category) -> category.getName() + "##" + category.getSuperCategory();
			BiConsumer<List<CocoData.Category>, Map<Integer, Integer>> mergeCategory = (categories, map) -> {
				for(var categoryOld : categories)
				{
					var idOld = categoryOld.getId();
					var combination = combineKey.apply(categoryOld);

					var categoryIdNew = mapCategoryNew.get(combination);
					if(categoryIdNew != null)
					{
						map.put(idOld, categoryIdNew);
						continue;
					}

					var idNew = mapCategoryNew.size() + 1;
					mapCategoryNew.put(combination, idNew);
					map.put(idOld, idNew);

					var categoryNew = new CocoData.Category();
					categoryNew.setId(idNew);
					categoryNew.setName(categoryOld.getName());
					categoryNew.setSuperCategory(categoryOld.getSuperCategory());
					cocoCategories.add(categoryNew);
				}
			};
			var mapCategory1 = new HashMap<Integer, Integer>();
			var mapCategory2 = new HashMap<Integer, Integer>();
			mergeCategory.accept(categories1, mapCategory1);
			mergeCategory.accept(categories2, mapCategory2);

			// 合并标签数据
			// 这里需要用到刚才合并的图片和种类数据
			System.out.println("合并标签数据");
			var cocoAnnotations = new ArrayList<CocoData.Annotation>();
			var annotationId = new AtomicInteger(1);
			TriConsumer<
					List<CocoData.Annotation>,
					Map<Integer, Integer>,
					Map<Integer, Integer>
			> mergeAnnotation = (annotations, mapImage, mapCategory) -> {
				for(var annotationOld : annotations)
				{
//					var idOld = annotationOld.getId(); // 老id没啥用
					var idNew = annotationId.getAndIncrement();

					var imageIdOld = annotationOld.getImageId();
					var categoryIdOld = annotationOld.getCategoryId();

					var imageIdNew = mapImage.get(imageIdOld);
					var categoryIdNew = mapCategory.get(categoryIdOld);

					var annotationNew = new CocoData.Annotation();
					annotationNew.setId(idNew);
					annotationNew.setArea(annotationOld.getArea());
					annotationNew.setSegmentation(annotationOld.getSegmentation());
					annotationNew.setBbox(annotationOld.getBbox());
					annotationNew.setCategoryId(categoryIdNew);
					annotationNew.setImageId(imageIdNew);

					cocoAnnotations.add(annotationNew);
				}
			};
			mergeAnnotation.accept(annotations1, mapImage1, mapCategory1);
			mergeAnnotation.accept(annotations2, mapImage2, mapCategory2);

			// 万事大吉

			var coco = new CocoData();
			coco.setAnnotations(cocoAnnotations);
			coco.setImages(cocoImages);
			coco.setCategories(cocoCategories);

			System.out.println("写入合并标签");
			om.writeValue(fileLabelOut, coco);
			System.out.println("一段融合旅程结束了");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行发生错误");
		}
	}
}
