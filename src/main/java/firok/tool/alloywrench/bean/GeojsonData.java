package firok.tool.alloywrench.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @deprecated 内部使用
 * */
@Deprecated
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeojsonData
{
	String type;

	String name;

	List<Feature> features;

	@Data
	public static class Feature
	{
		String type;

		Properties properties;

		Geometry geometry;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Properties
		{
			@JsonProperty("MC")
			String name;
		}

		@Data
		public static class Geometry
		{
			String type;

			BigDecimal[][][][] coordinates;
		}
	}
}
