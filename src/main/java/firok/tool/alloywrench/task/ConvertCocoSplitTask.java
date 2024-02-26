package firok.tool.alloywrench.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import firok.tool.alloywrench.bean.CocoData;
import firok.topaz.function.MayRunnable;
import firok.topaz.general.Collections;
import firok.topaz.resource.Files;
import firok.topaz.thread.Threads;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static firok.topaz.general.Collections.isNotEmpty;
import static firok.topaz.general.Collections.sizeOf;

/**
 * 把 COCO JSON 格式的数据集转换为以 TXT 为基础的老 COCO 格式
 * */
public class ConvertCocoSplitTask
{
    /**
     * 是否替换掉原本的文件名
     * */
    public static boolean shouldRename = true;
    public static void execute(String pathCocoJson, String pathCocoImages, String pathOutputFolder) throws Exception
    {
        var fileCocoJson = new File(pathCocoJson).getCanonicalFile();
        var folderInputImages = new File(pathCocoImages).getCanonicalFile();
        var folderOutput = new File(pathOutputFolder).getCanonicalFile();
        var folderOutputLabels = new File(folderOutput, "labels").getCanonicalFile();
        var folderOutputImages = new File(folderOutput, "images").getCanonicalFile();

        Files.assertExist(fileCocoJson, true, "COCO 文件不存在");
        Files.assertExist(folderInputImages, false, "图片文件夹不存在");
//        Files.assertExist(folderOutputLabels, false, "输出标注文件夹已存在");
//        Files.assertExist(folderOutputImages, false, "输出图片文件夹已存在");

        folderOutputLabels.mkdirs();
        folderOutputImages.mkdirs();

        var om = new ObjectMapper();
        var coco = om.readValue(fileCocoJson, CocoData.class);

        var fileOutputImageTxt = new File(folderOutput, "images.txt").getCanonicalFile();
        var mapImageAnno = Collections.mappingKeyMultiEntityList(
                coco.getAnnotations(),
                CocoData.Annotation::getImageId
        );

        var counter = new AtomicInteger(0);
        var threadObserver = Threads.start(true, () -> {
            while(true)
            {
                try
                {
                    Thread.sleep(2000);
                    var count = counter.get();
                    System.out.println("已处理图片数量: " + count);
                }
                catch (InterruptedException any)
                {
                    break;
                }
            }
        });

        try(var ofsImageTxt = new PrintStream(fileOutputImageTxt);
            var pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        )
        {
            for(var imageData : coco.getImages())
            {
                pool.submit(MayRunnable.that(() -> {
                    var imageId = imageData.getId();
                    var listImageAnno = mapImageAnno.get(imageId);
                    var imageFilename = imageData.getFilename(); // xx.jpg
                    var imageFilenameName = imageFilename.substring(0, imageFilename.lastIndexOf('.')); // xx
                    var imageFilenameSuffix = imageFilename.substring(imageFilename.lastIndexOf('.') + 1); // .jpg

                    if(shouldRename)
                    {
                        imageFilenameName = imageId + "";
                    }

                    // 将图片路径写入 TXT 索引
                    synchronized (ofsImageTxt)
                    {
                        ofsImageTxt.println("./images/" + imageFilename);
                    }

                    // 先将图片数据读入内存
                    var fileInputImage = new File(folderInputImages, imageFilename);
                    Files.assertExist(fileInputImage, true, "图片文件不存在");

                    var imageFilesize = fileInputImage.length();
                    if(imageFilesize > Integer.MAX_VALUE) throw new RuntimeException("图片文件过大");
                    var bufferImage = new byte[(int) imageFilesize];
                    try(var ifs = new FileInputStream(fileInputImage)) { ifs.read(bufferImage); }
                    catch (Exception any) { throw new RuntimeException("读取图片文件失败", any); }

                    // 解析图像数据, 获取长宽信息
                    BufferedImage imageInstance;
                    try(var ibs = new ByteArrayInputStream(bufferImage)) { imageInstance = ImageIO.read(ibs); }
                    catch (Exception any) { throw new RuntimeException("解析图片文件失败", any); }

                    var width = new BigDecimal(imageInstance.getWidth());
                    var height = new BigDecimal(imageInstance.getHeight());

                    // 写入标注数据
                    var fileOutputLabel = new File(folderOutputLabels, imageFilenameName + ".txt");

                    if(isNotEmpty(listImageAnno)) try(var ofs = new PrintStream(fileOutputLabel))
                    {
                        var countAnno = sizeOf(listImageAnno);
                        for(var stepAnno = 0; stepAnno < countAnno; stepAnno++)
                        {
                            var imageAnno = listImageAnno.get(stepAnno);

                            var cateId = imageAnno.getCategoryId();
                            ofs.print(cateId);
                            ofs.print(" ");
                            var listPtn = imageAnno.getSegmentation().get(0);
                            var countPtn = sizeOf(listPtn);
                            for(var stepPtn = 0; stepPtn < countPtn; stepPtn++)
                            {
                                var ptn = listPtn.get(stepPtn); // point number data
                                var ptnPercent = ptn.divide(stepPtn % 2 == 0 ? width : height, 6,  RoundingMode.HALF_UP);
                                ofs.print(ptnPercent);
                                if(stepPtn < countPtn - 1) ofs.print(" ");
                            }
                            ofs.println();
                        }
                        ofs.flush();
                    }

                    // 写入图像数据
                    var fileOutputImage = new File(folderOutputImages, imageFilename).getCanonicalFile();
                    try(var ofs = new FileOutputStream(fileOutputImage))
                    {
                        ofs.write(bufferImage);
                        ofs.flush();
                    }

                    counter.incrementAndGet();
                }).anyway());
            }
            pool.shutdown();
            var resultPool = pool.awaitTermination(Collections.sizeOf(coco.getImages()) * 2500L, TimeUnit.MILLISECONDS);
            if(!resultPool) throw new TimeoutException("线程池执行超时");
        }
        catch (Exception any)
        {
            System.err.println("转换数据集发生错误");
            any.printStackTrace(System.err);
        }
        finally
        {
            threadObserver.interrupt();
        }

        System.out.println("数据集转换完成, 共处理图片: " + counter.get());
    }
}
