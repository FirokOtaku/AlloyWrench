package firok.tool.alloywrench.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CocoData
{
	List<Image> images;
	List<Annotation> annotations;
	List<Category> categories;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Image
	{
		int width, height, id;

		@JsonProperty("file_name")
		String filename;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Annotation
	{
		int id;

		@JsonProperty("image_id")
		int imageId;

		List<BigDecimal> bbox;

		List<List<BigDecimal>> segmentation;

		@JsonProperty("category_id")
		int categoryId;

		BigDecimal area;

		@JsonProperty("iscrowd")
		int isCrowd;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Category
	{
		int id;
		String name;
		@JsonProperty("supercategory")
		String superCategory;
	}
}
