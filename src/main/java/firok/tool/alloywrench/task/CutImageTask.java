package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.*;
import firok.tool.alloywrench.util.DotaReader;
import firok.tool.alloywrench.util.Files;
import firok.topaz.MayRunnable;
import firok.topaz.SimpleMultiThread;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CutImageTask
{
	/**
	 * @implSpec 这个方法真 tmd 长啊
	 * */
	public static void execute(
			String pathImage, String pathLabel,
			String pathImageTarget, String pathLabelTarget
	)
	{
		try
		{
			var fileImage = new File(pathImage);
			if(!fileImage.exists())
			{
				System.out.println("要切分的图片文件不存在");
				return;
			}
			var fileLabel = new File(pathLabel);
			if(!fileLabel.exists())
			{
				System.out.println("标签文件不存在");
				return;
			}
			var folderTargetImage = new File(pathImageTarget);
			if(folderTargetImage.exists())
			{
				System.out.println("目标图片目录已经存在");
				return;
			}
			var folderTargetLabel = new File(pathLabelTarget);
			if(folderTargetLabel.exists())
			{
				System.out.println("目标标签目录已经存在");
				return;
			}
			var resultImageMkdirs = folderTargetImage.mkdirs();
			if(!resultImageMkdirs)
			{
				System.out.println("创建目标图片目录失败");
				return;
			}
			var resultLabelMkdirs = folderTargetLabel.mkdirs();
			if(!resultLabelMkdirs)
			{
				System.out.println("创建目标标签目录失败");
				return;
			}

			// 你看下面这个写多线程的方式扭曲不扭曲
			// 其实就是懒得看 JDK 线程池 API 的用法
			// 反正能跑就完事了
			@SuppressWarnings("RedundantCast")
			var results = new Object[] {
					(List<DotaLabel>) null,
					(BufferedImage) null
			};

			var mapLabelRange = new HashMap<DotaLabel, DecimalRect>();

			var poolRead = new SimpleMultiThread(true,
					() -> {
						System.out.println("读取标签文件");
						var raw = Files.read(fileLabel);
						var list = DotaReader.read(raw, 0, 0);
						results[0] = list;
						System.out.println("标签读取完成, 开始计算标签范围");
						list.stream().parallel().forEach(label -> {
							var rect = DecimalPoint.toRect(label.pt1(),label.pt2(),label.pt3(),label.pt4());
							synchronized (mapLabelRange)
							{
								mapLabelRange.put(label, rect);
							}
						});
						System.out.println("标签范围计算完成");
					},
					() -> {
						System.out.println("开始读取图片, 这大抵是需要很长时间");
						var image = ImageIO.read(fileImage);
						System.out.println("图片读取完成");
						results[1] = image;
					}
			);
			poolRead.start();
			poolRead.waitEnd(500);

			var exception = poolRead.anyException();
			if(exception != null)
			{
				exception.printStackTrace(System.err);
				System.err.println("读取某个文件发生错误");
				return;
			}

			System.out.println("至此, 我们获取了所有必须的数据, 即将开始一段伟大旅程");

			// 爷很清楚自己在做什么.jpg
			@SuppressWarnings("unchecked") var listLabelDota = (List<DotaLabel>) results[0];
			var image = (BufferedImage) results[1];
			var imageWidth = image.getWidth();
			var imageHeight = image.getHeight();

			System.out.printf("图片大小 [%d, %d]\n", imageWidth, imageHeight);
			System.out.printf("切片大小 [%d, %d]\n", pieceX, pieceY);
			System.out.printf("叠盖大小 [%d, %d]\n", overlyingX, overlyingY);
			if(imageWidth == 0 || imageHeight == 0)
			{
				System.out.println("图片大小错误");
				return;
			}

			System.out.println("计算裁剪范围...");
			var listRange = new ArrayList<IntRect>();
			int fromX = 0, toX = pieceX;
			while(true)
			{
				final boolean isFinalX = toX >= imageWidth;

				if(imageWidth < toX)
				{
					toX = imageWidth;
					fromX = Math.max(0, imageWidth - pieceX);
				}

				int fromY = 0, toY = pieceY;
				while(true)
				{
					final boolean isFinalY = toY >= imageHeight;

					if(imageHeight < toY)
					{
						toY = imageHeight;
						fromY = Math.max(0, imageHeight - pieceY);
					}

					var range = new IntRect(fromX, fromY, toX, toY);
					listRange.add(range);

					if(isFinalY) break;

					fromY += cutOffsetY;
					toY = fromY + pieceY;
				}

				if(isFinalX) break;

				fromX += cutOffsetX;
				toX = fromX + pieceX;
			}

			System.out.printf("经过及其精密的计算, 我们获得了 %d 个裁剪区域, 下面的时间交给 AWT\n", listRange.size());

			var imageFilename = fileImage.getName();
			var indexDot = imageFilename.indexOf('.');
			var imageName = imageFilename.substring(0, indexDot);

			var processors = new ArrayList<MayRunnable>(listRange.size() * 2);
			listRange.stream().parallel().forEach(rangeCut ->
			{
				var rangeName = "裁剪区域 [%d, %d ~ %d, %d]".formatted(rangeCut.fromX(), rangeCut.fromY(), rangeCut.toX(), rangeCut.toY());
				var imageSub = image.getSubimage(rangeCut.fromX(), rangeCut.fromY(), rangeCut.toX() - rangeCut.fromX(), rangeCut.toY() - rangeCut.fromY());

				var filename = "%s_%d_%d_%d_%d".formatted(
						imageName,
						rangeCut.fromX(), rangeCut.fromY(), rangeCut.toX(), rangeCut.toY()
				);
				var fileImageSub = new File(folderTargetImage, filename + ".png");
				var fileLabelSub = new File(folderTargetLabel, filename + ".txt");

				// 当前切片相对于原始切片的偏移
				var decimalOffsetX = new BigDecimal(rangeCut.fromX());
				var decimalOffsetY = new BigDecimal(rangeCut.fromY());

				MayRunnable processorCut = () ->
				{
					System.out.println(rangeName + " : 切片开始");

					try
					{
						System.out.println(rangeName + " : 写入切片图片开始");
						ImageIO.write(imageSub, "png", fileImageSub);
						System.out.println(rangeName + " : 写入切片图片完成");
					} catch (IOException any)
					{
						System.out.println(rangeName + " : 写入切片图片错误");
					}

					System.out.println(rangeName + " : 切片完成");
				};
				MayRunnable processorLabel = () -> {
					System.out.println(rangeName + " : 标签处理开始");
					try(var ofs = new PrintStream(fileLabelSub, StandardCharsets.UTF_8))
					{
						// 计算所有在范围内的标签
						// 并且会带上偏移量
						for(var labelDota : listLabelDota)
						{
							var labelRange = mapLabelRange.get(labelDota);
							if(!labelRange.in(rangeCut)) continue;

							// 计算偏移标签
							var labelOffset = labelDota.subtract(decimalOffsetX, decimalOffsetY);
							ofs.println(labelOffset.toLabelText());
						}

						ofs.flush();
					}

					System.out.println(rangeName + " : 标签处理结束");
				};
				processors.add(processorCut);
				processors.add(processorLabel);
			});

			var poolCut = new SimpleMultiThread(true, processors.toArray(new MayRunnable[0]));
			poolCut.start();
			poolCut.waitEnd(1000);
			System.out.println("旅程已然抵达终点");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.out.println("切分发生错误: " + any.getMessage());
		}
	}
	// 裁剪偏移量
	private static final int overlyingX = 400;
	private static final int overlyingY = 400;
	// 裁剪之后的大小
	private static final int pieceX = 3000;
	private static final int pieceY = 3000;
	// 裁剪偏移量
	private static final int cutOffsetX = pieceX - overlyingX;
	private static final int cutOffsetY = pieceY - overlyingY;
}
