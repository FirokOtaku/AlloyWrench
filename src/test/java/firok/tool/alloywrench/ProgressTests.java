package firok.tool.alloywrench;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressTests
{
//	@Test
	public void test() throws Exception
	{
		var file = new File("V:\\221012 影像示例\\前三甲-地图切片\\前三甲1 .png");

		final var len = file.length();
		final var available = new AtomicLong(len);

		var thread = new Thread(() -> {
			while(true)
			{
				try
				{
					var avaNow = available.get();
					System.out.printf("total [%d], now [%d], progress [%f%%]",
							len, avaNow,
							((int)((1f - 1f * avaNow / len) * 1000)) / 10f
							);
					System.out.println("avaNow");
					Thread.sleep(500);
				}
				catch (Exception ignored) { }
			}
		});
		thread.setDaemon(true);
		thread.start();

		try(var ifs = new FileInputStream(file))
		{
			var ava = ifs.available();
			System.out.println("文件大小: " + len);
			System.out.println("流可用: " + ava);
			var buffer = new byte[20 * 1024 * 1024];
			while(ava > 0)
			{
				var lenRead = ifs.read(buffer);
				try { Thread.sleep(100); } catch (Exception ignored) { }
				ava = ifs.available();
				available.set(ava);
			}

			System.out.println("读取完成");
		}
	}
}
