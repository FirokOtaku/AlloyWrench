package firok.tool.alloywrench;

import firok.tool.alloywrench.http.ConvertScriptJsonGeojsonServer;
import firok.tool.alloywrench.task.*;
import firok.topaz.Topaz;
import firok.topaz.general.Version;

import java.util.*;

public class AlloyWrench
{
	public static final String name = "Alloy Wrench";
	public static final String author = "Firok";
	public static final Version version = new Version(0, 34, 0);
	public static final String link = "https://github.com/FirokOtaku/AlloyWrench";

	private static boolean compare(String[] args, int length, String... needs)
	{
		if(args == null || args.length < length) return false;
		for(int step = 0; step < needs.length; step++)
		{
			var need = needs[step];
			if(need == null) continue;
			if(!args[step].equals(need)) return false;
		}
		return true;
	}
	private static boolean compare(String[] args, String... needs)
	{
		if(needs == null || needs.length == 0) return true;
		if(args == null || args.length < needs.length) return false;
		for(int step = 0; step < needs.length; step++)
		{
			var need = needs[step];
			if(need == null) continue;
			var arg = args[step];
			if (!Objects.equals(arg, need)) return false;
		}
		return true;
	}
	public static void main(String[] args) throws Exception
	{
//		final var len = args == null ? 0 : args.length;
		if(compare(args, 7, "convert", "dota", "yolo"))
			ConvertDotaYoloTask.execute(args[3], args[4], args[5], args[6]);

		else if(compare(args, 7, "convert", "dota", "coco"))
			System.out.println("not implemented"); // todo low

		else if(compare(args, 5, "convert", "coco", "dota"))
			ConvertCocoDotaTask.execute(args[3], args[4]);

		else if(compare(args, 7, "convert", "coco", "mvtec"))
			ConvertCocoMvtecTask.execute(args[3], args[4], args[5], args[6]);

		else if(compare(args, 5, "convert", "labelme", "coco"))
			ConvertLabelmeCocoTask.execute(args[3], args[4]);
		else if(compare(args, 6, "convert", "labelme", "yolo"))
			ConvertLabelmeYoloTask.execute(args[3], args[4], args[5]);
		else if(compare(args, 5, "convert", "labelme", "dota"))
			ConvertLabelmeDotaTask.execute(args[3], args[4]);

		else if(compare(args, 10, "convert", "geojson", "coco"))
		{
			// 我知道自己在干啥.jpg
			//noinspection deprecation
			ConvertMultiPolygonGeojsonCocoTask.execute(
					args[3],
					args[4],
					args[5],
					new String[]{args[6], args[7], args[8], args[9]}
			);
		}

		else if(compare(args, 4, "collect", "dota"))
		{
			var pathMappingFile = args[2];
			var pathSourceFolders = new String[args.length - 3];
			System.arraycopy(args, 3, pathSourceFolders, 0, pathSourceFolders.length);
			CollectDotaTask.execute(pathMappingFile, pathSourceFolders);
		}

		else if(compare(args, "merge", "coco", "multi") && args.length > 5)
		{
			MergeCocoTask.execute(
					Arrays.copyOfRange(args, 4, args.length),
					args[3]
			);
		}
		else if(compare(args, 7, "cut", "block", "direct"))
			CutImageDirectTask.execute(args[3], args[4], args[5], args[6], args[7], args[8]);
		else if(compare(args, 7, "cut", "block", "dota"))
			CutImageBlockDotaTask.execute(args[3], args[4], args[5], args[6]);
		else if(compare(args, 8, "cut", "block", "coco"))
			CutImageBlockCocoTask.execute(args[3], args[4], args[5], args[6], args[7]);
		else if(compare(args, 6, "cut", "coco"))
			CutImageBlockByCocoInstanceTask.execute(args[2], args[3], args[4], args[5]);

//		else if(compare(args, 5, "clean", "dataset", "coco"))
//		{
//			CleanDatasetCoco.execute(args[3], args[4]);
//		}
		else if(compare(args, 6, "merge", "coco", "category"))
			MergeCocoCategoryTask.execute(args[3], args[4], args[5]);

		else if(args.length >= 4 && "filter".equals(args[0]) && "coco".equals(args[1]))
		{
			enum ModeStream { Unset, AnnoId, ImageId, CategoryId }
			var ms = ModeStream.Unset;
			var pathInputLabel = args[2];
			var pathOutputLabel = args[3];
			var setAnnoId = new HashSet<Integer>();
			var setImageId = new HashSet<Integer>();
			var setCategoryId = new HashSet<Integer>();
			for(var step = 4; step < args.length; step++)
			{
				var word = args[step];

				switch(word)
				{
					case "--filter-anno-id" -> ms = ModeStream.AnnoId;
					case "--filter-image-id" -> ms = ModeStream.ImageId;
					case "--filter-category-id" -> ms = ModeStream.CategoryId;
					default -> (switch (ms)
					{
						case AnnoId -> setAnnoId;
						case ImageId -> setImageId;
						case CategoryId -> setCategoryId;
						case Unset -> throw new IllegalStateException("未指定过滤集");
					}).add(Integer.valueOf(word));
				}
			}

			FilterCocoTask.execute(
					pathInputLabel,
					pathOutputLabel,
					setImageId,
					setCategoryId,
					setAnnoId
			);
		}

		else if(compare(args, 2, "server"))
		{
			try
			{
				int port = Integer.parseInt(args[1]);
				//noinspection resource,deprecation
				new ConvertScriptJsonGeojsonServer(port);
				System.out.println("服务器启动");
			}
			catch (Exception any)
			{
				System.out.println("服务器启动失败");
			}
		}

		else if(compare(args, 1, "renderer"))
			RendererDotaTask.execute();

//		else if(len == 1 && "marker".equals(args[0]))
//			MarkerTask.execute();

		else
			printHelp();
	}

