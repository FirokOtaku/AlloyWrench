package firok.tool.alloywrench.task;

import firok.tool.alloywrench.bean.CocoData;
import firok.tool.alloywrench.util.Jsons;
import firok.topaz.Files;

import java.io.File;
import java.util.*;

public class CleanImageFileByCocoTask
{
	public static void execute(
			String pathFolderImages,
			String pathFileLabel
	) throws Exception
	{
		var fileLabel = new File(pathFileLabel);
		var folderImages = new File(pathFolderImages);
		Files.assertExist(folderImages, false, "图片目录不存在");
		Files.assertExist(fileLabel, true, "标签文件不存在");
		var om = Jsons.omDecimal();
		var coco = om.readValue(fileLabel, CocoData.class);
		var files = folderImages.listFiles(File::isFile);
		var images = coco.getImages();
		if(files == null) throw new IllegalArgumentException("目录不包含任何文件");
		if(images.isEmpty()) throw new IllegalArgumentException("标签不包含任何图片");

		var setFilename = new HashSet<String>();
		for(var image : images)
		{
			setFilename.add(image.getFilename());
		}

		Arrays.stream(files).parallel().forEach(file -> {
			var filename = file.getName();
			if(setFilename.contains(filename)) return;
			file.delete();
		});
	}
}
