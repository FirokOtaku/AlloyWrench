package firok.tool.alloywrench;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.ScriptJsonData;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ScriptJsonDataLoadTests
{
	@Test
	void test() throws Exception
	{
		var om = new ObjectMapper();
		var file = new File("V:/workspace/workspace-open-mmlab/dataset/single-png/test-cut/img (8).png.ep312.json");
//		var sjd = om.readValue(file, ScriptJsonData.class);
//		var masks = sjd.getMasks();
		var json = om.readTree(file);
		var m1 = json.get("masks");


	}
}
