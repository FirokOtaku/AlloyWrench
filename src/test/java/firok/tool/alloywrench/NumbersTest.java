package firok.tool.alloywrench;

import firok.tool.alloywrench.util.Numbers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * 测试数字裁剪功能
 * */
public class NumbersTest
{
	private static void testOne(String raw, String goal)
	{
		Assertions.assertEquals(goal, Numbers.subnum(new BigDecimal(raw)));
	}

	@Test
	public void test()
	{
		testOne("0.123", "0.123");
		testOne("123455", "123455");
		testOne("0.123455", "0.123455");
		testOne("0.123456", "0.123456");
		testOne("12345678.123456", "12345678.123456");
		testOne("12345678.12345678", "12345678.123456");
	}
}
