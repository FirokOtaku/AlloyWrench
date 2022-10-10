package firok.tool.alloywrench.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Files
{
	public static String read(File file) throws IOException
	{
		var ret = new StringBuilder();
		try(var in = new Scanner(file, StandardCharsets.UTF_8))
		{
			while(in.hasNextLine())
			{
				var line = in.nextLine();
				ret.append(line).append("\n");
			}
		}
		return ret.toString();
	}
}
