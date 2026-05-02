package com.xiang.pic.xiangPicBackend.constant;


/**
 * 图片常量类
 */
public interface PictureConstant {

    /**
     * AI 扩图次数上限
     */
    Integer OUT_PAINTING_DAILY_LIMIT = 3;

    /**
     * AI 扩图次数上限缓存 key
     */
    String OUT_PAINTING_DAILY_LIMIT_CACHE_KEY = "ai_out_painting:limit:%s:%s";

    /**
     * AI 扩图状态 - 正在扩图中
     */
    String OUT_PAINTING_RUNNING = "RUNNING";

    /**
     * AI 扩图状态 - 扩图失败
     */
    String OUT_PAINTING_FAILED = "FAILED";

    /**
     * AI 扩图状态 - 扩图成功
     */
    String OUT_PAINTING_SUCCEEDED = "SUCCEEDED";

}
