# Alloy Wrench ![wrench](wrench.svg)

一些 ~~研究~~ 折腾机器视觉框架时候用到的 ~~工具~~ 轮子代码.

代码基于 **Java 17**, 以 MIT 协议开源.

> _为什么用 Java?_  
> 因为主业是写 Java 的, 写着习惯
> 
> _<del><sub>为什么不用 Java 8?!</sub></del> 为什么是 Java 17?_  
> <del><sub>因为相信的心就是你的魔法</sub></del> 因为乐意
>
> _这一切值得吗?_  
> 能用就行, 哪来那么多毛病
> 
> _图标?_  
> 原图为 [工业时代 - 扳手](https://wiki.industrial-craft.net/index.php?title=Wrench) 的材质

## 用法

```bash
# 神说 要有 jar
mvn package

# 帮助信息
java -jar alloy-wrench-jar-with-dependencies.jar

# 收集 DOTA 数据标签文件里面的标签目标类型
# 整理成一个 json
java -jar alloy-wrench-jar-with-dependencies.jar ^
 collect dota ^
 ".\playground\mapping.json" ^
 ".\dota-1.0\train\labelTxt" ^
 ".\dota-1.0\val\labelTxt"

# 将 DOTA 数据标签文件转换成 YOLO 格式的
# 需要用到上面读取创建的映射 json
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

## changelog

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
