package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import firok.tool.alloywrench.bean.DotaLabel;
import firok.tool.alloywrench.util.LabelmeReader;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ConvertLabelmeDotaTask
{
	public static void execute(String pathSource, String pathTarget)
	{
		var fileSource = new File(pathSource);
		if(!fileSource.exists())
		{
			System.out.println("源文件不存在");
			return;
		}
		var fileTarget = new File(pathTarget);
		if(fileTarget.exists())
		{
			System.out.println("目标文件已经存在");
			return;
		}

		var om = new ObjectMapper();

		try
		{
			System.out.println("读取 labelme 标签数据");
			var json = om.readTree(fileSource);

			var shapes = json.get("shapes") instanceof ArrayNode arr ? arr : null;
			if(shapes == null)
			{
				System.out.println("JSON 格式错误");
				return;
			}
			if(shapes.size() == 0)
			{
				System.out.println("图形数量为 0");
				return;
			}

			var listLabelme = LabelmeReader.read(pathSource);
			System.out.printf("共读取到 %d 个图形\n", listLabelme.size());

			var listDota = new ArrayList<DotaLabel>();
			for(var labelme : listLabelme)
			{
				var pt1 = labelme.points().get(0);
				var pt2 = labelme.points().get(1);
				var pt3 = labelme.points().get(2);
				var pt4 = labelme.points().get(3);
				var imageWidth = labelme.imageWidth();
				var imageHeight = labelme.imageHeight();
				var catalog = labelme.catalog();
				var label = new DotaLabel(
						pt1, pt2, pt3, pt4,
						catalog, false,
						imageWidth, imageHeight
				);
				listDota.add(label);
			}

			System.out.println("创建 DOTA 标签文件");
			try(var out = new PrintStream(fileTarget, StandardCharsets.UTF_8))
			{
				for(var labelDota : listDota)
				{
					var text = labelDota.toLabelText();
					out.println(text);
				}
				out.flush();
			}

			System.out.println("完成");
		}
		catch (Exception any)
		{
			any.printStackTrace(System.err);
			System.err.println("执行过程发生错误");
		}
	}
}
