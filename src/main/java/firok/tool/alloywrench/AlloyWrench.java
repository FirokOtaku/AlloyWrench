package firok.tool.alloywrench;

import firok.tool.alloywrench.task.*;
import firok.topaz.Topaz;

public class AlloyWrench
{
	public static final String name = "Alloy Wrench";
	public static final String author = "Firok";
	public static final String version = "0.9.0";
	public static final String link = "https://github.com/FirokOtaku/AlloyWrench";

	public static void main(String[] args)
	{
		final var len = args == null ? 0 : args.length;

		if(len == 7 && "convert".equals(args[0]) && "dota".equals(args[1]) && "yolo".equals(args[2]))
			ConvertDotaYoloTask.execute(args[3], args[4], args[5], args[6]);

		else if(len == 6 && "convert".equals(args[0]) && "labelme".equals(args[1]) && "yolo".equals(args[2]))
			ConvertLabelmeYoloTask.execute(args[3], args[4], args[5]);

		else if(len == 5 && "convert".equals(args[0]) && "labelme".equals(args[1]) && "dota".equals(args[2]))
			ConvertLabelmeDotaTask.execute(args[3], args[4]);

		else if(len >= 4 && "collect".equals(args[0]) && "dota".equals(args[1]))
		{
			var pathMappingFile = args[2];
			var pathSourceFolders = new String[args.length - 3];
			System.arraycopy(args, 3, pathSourceFolders, 0, pathSourceFolders.length);
			CollectDotaTask.execute(pathMappingFile, pathSourceFolders);
		}

		else if(len == 5 && "cut".equals(args[0]))
			CutImageTask.execute(args[1], args[2], args[3], args[4]);

		else if(len == 1 && "renderer".equals(args[0]))
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
				view on GitHub: %s
                
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
				
				* 将 labelme 数据集 JSON 转换为 YOLO 格式
				> convert labelme yolo {labelme-file} {yolo-file} {mapping-file}
				- {labelme-file} labelme JSON 文件
				- {yolo-file} 将要创建的 YOLO 标签文件
				- {mapping-file} 将要创建的映射文件地址
				
				* 切分图片和相关的 DOTA 标签
				> cut {image-file} {label-file} {target-image-folder} {target-label-folder}
				- {image-file} 要切分的大图路径
				- {label-file} 标签数据文件
				- {target-image-folder} 储存切分后图像的目录
				- {target-label-folder} 储存切分后标签的目录
				
				* 打开标签数据可视化工具
				> renderer
				""", name, version, author, Topaz.NAME, Topaz.VERSION, link);
	}


}
