package firok.tool.alloywrench.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class CocoData
{
	List<Image> images;
	List<Annotation> annotations;
	List<Category> categories;

	@Data
	public static class Image
	{
		int width, height, id;

		@JsonProperty("file_name")
		String filename;
	}

	@Data
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
	public static class Category
	{
		int id;
		String name;
		@JsonProperty("supercategory")
		String superCategory;
	}
}
