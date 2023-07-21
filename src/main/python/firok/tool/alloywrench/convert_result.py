from matplotlib.patches import Polygon


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
    else:
        return target


# 把MMDetection探测到的实例数据转化成坐标点数据
# 另外还附带一大堆别的玩意
def convert_polygon(
        result,
        score_thr=0.3,

):
    import numpy as np
    import torch
    import cv2

    ms_bbox_result, ms_segm_result = result
    if isinstance(ms_bbox_result, dict):
        result = (ms_bbox_result['ensemble'],
                  ms_segm_result['ensemble'])

    if isinstance(result, tuple):
        bbox_result, segm_result = result
        if isinstance(segm_result, tuple):
            segm_result = segm_result[0]  # ms rcnn
    else:
        bbox_result, segm_result = result, None
    bboxes = np.vstack(bbox_result)
    labels = [
        np.full(bbox.shape[0], i, dtype=np.int32)
        for i, bbox in enumerate(bbox_result)
    ]
    labels = np.concatenate(labels)
    # draw segmentation masks
    segms = segm_result
    if segm_result is not None and len(labels) > 0:  # non empty
        segms = mmcv.concat_list(segm_result)
        if isinstance(segms[0], torch.Tensor):
            segms = torch.stack(segms, dim=0).detach().cpu().numpy()
        else:
            segms = np.stack(segms, axis=0)

    assert bboxes is None or bboxes.ndim == 2, \
        f' bboxes ndim should be 2, but its ndim is {bboxes.ndim}.'
    assert labels.ndim == 1, \
        f' labels ndim should be 1, but its ndim is {labels.ndim}.'
    assert bboxes is None or bboxes.shape[1] == 4 or bboxes.shape[1] == 5, \
        f' bboxes.shape[1] should be 4 or 5, but its {bboxes.shape[1]}.'
    assert bboxes is None or bboxes.shape[0] <= labels.shape[0], \
        'labels.shape[0] should not be less than bboxes.shape[0].'
    assert segms is None or segms.shape[0] == labels.shape[0], \
        'segms.shape[0] and labels.shape[0] should have the same length.'
    assert segms is not None or bboxes is not None, \
        'segms and bboxes should not be None at the same time.'

    if score_thr > 0:
        assert bboxes is not None and bboxes.shape[1] == 5
        scores = bboxes[:, -1]
        inds = scores > score_thr
        bboxes = bboxes[inds, :]
        labels = labels[inds]
        if segms is not None:
            segms = segms[inds, ...]

    num_bboxes = 0
    ret_label = None
    ret_bbox = None
    ret_polygon = None
    ret_area = None
    ret_position = None
    ret_mask = None
    if bboxes is not None:
        num_bboxes = bboxes.shape[0]
        ret_bbox = bboxes
        ret_polygon = []
        for i, bbox in enumerate(bboxes):
            bbox_int = bbox.astype(np.int32)
            poly = [[bbox_int[0], bbox_int[1]], [bbox_int[0], bbox_int[3]],
                    [bbox_int[2], bbox_int[3]], [bbox_int[2], bbox_int[1]]]
            np_poly = np.array(poly).reshape((4, 2))
            ret_polygon.append(Polygon(np_poly))
        ret_label = labels[:num_bboxes]

    if segms is not None:
        ret_mask = []
        for i, mask in enumerate(segms):
            temp_mask = []
            from mmdet.core.mask.structures import bitmap_to_polygon
            contours, _ = bitmap_to_polygon(mask)
            temp_mask += [Polygon(c) for c in contours]
            ret_mask.append(temp_mask)

        if num_bboxes < segms.shape[0]:
            segms = segms[num_bboxes:]
            areas = []
            positions = []
            for mask in segms:
                _, _, stats, centroids = cv2.connectedComponentsWithStats(
                    mask.astype(np.uint8), connectivity=8)
                largest_id = np.argmax(stats[1:, -1]) + 1
                positions.append(centroids[largest_id])
                areas.append(stats[largest_id, -1])
            areas = np.stack(areas, axis=0)
            ret_area = areas
            ret_position = positions

    return {'labels': ret_label,
            'bboxes': ret_bbox,
            'polygons': ret_polygon,
            'areas': ret_area,
            'positions': ret_position,
            'masks': ret_mask}
