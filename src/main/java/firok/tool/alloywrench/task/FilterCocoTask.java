package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.Files;

import java.io.File;
import java.util.*;

import static firok.topaz.Collections.isEmpty;

public class FilterCocoTask
{
	@SuppressWarnings("DuplicatedCode")
	public static void execute(
			String pathCoco,
			String pathCocoOut,
			Set<Integer> setImageIdFilter,
			Set<Integer> setCategoryIdFilter,
			Set<Integer> setAnnotationIdFilter
	) throws Exception
	{
		var fileCoco = new File(pathCoco);
		var fileCocoOut = new File(pathCocoOut);
		var om = Jsons.omDecimal();
		Files.assertExist(fileCoco, true, "标签文件必须存在");
		Files.assertNoExist(fileCocoOut, "目标标签文件已经存在");
		if(isEmpty(setImageIdFilter) && isEmpty(setCategoryIdFilter) && isEmpty(setAnnotationIdFilter))
//				throw new IllegalArgumentException("过滤器全部为空 你再考虑一下");
			System.out.println("过滤器全部为空 我暂且蒙鼓");

		var old = om.readValue(fileCoco, CocoData.class);
		var images = new ArrayList<CocoData.Image>();
		var annotations = new ArrayList<CocoData.Annotation>();
		var categories = new ArrayList<CocoData.Category>();

		var categoriesOld = old.getCategories();
		if(isEmpty(categoriesOld))
			throw new IllegalArgumentException("类型不可为空");

		var imagesOld = old.getImages();
		if(isEmpty(imagesOld))
			throw new IllegalArgumentException("图片数据为空");

		var annotationsOld = old.getAnnotations();
		if(isEmpty(annotationsOld))
			throw new IllegalArgumentException("标记数据为空");

		System.out.println("过滤标签");
		int _annoId = 1;
		var setImageIdUsed = new HashSet<Integer>();
		var setCategoryIdUsed = new HashSet<Integer>();
		for(var annotationOld : annotationsOld)
		{
			if(annotationOld == null) continue;
			var annotationId = annotationOld.getId();
			var imageId = annotationOld.getImageId();
			var categoryId = annotationOld.getCategoryId();
			// info 全是黑名单策略
			if(setImageIdFilter != null && setImageIdFilter.contains(imageId)) continue;
			if(setCategoryIdFilter != null && setCategoryIdFilter.contains(categoryId)) continue;
			if(setAnnotationIdFilter != null && setAnnotationIdFilter.contains(annotationId)) continue;
			setImageIdUsed.add(imageId);
			setCategoryIdUsed.add(categoryId);
			annotations.add(annotationOld);

			annotationOld.setId(_annoId);
			_annoId++;
		}
		if(isEmpty(annotations))
		{
			System.out.println("啥标签都不剩了 你再想想");
			return;
		}

		System.out.println("过滤图片");
		int _imageId = 1;
		var mapImageId = new HashMap<Integer, Integer>();
		for(var imageOld : imagesOld)
		{
			var imageId = imageOld.getId();
			if(!setImageIdUsed.contains(imageId))
				continue;

			images.add(imageOld);

			imageOld.setId(_imageId);
			mapImageId.put(imageId, _imageId);
			_imageId++;
		}
		if(isEmpty(images))
		{
			System.out.println("啥图片都不剩了 你再想想");
			return;
		}

		System.out.println("过滤种类");
		int _categoryId = 1;
		var mapCategoryId = new HashMap<Integer, Integer>();
		for(var categoryOld : categoriesOld)
		{
			var categoryId = categoryOld.getId();
			if(!setCategoryIdUsed.contains(categoryId))
				continue;

			categories.add(categoryOld);

			categoryOld.setId(_categoryId);
			mapCategoryId.put(categoryId, _categoryId);
			_categoryId++;
		}
		if(isEmpty(categories))
		{
			System.out.println("啥类型都不剩了 你再想想");
			return;
		}

		System.out.println("重建映射");
		for(var anno : annotations)
		{
			var imageId = anno.getImageId();
			anno.setImageId(mapImageId.get(imageId));
			var categoryId = anno.getCategoryId();
			anno.setCategoryId(mapCategoryId.get(categoryId));
		}

		System.out.println("写入文件");
		var ret = new CocoData();
		ret.setImages(images);
		ret.setAnnotations(annotations);
		ret.setCategories(categories);
		om.writeValue(fileCocoOut, ret);
		System.out.println("一段细碎的旅程结束了");
	}
}
