package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.YoloLabel;
import firok.tool.alloywrench.util.LabelmeReader;

import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static firok.tool.alloywrench.task.ConvertDotaYoloTask.*;
import static firok.tool.alloywrench.task.ConvertDotaYoloTask.BD2;

public class ConvertLabelmeYoloTask
{
	/**
	 * @see ConvertDotaYoloTask
	 * */
	@SuppressWarnings("DuplicatedCode")
	public static void execute(String pathLabelme, String pathYolo, String pathMapping)
	{
		//noinspection DanglingJavadoc
		try
		{
			System.out.println("读取 labelme 标签文件");
			var listLabel = LabelmeReader.read(pathLabelme);
			if(listLabel.isEmpty()) throw new IllegalArgumentException("标签列表为空");

			var om = new ObjectMapper();

			System.out.println("创建映射");
			Map<String, Integer> mapping = new HashMap<>();
			listLabel.stream().parallel().forEach(labelLabelme -> {
				var catalog = labelLabelme.catalog();
				synchronized (mapping)
				{
					Integer tempIndex = mapping.get(catalog);
					if(tempIndex == null)
					{
						tempIndex = mapping.size();
						mapping.put(catalog, tempIndex);
					}
				}
			});

			var listYolo = listLabel.stream().parallel().map(labelme -> {
				var catalog = mapping.get(labelme.catalog());
				var pt1 = labelme.points().get(0);
				var pt2 = labelme.points().get(1);
				var pt3 = labelme.points().get(2);
				var pt4 = labelme.points().get(3);
				var decimalWidth = new BigDecimal(labelme.imageWidth());
				var decimalHeight = new BigDecimal(labelme.imageHeight());

				// 寻找标签边界点
				var min = new DecimalPoint(_min, _min);
				var max = new DecimalPoint(_max, _max);
				DecimalPoint.range(min, max, pt1, pt2, pt3, pt4);
				@SuppressWarnings("DuplicatedCode") var boxWidth = max.x.subtract(min.x);
				var boxHeight = max.y.subtract(min.y);
				var centerX = max.x.add(min.x).divide(BD2, 6, RoundingMode.HALF_UP).divide(decimalWidth, 6, RoundingMode.HALF_UP);
				var centerY = max.y.add(min.y).divide(BD2, 6, RoundingMode.HALF_UP).divide(decimalHeight, 6, RoundingMode.HALF_UP);
				var rangeX = boxWidth.divide(decimalWidth, 6, RoundingMode.HALF_UP);
				var rangeY = boxHeight.divide(decimalHeight, 6, RoundingMode.HALF_UP);

				var center = new DecimalPoint(centerX, centerY);
				var range = new DecimalPoint(rangeX, rangeY);

				return new YoloLabel(catalog, center, range);
			}).collect(Collectors.toList());

			System.out.println("创建映射文件");
			om.writeValue(new File(pathMapping), mapping);

			System.out.println("创建标签文件");
			var fileOut = new File(pathYolo);
			try(var out = new PrintStream(fileOut, StandardCharsets.UTF_8))
			{
				for(var labelYolo : listYolo)
					out.println(labelYolo.toLabelText());
				out.flush();
			}

			System.out.println("完成");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("发生错误");
		}
	}
}
