# Alloy Wrench ![wrench](wrench.svg)

一些研究机器视觉框架时候用到的工具代码.

代码基于 **Java 17**, 以 MIT 协议开源.

> _为什么用 Java?_  
> 因为主业是写 Java 的, 写着习惯
>
> _这一切值得吗?_  
> 能用就行, 哪来那么多毛病
> 
> _图标?_  
> 原图为 [工业时代 - 扳手](https://wiki.industrial-craft.net/index.php?title=Wrench) 的材质

## 示例用法

```bash
# 收集 DOTA 数据标签文件里面的标签目标类型, 整理成一个 json
java -jar alloy-wrench-jar-with-dependencies.jar ^
 collect dota ^
 ".\playground\mapping.json" ^
 ".\dota-1.0\train\labelTxt" ^
 ".\dota-1.0\val\labelTxt"

# 将 DOTA 数据标签文件转换成 YOLO 格式的
java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert dota yolo ^ 
 ".\dota-1.0\val\labelTxt" ^
 ".\dota-1.0\val\images" ^
 ".\playground\mapping.json" ^
 ".\playground\val-labels"

java -jar alloy-wrench-jar-with-dependencies.jar ^
 convert dota yolo ^
 ".\dota-1.0\train\labelTxt" ^
 ".\dota-1.0\train\images" ^
 ".\playground\mapping.json" ^
 ".\playground\train-labels"
```

## 相关链接

* [DOTA 数据集](https://captain-whu.github.io/DOTA/index.html)
* [YOLO 框架](https://github.com/ultralytics/yolov5)

