package firok.tool.alloywrench.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.CocoData;
import firok.topaz.annotation.Indev;
import firok.topaz.design.Colors;
import firok.topaz.design.CssColors;
import firok.topaz.function.MayRunnable;
import firok.topaz.general.Collections;
import firok.topaz.thread.Ref;
import firok.topaz.thread.Threads;
import org.locationtech.jts.geom.*;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static firok.topaz.general.Collections.isEmpty;
import static firok.topaz.general.Collections.sizeOf;
import static java.lang.StringTemplate.STR;

/**
 * 将 COCO 格式数据集转换为 MVTec 格式数据集
 * @implNote 这个实现不负责将图片切片为正方形. 如果目标数据集只接受正方形图片, 则需要先用其它指令或工具对数据集中的图像进行切片处理
 * */
@Indev
public class ConvertCocoMvtecTask
{
    private static final GeometryFactory facGeo = JtsSpatialContext.GEO.getShapeFactory().getGeometryFactory();

    public static void execute(String pathImages, String pathDataset, String pathMapping, String pathOutput)
    {
        var folderImages = new File(pathImages);
        var fileDataset = new File(pathDataset);
        var fileMapping = new File(pathMapping);
        var folderOutput = new File(pathOutput);

        if(!folderImages.exists() || !folderImages.isDirectory())
        {
            System.out.println("图片目录不存在");
            return;
        }
        if(!fileDataset.exists() || !fileDataset.isFile())
        {
            System.out.println("数据集文件不存在");
            return;
        }
        if(!fileMapping.exists() || !fileMapping.isFile())
        {
            System.out.println("映射文件不存在");
            return;
        }
        if(folderOutput.exists() && folderOutput.isDirectory())
        {
            System.out.println("输出目录已经存在");
            return;
        }

        var refCoco = new Ref<CocoData>();
        var refMapping = new Ref<Map<String, List<String>>>();
        Threads.waitFor(
                () -> { // 加载 COCO 数据集
                    var om = new ObjectMapper();
                    refCoco.entry = om.readValue(fileDataset, CocoData.class);
                },
                () -> { // 加载映射规则
                    var om = new ObjectMapper();
                    refMapping.entry = om.readValue(fileMapping, new TypeReference<>() {});
                }
        );
        if(refCoco.entry == null)
        {
            System.out.println("COCO 数据集加载错误");
            return;
        }
        if(isEmpty(refCoco.entry.getImages()))
        {
            System.out.println("数据集不包含图片");
            return;
        }
        if(refMapping.entry == null)
        {
            System.out.println("映射规则加载错误");
            return;
        }
        if(!refMapping.entry.containsKey("train.good"))
        {
            System.out.println("映射规则缺少 train.good 字段");
            return;
        }
        if(!refMapping.entry.containsKey("test.good"))
        {
            System.out.println("映射规则缺少 test.good 字段");
            return;
        }

        // 计算图像类别映射
        var mapCategoryId = firok.topaz.general.Collections.mappingKeyValue(
                refCoco.entry.getCategories(),
                CocoData.Category::getId,
                CocoData.Category::getName
        ); // category_id -> category_name
        // 计算图像对应的标注数据映射
        var mapImageAnnotation = firok.topaz.general.Collections.mappingKeyMultiEntityList(
                refCoco.entry.getAnnotations(),
                CocoData.Annotation::getImageId
        ); // image_id -> annotation

        // 对于每种要输出的类型, 常见一个该类型的索引数
        var mapTypeIndex = new HashMap<String, AtomicInteger>();
        for(var entryType : refMapping.entry.entrySet())
        {
            var typeName = entryType.getKey();
            mapTypeIndex.put(typeName, new AtomicInteger(0));
        }

        // 遍历所有需要处理的异常或正常类型
        for(var entryType : refMapping.entry.entrySet())
        {
            var typeName = entryType.getKey();
            var typeContains = entryType.getValue();

            System.out.println("处理类型: " + typeName);

            // 计算所有类型应该输出到什么目录里
            var folderOutputImage = switch (typeName)
            {
                case "train.good" -> new File(folderOutput, "train/good");
                case "test.good" -> new File(folderOutput, "test/good");
                default -> new File(folderOutput, "test/" + typeName);
            };
            var folderOutputMask = switch (typeName)
            {
                case "train.good", "test.good" -> null;
                default -> new File(folderOutput, "ground_truth/" + typeName);
            };

            // 开始处理每张图片
            refCoco.entry.getImages().stream().parallel().forEach(imageInfo -> {
                var imageId = imageInfo.getId();
                var imageFilename = imageInfo.getFilename();
                var width = imageInfo.getWidth();
                var height = imageInfo.getHeight();
                System.out.println(STR."[\{typeName}] 处理数据集图片 [\{imageId}], [\{imageFilename}] (\{width}×\{height})");

                var fileImage = new File(folderImages, imageFilename);

                var isNeedOutputImage = true;
                var isNeedOutputMask = true;
                var shapeMask = new Ref<>(facGeo.createEmpty(2)); // mask 图形

                var listImageAnnotation = mapImageAnnotation.get(imageId);
                if(isEmpty(listImageAnnotation))
                {
                    System.out.println("图片不包含标注, 视为忽略样本");
                    isNeedOutputImage = false;
                    isNeedOutputMask = false;
                }
                else switch (typeName)
                {
                    case "train.good", "test.good" -> { // 判断是否只包含了声明的类型
                        isNeedOutputMask = false;
                        for(var anno : listImageAnnotation)
                        {
                            var categoryId = anno.getCategoryId();
                            var categoryName = mapCategoryId.get(categoryId);
                            if(!typeContains.contains(categoryName))
                            {
                                isNeedOutputImage = false;
                                break;
                            }
                        }
                    }
                    default -> { // 进行一个 mask 的计算
                        for(var anno : listImageAnnotation)
                        {
                            var categoryId = anno.getCategoryId();
                            var categoryName = mapCategoryId.get(categoryId);
                            var isCategoryContain = typeContains.contains(categoryName);
                            if(!isCategoryContain)
                            {
                                continue;
                            }

                            var listPolygonPart = new ArrayList<LinearRing>();
                            var seg = anno.getSegmentation();
                            for(var segPart : seg)
                            {
                                var countPoint = sizeOf(segPart) / 2;
                                var arrPoint = new Coordinate[countPoint + 1];
                                for(var stepPoint = 0; stepPoint < countPoint; stepPoint++)
                                {
                                    var x = segPart.get(stepPoint * 2);
                                    var y = segPart.get(stepPoint * 2 + 1);
                                    var coor = new Coordinate(x.intValue(), y.intValue());
                                    arrPoint[stepPoint] = coor;
                                }
                                arrPoint[countPoint] = arrPoint[0];

                                var lr = facGeo.createLinearRing(arrPoint);
                                listPolygonPart.add(lr);
                            }

                            var polygon = facGeo.createPolygon(
                                    listPolygonPart.get(0),
                                    Arrays.copyOfRange(listPolygonPart.toArray(new LinearRing[0]), 1, listPolygonPart.size())
                            );

                            shapeMask.entry = shapeMask.entry.union(polygon);
                        }
                        shapeMask.entry = shapeMask.entry.buffer(0);
                    }
                }

                // 将原图输出到指定目录
                //noinspection ConstantValue
                if(!isNeedOutputImage && !isNeedOutputMask)
                {
                    return;
                }

                var typeIndex = mapTypeIndex.get(typeName).getAndIncrement(); // 这个用来决定输出的文件名
                var indexDot = imageFilename.lastIndexOf('.');
                var imageFormat = indexDot < 0 ? "png" : imageFilename.substring(indexDot + 1);
                var filenameIndex = String.format("%03d", typeIndex);

                //noinspection PointlessBooleanExpression
                if(true || isNeedOutputImage) // 实际上这里肯定为 true, 那就手动加上 true 告诉编译器咯
                {
                    var fileOutputImage = new File(folderOutputImage, filenameIndex + ".png");
                    ((MayRunnable) () -> {
                        fileOutputImage.getParentFile().mkdirs();
                        fileOutputImage.createNewFile();
                    }).anyway(true).run();
                    if("png".equals(imageFormat.toLowerCase()))
                    {
                        try(var ifs = new FileInputStream(fileImage); var ofs = new FileOutputStream(fileOutputImage))
                        {
                            ifs.transferTo(ofs);
                            ofs.flush();
                        }
                        catch (Exception any)
                        {
                            throw new RuntimeException("复制图像出错", any);
                        }
                    }
                    else
                    {
                        try(var ofs = new FileOutputStream(fileOutputImage))
                        {
                            var image = ImageIO.read(fileImage);
                            ImageIO.write(image, "png", ofs);
                            ofs.flush();
                        }
                        catch (Exception any)
                        {
                            throw new RuntimeException("转换图像出错", any);
                        }
                    }
                }
                if(isNeedOutputMask)
                {
                    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    var fileOutputMask = new File(folderOutputMask, filenameIndex + "_mask.png");
                    ((MayRunnable) () -> {
                        fileOutputMask.getParentFile().mkdirs();
                        fileOutputMask.createNewFile();
                    }).anyway(true).run();
                    for(var stepWidth = 0; stepWidth < width; stepWidth++)
                    {
                        for(var stepHeight = 0; stepHeight < height; stepHeight++)
                        {
                            final int sw = stepWidth, sh = stepHeight;
                            var coor = new Coordinate(sw, sh);
                            Point point;
                            point = facGeo.createPoint(coor);

                            var isMasking = point.within(shapeMask.entry) || point.intersects(shapeMask.entry);

                            image.setRGB(sw, sh, isMasking ? CssColors.White : CssColors.Black);
                        }
                    }

//                    System.out.println(STR."[\{typeName}] 处理数据集图片 [\{imageId}], [\{imageFilename}] Mask 面积 [\{shapeMask.entry.getArea()}]");

                    ((MayRunnable) () -> {
                        ImageIO.write(image, "png", fileOutputMask);
                    }).anyway(true).run();
                }

                System.out.println(STR."[\{typeName}] 处理数据集图片 [\{imageId}], [\{imageFilename}] 完成");
            });
        };
    }
}
