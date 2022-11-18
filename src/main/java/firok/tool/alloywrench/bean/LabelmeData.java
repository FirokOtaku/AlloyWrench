package firok.tool.alloywrench.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelmeData
{
	@JsonProperty("image_path")
	String imagePath;

	@JsonProperty("image_width")
	int imageWidth;

	@JsonProperty("image_height")
	int imageHeight;

	List<Shape> shapes;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	public static class Shape
	{
		String label;

		List<BigDecimal[]> points;

		@JsonProperty("shape_type")
		String shapeType;
	}
}
