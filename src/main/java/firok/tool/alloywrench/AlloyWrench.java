package firok.tool.alloywrench;

import firok.tool.alloywrench.task.CollectDotaTask;
import firok.tool.alloywrench.task.ConvertDotaYoloTask;
import firok.tool.alloywrench.task.RendererDotaTask;

public class AlloyWrench
{
	public static final String name = "Alloy Wrench";
	public static final String author = "Firok";
	public static final String version = "0.5.0";
	public static final String link = "https://github.com/FirokOtaku/AlloyWrench";

	public static void main(String[] args)
	{
		final var len = args == null ? 0 : args.length;
		if(len == 7 && "convert".equals(args[0]) && "dota".equals(args[1]) && "yolo".equals(args[2]))
		{
			ConvertDotaYoloTask.execute(args[3], args[4], args[5], args[6]);
		}
		else if(len >= 4 && "collect".equals(args[0]) && "dota".equals(args[1]))
		{
			var pathMappingFile = args[2];
			var pathSourceFolders = new String[args.length - 3];
			System.arraycopy(args, 3, pathSourceFolders, 0, pathSourceFolders.length);
			CollectDotaTask.execute(pathMappingFile, pathSourceFolders);
		}
		else if(len == 1 && "renderer".equals(args[0]))
			RendererDotaTask.execute();
		else
			printHelp();
	}

	private static void printHelp()
	{
		System.out.printf("""
				%s %s by %s
				view on GitHub: %s
                
				* 收集 DOTA 数据集中的目标类型
				> collect dota {mapping-file} {source-folder} [{source-folder}...]
				- {mapping-file} CLASS ID 映射文件
				- {source-folder} 源目录
                
				* 将 DOTA 数据集标签转换为 YOLO 格式
				> convert dota yolo {source-folder} {image-folder} {mapping-file} {target-folder}
				- {source-folder} 源目录
				- {image-folder} 图片目录
				- {mapping-file} CLASS ID 映射文件
				- {target-folder} 目标目录
				
				* 打开标签数据可视化工具
				> renderer
				""", name, version, author, link);
	}


}
