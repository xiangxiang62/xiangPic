package com.xiang.pic.xiangPicBackend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间等级
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 值
     */
    private int value;

    /**
     * 文本
     */
    private String text;

    /**
     * 最大图片总数量
     */
    private long maxCount;

    /**
     * 最大图片总大小
     */
    private long maxSize;
}