	private static void printHelp()
	{
		System.out.printf("""
				%s %s by %s depends on %s %s
				Open source under MIT license. View on GitHub: %s
                
				* 收集 DOTA 数据集中的目标类型
				> collect dota {mapping-file} {source-folder} [{source-folder}...]
				- {mapping-file} 将要创建的 CLASS ID 映射文件
				- {source-folder} 源目录
                
				* 将 DOTA 数据集标签转换为 YOLO 格式
				> convert dota yolo {source-folder} {image-folder} {mapping-file} {target-folder}
				- {source-folder} 源目录
				- {image-folder} 图片目录
				- {mapping-file} 将要创建的 CLASS ID 映射文件
				- {target-folder} 将要创建的目标目录
				
				# 将 labelme 创建的 JSON 数据文件转换为 COCO 格式的
				> convert labelme coco {label-input} {label-output}
				- {label-input} labelme 标签文件
				- {label-output} 目标 COCO 标签文件
				
				* 将 COCO 数据集转换为 DOTA 格式
				> convert coco dota {label-file} {target-label-folder}
				* {label-file} 要转换的 COCO 数据文件
				* {target-label-folder} 导出目标目录
				
				* 将 COCO 数据集转换为 MVTec 格式
				> convert coco mvtec {images-folder} {label-file} {mapping-file} {target-folder}
				- {images-folder} COCO 图片目录
				- {label-file} COCO 标签文件
				- {mapping-file} 映射文件, 格式详见 GitHub 文档
				- {target-folder} 储存转换后数据集的目录
				
				* 将 labelme 数据集 JSON 转换为 YOLO 格式
				> convert labelme yolo {labelme-file} {yolo-file} {mapping-file}
				- {labelme-file} labelme JSON 文件
				- {yolo-file} 将要创建的 YOLO 标签文件
				- {mapping-file} 将要创建的映射文件地址
				
				* 直接根据参数按照矩形切割图片
				> cut block direct {image-file} {output-folder} {cut-width} {cut-height} {overlying-width} {overlying-height}
				* {image-file} 要切割的图片文件
				* {output-folder} 输出目录
				* {cut-width} 切片宽度
				* {cut-height} 切片高度
				* {overlying-width} 切片间叠盖宽度
				* {overlying-height} 切片间叠高高度
				
				* 切分图片和相关的 DOTA 标签, 按照固定大小切分
				> cut block dota {image-file} {label-file} {target-image-folder} {target-label-folder}
				- {image-file} 要切分的大图路径
				- {label-file} 标签数据文件
				- {target-image-folder} 储存切分后图像的目录
				- {target-label-folder} 储存切分后标签的目录
				
				* 把单一的 COCO 标签图按照矩形范围切割成小图 COCO 数据集
				> cut block coco {label-file} {image-folder} {target-label-file} {target-image-folder} {image-prefix}
				- {label-file} 要切割的标签文件
				- {image-folder} 要切割的图片目录
				- {target-label-file} 目标标签文件
				- {target-image-folder} 目标图片目录
				- {image-prefix} 切分的子图片文件名前缀
				
				* 根据 COCO 标签数据切分图片, 只保留每个实例本身
				> cut coco {image-file} {label-file} {target-image-folder} {target-label-folder}
				- {image-file-base} 储存要切分的图片的目录
				- {label-file} 标签数据文件
				- {target-image-folder} 储存切分后图像的目录
				- {target-label} 储存切分后标签的文件
				
				* 合并两个 COCO 标签文件
				> merge coco multi {label-output} {label-file} {label-file} [{label-file}...]
				- {label-file} 文件
				- {label-output} 目标输出文件
				
				* 合并 COCO 标签文件中的种类
				> merge coco category {label-input} {label-output} {mapping-file}
				* {label-input} 标签输入
				* {label-outpu} 标签输出
				* {mapping-file} JSON 格式映射数据
				
				* 过滤 COCO 标签文件的内容
				> filter coco {input-label-file} {output-label-file}
				    [--filter-anno-id <anno-id> [, <anno-id> ...]]
				    [--filter-image-id <image-id> [, <image-id> ...]]
				    [--filter-category-id <category-id> [, <category-id> ...]]
				- {input-label-file} 源标签文件
				- {output-label-file} 目标输出标签文件
				- [filter-anno-id <anno-id>] 标签 ID 过滤器. 黑名单策略, 指定 ID 的标签将不会出现在结果
				- [filter-image-id <image-id>] 图片 ID 过滤器. 黑名单策略, 指定 ID 的图片将不会出现在结果
				- [filter-category-id <category-id>] 种类 ID 过滤器. 黑名单策略, 指定 ID 的类型将不会出现在结果
				
				* 打开标签数据可视化工具
				> renderer
				""", name, version, author, Topaz.NAME, Topaz.VERSION, link);
	}


}
