package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.*;
import firok.tool.alloywrench.util.DotaReader;
import firok.tool.alloywrench.util.Files;
import firok.tool.alloywrench.util.Images;
import firok.topaz.function.MayRunnable;
import firok.topaz.thread.SimpleMultiThread;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CutImageBlockDotaTask
{
	/**
	 * 是否对图片切片并输出
	 * */
	public static final boolean OUTPUT_CUT_IMAGE = true;
	/**
	 * 是否对标签切片并输出
	 * */
	public static final boolean OUTPUT_CUT_LABEL = true;
	/**
	 * 裁剪叠盖量
	 * */
	public static final int OVERLYING_X = 400;
	public static final int OVERLYING_Y = 400;
	/**
	 * 裁剪之后的大小
	 * */
	public static final int PIECE_X = 3000;
	public static final int PIECE_Y = 3000;

	public static final int USE_ALL_IN_OR_NO = 1; // 仅当标签范围完全在裁剪范围时才会包含 这会导致跨切片的标签全部被忽略
	public static final int USE_OVERLAP = 2; // 计算标签和裁剪范围的叠盖形状
	/**
	 * 裁剪标签时的计算方式
	 */
	public static final int METHOD_CUT_LABEL = USE_ALL_IN_OR_NO;

	/**
	 * @implSpec 这个方法真 tmd 长啊
	 * */
	@SuppressWarnings("ConstantConditions")
	public static void execute(
			String pathImage, String pathLabel,
			String pathImageTarget, String pathLabelTarget
	) throws Exception
	{
		if(!OUTPUT_CUT_IMAGE && !OUTPUT_CUT_LABEL)
		{
			System.out.println("你想干啥?");
			return;
		}
		if(OVERLYING_X <= 0 || OVERLYING_Y <= 0 || PIECE_X <= 0 || PIECE_Y <= 0)
		{
			System.out.println("嗯?");
			return;
		}
		if(PIECE_X <= OVERLYING_X || PIECE_Y <= OVERLYING_Y)
		{
			System.out.println("哈?");
			return;
		}

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
		System.out.printf("切片大小 [%d, %d]\n", PIECE_X, PIECE_Y);
		System.out.printf("叠盖大小 [%d, %d]\n", OVERLYING_X, OVERLYING_Y);
		if(imageWidth == 0 || imageHeight == 0)
		{
			System.out.println("图片大小错误");
			return;
		}


		System.out.println("计算裁剪范围...");
		var listRange = Images.calcCutRanges(
				imageWidth, imageHeight,
				PIECE_X, PIECE_Y,
				OVERLYING_X, OVERLYING_Y
		);

		System.out.printf("经过及其精密的计算, 我们获得了 %d 个裁剪区域, 下面的时间交给 AWT\n", listRange.size());

		var imageFilename = fileImage.getName();
		var indexDot = imageFilename.indexOf('.');
		var imageName = imageFilename.substring(0, indexDot);

		var processors = new ArrayList<MayRunnable>(listRange.size() * 2);
		listRange.stream().parallel().forEach(rangeCut ->
		{
			var rangeName = "裁剪区域 [%d, %d ~ %d, %d]".formatted(rangeCut.fromX(), rangeCut.fromY(), rangeCut.toX(), rangeCut.toY());

			var filename = "%s_%d_%d_%d_%d".formatted(
					imageName,
					rangeCut.fromX(), rangeCut.fromY(), rangeCut.toX(), rangeCut.toY()
			);
			var fileImageSub = new File(folderTargetImage, filename + ".png");
			var fileLabelSub = new File(folderTargetLabel, filename + ".txt");

			// 当前切片相对于原始切片的偏移
			var decimalOffsetX = new BigDecimal(rangeCut.fromX());
			var decimalOffsetY = new BigDecimal(rangeCut.fromY());

			if(OUTPUT_CUT_IMAGE) processors.add(() ->
			{
				System.out.println(rangeName + " : 切片开始");
				var imageSub = Images.cutImage(image, rangeCut);

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
			});
			if(OUTPUT_CUT_LABEL) processors.add(() -> {
				System.out.println(rangeName + " : 标签处理开始");
				try(var ofs = new PrintStream(fileLabelSub, StandardCharsets.UTF_8))
				{
					// 计算所有在范围内的标签
					// 并且会带上偏移量
					for(var labelDota : listLabelDota)
					{
						var labelRange = mapLabelRange.get(labelDota);
						switch (METHOD_CUT_LABEL)
						{
							case USE_ALL_IN_OR_NO -> {
								if(!labelRange.in(rangeCut)) continue;

								// 计算偏移标签
								var labelOffset = labelDota.subtract(decimalOffsetX, decimalOffsetY);
								ofs.println(labelOffset.toLabelText());
							}
							case USE_OVERLAP -> {
								var rectOverlap = labelRange.overlapOf(rangeCut);
								if(rectOverlap == null) continue;
								var rectOffset = rectOverlap.subtract(decimalOffsetX, decimalOffsetY);
								var labelOffset = new DotaLabel(
										new DecimalPoint(rectOffset.fromX(), rectOffset.fromY()),
										new DecimalPoint(rectOffset.toX(), rectOffset.fromY()),
										new DecimalPoint(rectOffset.toX(), rectOffset.toY()),
										new DecimalPoint(rectOffset.fromX(), rectOffset.toY()),
										labelDota.catalog(), labelDota.isDifficult(),
										labelDota.imageWidth(), labelDota.imageHeight()
								);
								ofs.println(labelOffset.toLabelText());
							}
						}

					}

					ofs.flush();
				}

				System.out.println(rangeName + " : 标签处理结束");
			});
		});

		var poolCut = new SimpleMultiThread(true, processors.toArray(new MayRunnable[0]));
		poolCut.start();
		poolCut.waitEnd(1000);
		System.out.println("旅程已然抵达终点");
	}

}
