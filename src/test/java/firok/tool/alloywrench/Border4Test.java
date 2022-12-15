package firok.tool.alloywrench;

import firok.tool.alloywrench.bean.Border4;
import org.junit.jupiter.api.Test;

public class Border4Test
{
	@Test
	void test()
	{
		var b41 = new Border4(
				100, 0,
				0, 100,
				100, 100
		);
		System.out.println(b41);

		System.out.println(b41.cut(11, 12, 81, 82));

	}
}
