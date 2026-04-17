package com.xiang.pic.xiangPicBackend.api.imagesearch.model;

import lombok.Data;

/**
 * 图片查询结果
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}
