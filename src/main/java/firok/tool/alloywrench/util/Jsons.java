package firok.tool.alloywrench.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Jsons
{
	/**
	 * 处理精准数字用的
	 * */
	public static ObjectMapper omDecimal()
	{
		var ret = new ObjectMapper();

		ret.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		return ret;
	}
}
