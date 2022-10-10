package firok.tool.alloywrench.util;

import firok.tool.alloywrench.bean.DecimalPoint;
import firok.tool.alloywrench.bean.DotaLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DotaReader
{
	public static List<DotaLabel> read(String raw, int imageWidth, int imageHeight)
	{
		var ret = new ArrayList<DotaLabel>();

		var lines = raw.split("\n");
		Arrays.stream(lines).parallel().forEach(line -> {
			if(line.indexOf(':') > 0) // 这是一行注释
			{
				System.out.printf("注释行 [%s]\n", line);
				return;
			}

			var words = line.split(" ");
			if(words.length != 10)
			{
				System.out.printf("格式错误 [%s]\n", line);
				return;
			}

			try
			{
				var pt1 = new DecimalPoint(words[0], words[1]);
				var pt2 = new DecimalPoint(words[2], words[3]);
				var pt3 = new DecimalPoint(words[4], words[5]);
				var pt4 = new DecimalPoint(words[6], words[7]);
				var catalog = words[8];
				var isDifficult = Integer.parseInt(words[9]) != 0;

				var bean = new DotaLabel(
						pt1, pt2, pt3, pt4,
						catalog, isDifficult,
						imageWidth, imageHeight
				);

				synchronized (ret)
				{
					ret.add(bean);
				}
			}
			catch (Exception any)
			{
				System.out.printf("数据错误 [%s] [%s]\n", line, any.getMessage());
			}
		});


		return ret;
	}
}
