package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.IntRect;
import firok.tool.alloywrench.util.Images;
import firok.topaz.Files;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class CutImageDirectTask
{
	public static void execute(
			String pathImage,
			String pathImageOut,
			String paramCutWidth,
			String paramCutHeight,
			String paramOverlyingWidth,
			String paramOverlyingHeight
	) throws Exception
	{
		var fileImage = new File(pathImage);
		var folderImage = new File(pathImageOut);
		int cutWidth, cutHeight, overlyingWidth, overlyingHeight;
		cutWidth = Integer.parseInt(paramCutWidth);
		cutHeight = Integer.parseInt(paramCutHeight);
		overlyingWidth = Integer.parseInt(paramOverlyingWidth);
		overlyingHeight = Integer.parseInt(paramOverlyingHeight);
		cutImage(fileImage, folderImage, cutWidth, cutHeight, overlyingWidth, overlyingHeight);
		System.out.println("一段直接的旅程结束了");
	}

	/**
	 * 把中间运算用到的数据提供出来
	 * */
	public static Map<IntRect, File> cutImage(
			File fileImage,
			File folderImage,
			int cutWidth, int cutHeight,
			int overlyingWidth, int overlyingHeight
	) throws Exception
	{
		Files.assertExist(fileImage, true, "图片文件不存在");
		Files.assertNoExist(folderImage, "目标目录已经存在");

		System.out.println("加载图片数据");
		var image = ImageIO.read(fileImage);
		System.out.println("图片数据加载完成, 开始计算切割范围");
		int imageHeight = image.getHeight(), imageWidth = image.getWidth();
		var crs = Images.calcCutRanges(imageWidth, imageHeight, cutWidth, cutHeight, overlyingWidth, overlyingHeight);
		System.out.printf("得出切割范围 %d 个\n", crs.size());
		if(crs.isEmpty())
		{
			System.out.println("无需切割");
			return null;
		}
		folderImage.mkdirs();
		var ret = new HashMap<IntRect, File>();
		var _filename = fileImage.getName();
		var filename = _filename.substring(0, _filename.lastIndexOf('.'));
		System.out.println("开始切分图片并写入");
		crs.stream().parallel().forEach(cr -> {
			var filenameSub = "%s-%d,%d-%d,%d.png".formatted(filename, cr.fromX(), cr.fromY(), cr.toX(), cr.toY());
			var fileSub = new File(folderImage, filenameSub);
			var imageSub = Images.cutImage(image, cr);
			try
			{
				ImageIO.write(imageSub, "png", fileSub);
				synchronized (ret)
				{
					ret.put(cr, fileSub);
				}
			}
			catch (Exception any)
			{
				System.out.println("写入切分图片错误");
				throw new RuntimeException(any);
			}
		});
		System.out.println("写入切分图片完成");
		return ret;
	}
}
