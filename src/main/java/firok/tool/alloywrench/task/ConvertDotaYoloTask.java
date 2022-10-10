package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.DotaLabel;
import firok.tool.alloywrench.bean.YoloLabel;
import firok.tool.alloywrench.util.DotaReader;
import firok.tool.alloywrench.util.Files;

import javax.imageio.ImageIO;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConvertDotaYoloTask
{
	public static void execute(String pathSourceFolder, String pathImageFolder, String pathMappingFile, String pathTargetFolder)
	{
		var folderSource = new File(pathSourceFolder);
		var folderImage = new File(pathImageFolder);
		var folderTarget = new File(pathTargetFolder);
		var fileMapping = new File(pathMappingFile);

		if(!folderSource.exists() || !folderSource.isDirectory())
		{
			System.out.println("源目录不存在");
			return;
		}
		if(!folderImage.exists() || !folderImage.isDirectory())
		{
			System.out.println("图片目录不存在");
			return;
		}
		if(!fileMapping.exists() || !fileMapping.isFile())
		{
			System.out.println("映射文件不存在");
			return;
		}
		if(folderTarget.exists() && folderTarget.isDirectory())
		{
			System.out.println("目标目录已经存在");
			return;
		}

		var files = folderSource.listFiles();
		if(files == null || files.length == 0)
		{
			System.out.println("源目录为空");
			return;
		}
		var resultMkdirs = folderTarget.mkdirs();
		if(!resultMkdirs)
		{
			System.out.println("创建目标目录失败");
			return;
		}

		// 读取映射文件
		var mapping = new HashMap<String, Integer>();
		var om = new ObjectMapper();
		try
		{
			var json = om.readTree(fileMapping) instanceof ObjectNode obj ? obj : null;
			if(json == null)
				throw new RuntimeException("映射文件内容不是有效的 JSON 对象");

			// 大概就是这么个内容格式
			// { "a": 1, "b": 2, ... }
			var iterName = json.fieldNames();
			while(iterName.hasNext())
			{
				var name = iterName.next();
				var value = json.get(name) instanceof NumericNode number ? number.intValue() : -1;
				if(value == -1)
					throw new RuntimeException("映射文件包含错误的映射 [%s]".formatted(name));
				mapping.put(name, value);
			}
		}
		catch (Exception any)
		{
			System.out.printf("读取映射文件失败 [%s]\n", any.getMessage());
		}

		Arrays.stream(files).parallel().forEach(fileIn -> {
			// 读取标签文本文件
			String raw;
			try
			{
				raw = Files.read(fileIn);
			}
			catch (IOException any)
			{
				System.out.printf("读取文件发生错误, 跳过文件 [%s]\n", fileIn.getAbsolutePath());
				return;
			}

			// 读取图片元信息
			var filename = fileIn.getName();
			var pictureId = filename.substring(0, filename.indexOf('.'));
			var fileImage = new File(folderImage, pictureId + ".png");
			if(!fileImage.exists() || !fileImage.isFile())
			{
				System.out.printf("找不到相应图片文件, 跳过文件 [%s]\n", fileImage.getAbsolutePath());
				return;
			}

			int imageWidth, imageHeight;
			try
			{
				var image = ImageIO.read(fileImage);
				imageWidth = image.getWidth();
				imageHeight = image.getHeight();
			}
			catch (IOException any)
			{
				System.out.printf("读取图片数据出错, 跳过文件 [%s]\n", fileImage.getAbsolutePath());
				return;
			}

			// 开始转换
			var listDotaLabel = DotaReader.read(raw, imageWidth, imageHeight);
			var textYoloLabel = convert(listDotaLabel, mapping);

			// 开始写入
			var fileOut = new File(folderTarget, fileIn.getName());
			try(var ofs = new FileOutputStream(fileOut))
			{
				ofs.write(textYoloLabel.getBytes(StandardCharsets.UTF_8));
				ofs.flush();
			}
			catch (IOException any)
			{
				System.out.printf("写入文件发生错误, 跳过文件 [%s]\n", fileOut.getAbsolutePath());
				return;
			}
		});
	}

	public static final BigDecimal BD2 = new BigDecimal(2);

	public static String convert(List<DotaLabel> list, Map<String, Integer> mapClassOrder)
	{
		var ret = new StringBuffer();
		list.stream().parallel().forEach(labelDota -> {
			var _min = new BigDecimal(Long.MAX_VALUE);
			var _max = new BigDecimal(Long.MIN_VALUE);

			var min = new DecimalPoint(_min, _min);
			var max = new DecimalPoint(_max, _max);
			findMin(min, labelDota.pt1().x, labelDota.pt1().y);
			findMin(min, labelDota.pt2().x, labelDota.pt2().y);
			findMin(min, labelDota.pt3().x, labelDota.pt3().y);
			findMin(min, labelDota.pt4().x, labelDota.pt4().y);
			findMax(max, labelDota.pt1().x, labelDota.pt1().y);
			findMax(max, labelDota.pt2().x, labelDota.pt2().y);
			findMax(max, labelDota.pt3().x, labelDota.pt3().y);
			findMax(max, labelDota.pt4().x, labelDota.pt4().y);

			var orderClass = mapClassOrder.get(labelDota.catalog());
			if(orderClass == null)
			{
				System.out.printf("找不到映射, 跳过 [%s]\n", labelDota.catalog());
				return;
			}

			// center = (max - min) / 2
			// range = box / total
			// 进行一个六位小数的保留
			var boxWidth = max.x.subtract(min.x);
			var boxHeight = max.y.subtract(min.y);
			var centerX = boxWidth.divide(BD2, 6, RoundingMode.HALF_UP);
			var centerY = boxHeight.divide(BD2, 6, RoundingMode.HALF_UP);
			var rangeX = boxWidth.divide(new BigDecimal(labelDota.imageWidth()), 6, RoundingMode.HALF_UP);
			var rangeY = boxHeight.divide(new BigDecimal(labelDota.imageHeight()), 6, RoundingMode.HALF_UP);

			var center = new DecimalPoint(centerX, centerY);
			var range = new DecimalPoint(rangeX, rangeY);

			var labelYolo = new YoloLabel(orderClass, center, range);

			//noinspection StringConcatenationInsideStringBufferAppend
			ret.append(labelYolo.toLabelText() + '\n'); // 是的 我知道自己在干啥
		});
		return ret.toString();
	}
	private static void findMin(DecimalPoint pt, BigDecimal x, BigDecimal y)
	{
		if(pt.x.compareTo(x) > 0)
			pt.x = x;
		if(pt.y.compareTo(y) > 0)
			pt.y = y;
	}
	private static void findMax(DecimalPoint pt, BigDecimal x, BigDecimal y)
	{
		if(pt.x.compareTo(x) < 0)
			pt.x = x;
		if(pt.y.compareTo(y) < 0)
			pt.y = y;
	}
}
