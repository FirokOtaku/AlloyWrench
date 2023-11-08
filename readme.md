# Alloy Wrench ![wrench](wrench.svg)

一些 ~~研究~~ 折腾机器视觉框架时候用到的 ~~工具~~ 轮子代码.

代码基于 ~~**Java 17**~~ **Java 21**, 依赖 [Topaz 库](https://github.com/FirokOtaku/Topaz), 以 MIT 协议开源.

> _为什么用 Java?_  
> 因为主业是写 Java 的, 写着习惯
> 
> _<del><sub>为什么不用 Java 8?!</sub></del> <del>为什么是 Java 17?</del> 为什么是 Java 21?_  
> <del><sub>因为相信的心就是你的魔法哟~</sub></del> 因为乐意
>
> _这一切值得吗?_  
> 能用就行, 哪来那么多毛病
> 
> _图标?_  
> 原图为 [工业时代 - 扳手](https://wiki.industrial-craft.net/index.php?title=Wrench) 的材质 <sub>吱嘎吱嘎吱嘎 🔈</sub>

## 用法

```bash
# 神说 要有 jar
# 出于未知且知道也懒得修复的问题 现在打包出来的玩意不能用
# 反正在 IDE 里直接用也不是不能用
# 建议你也装一套开发环境
mvn package

# 帮助信息
# 下面所有的例子都是以前写的 参数列表的顺序可能发生过变化
# 建议有问题之前先不带参数直接执行一下 打印出来的帮助信息应该够详细了
java -jar alloy-wrench-jar-with-dependencies.jar

# 收集 DOTA 数据标签文件里面的标签目标类型
# 整理成一个 json
java -jar alloy-wrench-jar-with-dependencies.jar ^
 collect dota ^
 "./playground/mapping.json" ^
 "./dota-1.0/train/labelTxt" ^
 "./dota-1.0/val/labelTxt"

# 将 DOTA 数据标签文件转换成 YOLO 格式的
# 需要用到上面读取创建的映射 json
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert dota yolo ^ 
 "./dota-1.0/val/labelTxt" ^
 "./dota-1.0/val/images" ^
 "./playground/mapping.json" ^
 "./playground/val-labels"

java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert dota yolo ^
 "./dota-1.0/train/labelTxt" ^
 "./dota-1.0/train/images" ^
 "./playground/mapping.json" ^
 "./playground/train-labels"

# 将 COCO 数据集转换为 DOTA 格式
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert coco dota ^
 "./coco-label.json" ^
 "./coco-2-dota-label/"

# 将 COCO 数据集转换为 MVTec 格式
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert coco mvtec ^
 "./coco-dataset/images" ^
 "./coco-dataset/coco-label.json" ^
 "./playground/mapping.json" ^
 "./playground/output-dataset"

# 将 labelme 创建的 JSON 数据文件转换为 COCO 格式的
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert labelme coco ^
 "./labelme-labels.json" ^
 "./output-coco-labels.json"

# 将 labelme 创建的 JSON 数据文件转换为 YOLO 格式的
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert labelme yolo ^
 "./labelme-labels.json" ^
 "./yolo-labels.txt" ^
 "./mapping.json"

# 将 labelme 创建的 JSON 数据文件转换为 DOTA 格式的
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert labelme dota ^
 "./labelme-labels.json" ^
 "./dota-labels.txt"

# 直接以矩形切割图片
java -jar alloy-wrench-jar-with-dependencies.jar ^
 cut block direct ^
 "./input-image.png" ^
 "./output-images/" ^
 1600 1600 0 0

# 切分图片和相关 DOTA 标签数据
# 如果只希望输出切片图片或切片标签
# 需要手动修改代码里的 CutImageTask.OUTPUT_CUT_IMAGE 和 CutImageTask.OUTPUT_CUT_LABEL 值
# 如果需要调整输出图片的大小等参数
# 需要手动修改代码里的 CutImageTask.OVERLYING_X 和 CutImageTask.PIECE_X 等参数
# 为什么用这种方式 因为懒得从代码接收参数了
java -jar alloy-wrench-jar-with-dependencies.jar ^
 cut block dota ^
 "./raw.png" ^
 "./raw.txt" ^
 "./sub-images" ^
 "./sub-labels"

# 把单一的 COCO 标签图按照矩形范围切割成小图 COCO 数据集
java -jar alloy-wrench-jar-with-dependencies.jar ^
 cut block coco ^
 "./coco-label.json" ^
 "./coco-images/" ^
 "./coco-label-cut.json" ^
 "./coco-images-cut/" ^
 "coco-image-cut-"

# 切分图片和相关 COCO 标签数据
# 会把图片里的每个实例单独切分成一张图片
java -jar alloy-wrench-jar-with-dependencie.jar ^
 cut block coco ^
 "./super-big/images/" ^
 "./super-big.json" ^
 "./super-big-pieces/" ^
 "./super-big-pieces.json"

# 合并两个 COCO 标签文件
java -jar alloy-wrench-jar-with-dependencies.jar ^
 merge coco multi ^
 "./coco-label-output.json"
 "./coco-label-1.json" ^
 "./coco-label-2.json" ^

# 合并 COCO 标签文件中的种类
# 比如把 "板房" "砖房" 统一合并成 "房屋"
java -jar alloy-wrench-jar-with-dependencies.jar ^
 merge coco category
 "./label-input.json" ^
 "./label-output.json" ^
 "./mapping.json"

# 过滤 COCO 标签文件的内容
java -jar alloy-wrench-jar-with-dependencies.jar ^
 filter coco ^
 "./coco-label-input.json" "./coco-label-output.json" ^
 --filter-anno-id 1 2 3 ^
 --filter-image-id 1 2 3 ^
 --filter-category-id 1 2 3

# 启动数据标签可视化工具
java -jar alloy-wrench-jar-with-dependencies.jar ^
 renderer dota

# 启动数据标记工具
java -jar alloy-wrench-jar-with-dependencies.jar ^
 marker
```

* `convert coco mvtec` [COCO 数据集转换 MVTec 数据集 - 映射文件格式](doc-convert-coco-mvtec-mapping-format.md)

## 相关链接

* [DOTA 数据集](https://captain-whu.github.io/DOTA/index.html)
* [YOLO 框架](https://github.com/ultralytics/yolov5)
* [labelme 工具](https://github.com/wkentaro/labelme)
* [MMDetection 框架](https://github.com/open-mmlab/mmdetection)
* [MMDetection 框架官网](https://mmdetection.readthedocs.io/zh_CN/latest/)
* [JTS 库](https://github.com/locationtech/jts)
* [MVTec 数据集](https://www.mvtec.com/company/research/datasets/mvtec-ad)

## changelog

* 0.34.0
  * fix a logic error when converting coco dataset to mvtec dataset
* 0.33.0
  * add convert from coco dataset to mvtec dataset
  * **JDK dependency upgrade to 21 from 17**
* 0.32.0
  * fix an inference result conversion error
* 0.31.0
  * add [`imantics` dependency](https://github.com/jsbroks/imantics/tree/master)
    for converting inference results 
* 0.30.0
  * add more params to convert_result script
* 0.29.0
  * update convert_result script for MMDetection3 compatibility
* 0.28.0
  * adjust location of Python script and maven compiling config
* 0.27.0
  * update dependency
* 0.26.0
  * fix a coor calculation error
  * fix coco json read error
* 0.25.0
  * fix a coor calculation error
  * provide `CutImageDirectTask` api
* 0.24.0
  * fix border coor calculation error
* 0.23.0
  * coco label merging tool now can deal with multiple files at once
* 0.22.0
  * add tool for merging categories in coco label
* 0.21.0
  * add http server for converting script json to geojson (internal usage)
* 0.20.0
  * add convert from geojson to coco label (internal usage)
  * filtering coco label tool now can be used for remove unused images from label file
* 0.19.0
  * add tool for filtering coco label content
* 0.18.0
  * add tool for merging two coco label files
* 0.17.0
  * add convert from labelme label to coco label
* 0.16.0
  * add image cutter tool (cut into rectangle shape directly)
* 0.15.0
  * add a [python script](/src/main/python/convert_result.py) to convert results of mmdetection
    and get coordinates from it
  * now renderer tool could read json data converted
    from script mentioned above
* 0.14.0
  * add image cutter tool (cut by coco format dataset)
* 0.13.0
  * add convert from coco label to dota label
* 0.12.0
  * add image cutter tool
* 0.11.0
  * update label marker (unfinished)
* 0.10.0
  * update label marker (unfinished)
* 0.9.0
  * DOTA label renderer now will add points to indicate
    where the polygon starts
  * minor code improvement
  * add label marker (unfinished)
* 0.8.0
  * now we could calculate overlaps
    between labels and cutting area
    when cutting images
* 0.7.0
  * add image-and-labels cutter
* 0.6.0
  * add convert from _labelme_ json to yolo text
  * minor code improvement
  * add Java module support
* 0.5.0
  * reformat help information
* 0.4.0
  * add label viewer GUI
  * minor code improvement
* 0.3.0
  * print more log information
  * update readme
* 0.2.0
  * error fixes
* 0.1.0
  * init commit

## gossip

* DOTA 集里有些打点在图片外, 非常真实
* DOTA 集的标签文件换行符是 `/r/n` 哟
* 突然发现 JavaFX 当 Swing 写也可以嘛
* native-image + JavaFX + `--no-fallback` = **BOOM**
* native-image + Swing + `--no-fallback` = **BOOM**
* 要是某些项目的文档全一点, 这里的轮子至少少一半
* 自己写的 [库](https://github.com/FirokOtaku/Topaz) 肯定是要狠狠地用; 建议你们也用
* 这玩意真的有别的人会用吗? 也许吧
* 查看 COCO 数据集的功能在 [这里](https://github.com/FirokOtaku/TinWrench)
