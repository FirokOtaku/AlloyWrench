package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.CocoData;
import firok.topaz.resource.Files;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * 用来检查并修复 coco 数据集文件中的错误
 * */
public class FixCocoTask
{
    public static final String MethodFix = "fix";
    public static final String MethodCheck = "check";
    public static void execute(boolean executeFix, String pathImages, String pathCoco) throws Exception
    {
        var folderImages = new File(pathImages);
        var fileCoco = new File(pathCoco);
        Files.assertExist(folderImages, false, "图片目录不存在");
        Files.assertExist(fileCoco, true, "coco 数据集文件不存在");

        var om = new ObjectMapper();
        var coco = om.readValue(fileCoco, CocoData.class);

        System.out.println("检查标注中的所有图片是否存在...");
        var setImageId = new HashSet<Integer>();
        for(var image : coco.getImages())
        {
            setImageId.add(image.getId());
        }
        var setCateIdNeed = new HashSet<Integer>();
        for(var anno : new ArrayList<>(coco.getAnnotations()))
        {
            var imageIdNeed = anno.getImageId();
            if(!setImageId.contains(imageIdNeed))
            {
                System.err.println("(暂不支持修复此类错误) 标注中的图片不存在: " + imageIdNeed);
                throw new UnsupportedOperationException();
            }
            setCateIdNeed.add(anno.getCategoryId());
        }

        System.out.println("检查标注中的所有类别是否存在...");
        var setCateId = new HashSet<Integer>();
        for(var cate : coco.getCategories())
        {
            setCateId.add(cate.getId());
        }
        for(var cateIdNeed : setCateIdNeed)
        {
            if(!setCateId.contains(cateIdNeed))
            {
                System.err.println("(暂不支持修复此类错误) 标注中的类别不存在: " + cateIdNeed);
                throw new UnsupportedOperationException();
            }
        }

        System.out.println("检查图片文件和标注匹配性...");
        for(var image : coco.getImages())
        {
            var filename = image.getFilename();
            var fileImage = new File(folderImages, filename);
            if(!fileImage.exists())
            {
                System.err.println("(暂不支持修复此类错误) 图片文件不存在: " + filename);
                throw new UnsupportedOperationException();
            }

            BufferedImage imageInstance;
            try { imageInstance = ImageIO.read(fileImage); }
            catch (Exception any)
            {
                System.err.println("(暂不支持修复此类错误) 图片文件无法读取: " + filename);
                throw new UnsupportedOperationException();
            }

            var imageWidth = imageInstance.getWidth();
            var imageHeight = imageInstance.getHeight();
            if(image.getWidth() != imageWidth)
            {
                if(executeFix)
                {
                    image.setWidth(imageWidth);
                }
                System.out.println("图片宽度不匹配: " + filename + (executeFix ? ", 已经修复": ""));
            }
            if(image.getHeight() != imageHeight)
            {
                if(executeFix)
                {
                    image.setHeight(imageHeight);
                }
                System.out.println("图片高度不匹配: " + filename + (executeFix ? ", 已经修复": ""));
            }
        }

        if(executeFix)
        {
            System.out.println("写回数据");
            om.writeValue(fileCoco, coco);
        }
    }
}
