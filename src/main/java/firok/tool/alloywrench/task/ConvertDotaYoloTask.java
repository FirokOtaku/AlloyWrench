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
			System.out.printf("处理文件 [%s]\n", filename);

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
			var decimalWidth = new BigDecimal(imageWidth);
			var decimalHeight = new BigDecimal(imageHeight);
			var textYoloLabel = new StringBuffer();
			listDotaLabel.stream().parallel().forEach(labelDota -> {
				// DOTA 集里的数据有的点在图片外面, 绝了
				// 这里手动处理一下, 省的转换之后的 YOLO 标签有问题
				DecimalPoint pt1 = labelDota.pt1(), pt2 = labelDota.pt2(),
						pt3 = labelDota.pt3(), pt4 = labelDota.pt4();
				checkMax(pt1, decimalWidth, decimalHeight);
				checkMax(pt2, decimalWidth, decimalHeight);
				checkMax(pt3, decimalWidth, decimalHeight);
				checkMax(pt4, decimalWidth, decimalHeight);
				checkMin0(pt1);
				checkMin0(pt2);
				checkMin0(pt3);
				checkMin0(pt4);

				// 寻找标签边界点
				var min = new DecimalPoint(_min, _min);
				var max = new DecimalPoint(_max, _max);
				findMin(min, pt1.x, pt1.y);
				findMin(min, pt2.x, pt2.y);
				findMin(min, pt3.x, pt3.y);
				findMin(min, pt4.x, pt4.y);
				findMax(max, pt1.x, pt1.y);
				findMax(max, pt2.x, pt2.y);
				findMax(max, pt3.x, pt3.y);
				findMax(max, pt4.x, pt4.y);

				var classId = mapping.get(labelDota.catalog());
				if(classId == null)
				{
					System.out.printf("找不到映射, 跳过 [%s]\n", labelDota.catalog());
					return;
				}

				// center = (max + min) / 2 / total
				// range = box / total
				// 进行一个六位小数的保留
				var boxWidth = max.x.subtract(min.x);
				var boxHeight = max.y.subtract(min.y);
				var centerX = max.x.add(min.x).divide(BD2, 6, RoundingMode.HALF_UP).divide(decimalWidth, 6, RoundingMode.HALF_UP);
				var centerY = max.y.add(min.y).divide(BD2, 6, RoundingMode.HALF_UP).divide(decimalHeight, 6, RoundingMode.HALF_UP);
				var rangeX = boxWidth.divide(decimalWidth, 6, RoundingMode.HALF_UP);
				var rangeY = boxHeight.divide(decimalHeight, 6, RoundingMode.HALF_UP);

//				if(centerX.compareTo(BigDecimal.ONE) > 0 || centerY.compareTo(BigDecimal.ONE) > 0)
//					new RuntimeException("center calc exception");
//				if(rangeX.compareTo(BigDecimal.ONE) > 0 || rangeY.compareTo(BigDecimal.ONE) > 0)
//					new RuntimeException("range calc exception");

				var center = new DecimalPoint(centerX, centerY);
				var range = new DecimalPoint(rangeX, rangeY);

				var labelYolo = new YoloLabel(classId, center, range);

				//noinspection StringConcatenationInsideStringBufferAppend
				textYoloLabel.append(labelYolo.toLabelText() + '\n'); // 是的 我知道自己在干啥
			});

			// 开始写入
			var fileOut = new File(folderTarget, fileIn.getName());
			try(var ofs = new FileOutputStream(fileOut))
			{
				ofs.write(textYoloLabel.toString().getBytes(StandardCharsets.UTF_8));
				ofs.flush();
			}
			catch (IOException any)
			{
				System.out.printf("写入文件发生错误, 跳过文件 [%s]\n", fileOut.getAbsolutePath());
				return;
			}

			System.out.printf("完成处理 [%s]\n", filename);
		});
	}

	public static final BigDecimal BD2 = new BigDecimal(2);
	public static final BigDecimal _min = new BigDecimal(Long.MAX_VALUE);
	public static final BigDecimal _max = new BigDecimal(Long.MIN_VALUE);

	private static void checkMax(DecimalPoint pt, BigDecimal maxX, BigDecimal maxY)
	{
		if(pt.x.compareTo(maxX) > 0)
			pt.x = maxX;
		if(pt.y.compareTo(maxY) > 0)
			pt.y = maxY;
	}
	private static void checkMin0(DecimalPoint pt)
	{
		if(pt.x.compareTo(BigDecimal.ZERO) < 0)
			pt.x = BigDecimal.ZERO;
		if(pt.y.compareTo(BigDecimal.ZERO) < 0)
			pt.y = BigDecimal.ZERO;
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
