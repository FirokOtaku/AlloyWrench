package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.Files;

import java.io.File;
import java.util.*;

/**
 * 合并 coco 数据集中的种类
 * */
public class MergeCocoCategoryTask
{
	public static void execute(
			String pathLabelInput,
			String pathLabelOutput,
			String pathMapping
	)
	{
		var fileLabelInput = new File(pathLabelInput);
		var fileLabelOutput = new File(pathLabelOutput);
		var fileMapping = new File(pathMapping);
		try
		{
			Files.assertExist(fileLabelInput, true, "标签文件不存在");
			Files.assertExist(fileMapping, true, "映射配置不存在");
			Files.assertNoExist(fileLabelOutput, "目标文件已经存在");

			System.out.println("读取标签和映射数据");
			var om = Jsons.omDecimal();
			var label = om.readValue(fileLabelInput, CocoData.class);
			var mappingRaw = om.readTree(fileMapping) instanceof ObjectNode obj ? obj : null;
			if(mappingRaw == null) throw new IllegalArgumentException("映射文件读取出错");
			var mapName = new HashMap<String, String>();
			mappingRaw.fieldNames().forEachRemaining(name -> {
				var nameNew = mappingRaw.get(name) instanceof TextNode text ? text.textValue() : null;
				if(nameNew == null) return;
				mapName.put(name, nameNew);
			});

			var listCateOld = label.getCategories();
			var listCateNew = new ArrayList<CocoData.Category>();
			label.setCategories(listCateNew);

			System.out.println("重建映射");
			var mapCateId = new HashMap<Integer, Integer>(); // id old -> id new
			MERGE: {
				var mapNameCate = new HashMap<String, CocoData.Category>(); // cate name -> cate
				int cateId = 1;
				for(var categoryOld : listCateOld)
				{
					var idOld = categoryOld.getId();
					var nameOld = categoryOld.getName();
					var nameMapping = mapName.get(nameOld);
					var nameNew = nameMapping == null ? nameOld : nameMapping;
					if(mapNameCate.containsKey(nameNew)) continue;
					var cateNew = new CocoData.Category();
					cateNew.setId(cateId);
					cateNew.setName(nameNew);
					cateNew.setSuperCategory(nameNew);
					mapCateId.put(idOld, cateId);
					listCateNew.add(cateNew);
					mapNameCate.put(nameNew, cateNew);
					cateId++;
				}
			}

			System.out.println("写入映射");
			var annotations = label.getAnnotations();
			REMAP: {
				for(var annotation : annotations)
				{
					var idOld = annotation.getCategoryId();
					var idNew = mapCateId.get(idOld);
					annotation.setCategoryId(idNew);
				}
			}

			System.out.println("写入标签数据");
			om.writeValue(fileLabelOutput, label);
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("执行发生错误");
		}
	}
}
