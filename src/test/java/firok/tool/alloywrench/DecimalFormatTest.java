package firok.tool.alloywrench;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class DecimalFormatTest
{
//	@Test
	void test()
	{
		var d1 = new BigDecimal("0.123456789");
		var str1 = d1.toString();
		System.out.println("raw: " + str1);
		System.out.println("raw.substr: " + str1.substring(0, 6));
		var index1 = str1.indexOf('.');
		System.out.println("indexOf dot: " + index1);
		System.out.println("raw.substr index: " + str1.substring(0, Math.min(index1 + 7, str1.length())));
	}
}
