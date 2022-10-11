package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.DotaLabel;
import firok.tool.alloywrench.util.DotaReader;
import firok.tool.alloywrench.util.Files;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class CollectDotaTask
{
	public static void execute(String pathFileMapping, String... pathSourceFolders)
	{
		var fileMapping = new File(pathFileMapping);
		if(fileMapping.exists())
		{
			System.out.printf("映射文件已经存在, 请指定其它路径 [%s]\n", pathFileMapping);
			return;
		}

		var mapping = new HashMap<String, Integer>();

		Arrays.stream(pathSourceFolders).parallel().forEach(pathSourceFolder -> {
			var folderSource = new File(pathSourceFolder);
			if(!folderSource.exists() || !folderSource.isDirectory())
			{
				System.out.printf("源目录不存在, 跳过 [%s]\n", pathSourceFolder);
				return;
			}

			var files = folderSource.listFiles();
			if(files == null || files.length <= 0)
			{
				System.out.println("源目录为空, 跳过");
				return;
			}

			Arrays.stream(files).parallel().forEach(fileLabel -> {
				String raw;
				try
				{
					raw = Files.read(fileLabel);
				}
				catch (IOException any)
				{
					System.out.printf("读取标签数据文件出错, 跳过 [%s]\n", any.getMessage());
					return;
				}

				var filename = fileLabel.getName();
				System.out.printf("处理文件 [%s]\n", filename);

				// 这里用不着读取图片文件, 直接赋 0
				var listLabel = DotaReader.read(raw, 0, 0);
				listLabel.stream()
				.parallel()
				.map(DotaLabel::catalog)
				.forEach(category -> {
					synchronized (mapping)
					{
						if(mapping.containsKey(category))
							return;
						var size = mapping.size();
						mapping.put(category, size);
					}
				});

				System.out.printf("完成处理 [%s]\n", filename);
			});
		});

		// 把映射写到文件里
		var om = new ObjectMapper();
		try
		{
			om.writeValue(fileMapping, mapping);
		}
		catch (IOException any)
		{
			System.out.printf("写入映射文件出错 [%s]\n", any.getMessage());
			return;
		}

		System.out.println("写入映射完成");
	}
}
