package firok.tool.alloywrench.util;

import java.math.BigDecimal;

public class Numbers
{
	@SuppressWarnings("SpellCheckingInspection")
	public static String subnum(BigDecimal number)
	{
		var ret = number.toString();
		var indexDot = ret.indexOf('.');
		return indexDot >= 0 ? ret.substring(0, Math.min(indexDot + 7, ret.length())) : ret;
	}
}
