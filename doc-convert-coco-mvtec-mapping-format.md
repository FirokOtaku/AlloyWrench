# COCO 数据集转换 MVTec 数据集 - 映射文件格式

需要分别指定各类别的处理方式.

```json
{
  "train.good": [ "water-clean", "water-splashing" ],
  "test.good": [ "water-clean", "water-splashing" ],

  "dirty": [ "water-dirty" ],
  "muddy": [ "water-muddy" ],
  ...
}
```

* `train.good` 字段
  * **必要字段**
  * 一张图片若包含 `train.good` 具有的任一类型标注, 则该图片视为正样本
  * 一张图片若不包含 `train.good` 具有的任一类型标注, 则该图片将会被忽略, 不会输出到目标文件夹
* `test.good` 字段
  * 字段处理方式同 `train.good` 字段
* 对于所有其它字段, 会计算图片内相关标注范围的 **并集** 作为该异常类型 mask,
  原图片将会输出到 `test/{字段名}` 目录,
  mask 图片将会输出到 `ground_truth/{字段名}` 目录

示例:

* _图片1_ 不包含任何标注, _图片2_ 包含标注 `A`, _图片3_ 包含标注 `A, B`
* 当映射文件声明 `train.good` 为 `[ "A" ]`
  * _图片1_ 将会视为 _忽略样本_, 
  * _图片2_ 将会视为正样本, 
  * _图片3_ 将会被视为 _B 类负样本_
