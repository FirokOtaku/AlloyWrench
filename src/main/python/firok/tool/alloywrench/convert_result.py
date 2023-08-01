# since Alloy Wrench 0.30.0

# 当前脚本面向 MMDetection 3,
# 早先版本脚本请在 git 历史查看.
# Current script is for MMDetection3.
# Older version could be found in git history.


from matplotlib.patches import Polygon
import torch
import numpy


# 把各种乱七八糟东西转化成"能转换成JSON"的玩意
# 肯定有更好的办法
# 然而并没兴趣学那么多 Python
# 又不是不能用
def convert(target):
    type_target = type(target)
    if type_target == tuple or type_target == list:
        ret = []
        for tuple_child in target:
            temp_child = convert(tuple_child)
            ret.append(temp_child)
        return ret
    elif type_target == numpy.ndarray:
        ret = []
        for tuple_child in target.tolist():
            temp_child = convert(tuple_child)
            ret.append(temp_child)
        return ret
    elif type_target == dict:
        ret = {}
        for (key, value) in target.items():
            ret[key] = convert(value)
        return ret
    elif type_target == Polygon:
        return convert(target.xy)
    elif type_target == torch.Tensor:
        if target.dim() == 0:
            return target.item()
        ret = []
        for list_child in target:
            ret.append(convert(list_child))
        return ret
    else:
        return target


# 把MMDetection探测到的实例数据转化成坐标点数据
# 另外还附带一大堆别的玩意
def convert_polygon(result, with_bboxes=True, with_masks=True):
    ret = []
    pred_instances = result.pred_instances
    for pred_instance in pred_instances:
        ret.append({'labels': convert(pred_instance.labels),
                    'bboxes': convert(pred_instance.bboxes) if with_bboxes else None,
                    'masks': convert(pred_instance.masks) if with_masks else None,
                    'scores': convert(pred_instance.scores)})
    return ret
