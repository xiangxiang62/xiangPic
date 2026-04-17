package com.xiang.pic.xiangPicBackend.api.imagesearch;

import com.xiang.pic.xiangPicBackend.api.imagesearch.model.ImageSearchResult;
import com.xiang.pic.xiangPicBackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.xiang.pic.xiangPicBackend.api.imagesearch.sub.GetImageListApi;
import com.xiang.pic.xiangPicBackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://pic4.zhimg.com/v2-c34c61a90095abb8713de9d1dca7ec7b_r.jpg";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
