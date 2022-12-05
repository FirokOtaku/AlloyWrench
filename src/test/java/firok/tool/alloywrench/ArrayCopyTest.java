package firok.tool.alloywrench;

import org.junit.jupiter.api.Test;

import java.util.*;

public class ArrayCopyTest
{
	@Test
	void test()
	{
		var arr = new String[] {
				"copy", "coco", "multi",
				"label-output", "label1", "label2"
		};
		System.out.println(
				Arrays.toString(
						Arrays.copyOfRange(arr, 4, arr.length)
				)
		);
	}
}
