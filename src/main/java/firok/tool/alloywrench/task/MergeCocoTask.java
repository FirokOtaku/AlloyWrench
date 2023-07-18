package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.resource.Files;
import firok.topaz.function.TriConsumer;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static firok.topaz.general.Collections.isEmpty;

public class MergeCocoTask
{
	// 有的是优化空间 但是懒得整
	public static void execute(
			String[] pathLabels,
			String pathLabelOut
	) throws Exception
	{
		final int group = pathLabels.length;
		var fileLabels = new File[group];
		var fileLabelOut = new File(pathLabelOut);
		final var om = Jsons.omDecimal();
		Map<Integer, Integer>[] mapImages = new HashMap[group];
		Map<Integer, Integer>[] mapCategories = new HashMap[group];
		var cocos = new CocoData[group];
		for(int step = 0; step < group; step++)
		{
			var fileLabel = fileLabels[step] = new File(pathLabels[step]);
			Files.assertExist(fileLabel, true, "标签文件不存在");
			var coco = cocos[step] = om.readValue(fileLabel, CocoData.class);
			// 小小的检查一下数据
			if(isEmpty(coco.getImages()) || isEmpty(coco.getAnnotations()) || isEmpty(coco.getCategories()))
			{
				System.out.println("标签数据错误, 停止执行");
				return;
			}
			mapImages[step] = new HashMap<>();
			mapCategories[step] = new HashMap<>();
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
		for(int step = 0; step < group; step++)
		{
			mergeImage.accept(cocos[step].getImages(), mapImages[step]);
		}

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
		for(int step = 0; step < group; step++)
		{
			mergeCategory.accept(cocos[step].getCategories(), mapCategories[step]);
		}

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
		for(int step = 0; step < group; step++)
		{
			var coco = cocos[step];
			var annotations = coco.getAnnotations();
			var mapImage = mapImages[step];
			var mapCategory = mapCategories[step];
			mergeAnnotation.accept(annotations, mapImage, mapCategory);
		}

		// 万事大吉

		var coco = new CocoData();
		coco.setAnnotations(cocoAnnotations);
		coco.setImages(cocoImages);
		coco.setCategories(cocoCategories);

		System.out.println("写入合并标签");
		om.writeValue(fileLabelOut, coco);
		System.out.println("一段融合旅程结束了");
	}
}
