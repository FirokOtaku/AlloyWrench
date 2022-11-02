package firok.tool.alloywrench;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class StringTests
{
//	@Test
	public void testCopy()
	{
		var source = new String[10];
		for(int step = 0; step < 10; step++)
		{
			source[step] = String.valueOf(step);
		}

		var target = new String[source.length - 2];
		System.arraycopy(source, 2, target, 0, target.length);
		System.out.println(Arrays.toString(target));
	}

	@Test
	public void testIndex()
	{
		var filename = "abc123.txt";
		System.out.println(
				filename.substring(0, filename.lastIndexOf('.'))
		);
	}
}
