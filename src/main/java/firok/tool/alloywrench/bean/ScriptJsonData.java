package firok.tool.alloywrench.bean;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScriptJsonData
{
	int[] labels;
	double[][] bboxes;
	double[][][] polygons;
	double[][][][] masks;

	public int countEntry()
	{
		return labels == null ? 0 : labels.length;
	}
}
