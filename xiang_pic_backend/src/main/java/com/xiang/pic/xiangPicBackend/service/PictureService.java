package com.xiang.pic.xiangPicBackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiang.pic.xiangPicBackend.model.domain.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiang.pic.xiangPicBackend.model.domain.User;
import com.xiang.pic.xiangPicBackend.model.dto.picture.PictureQueryRequest;
import com.xiang.pic.xiangPicBackend.model.dto.picture.PictureReviewRequest;
import com.xiang.pic.xiangPicBackend.model.dto.picture.PictureUploadByBatchRequest;
import com.xiang.pic.xiangPicBackend.model.dto.picture.PictureUploadRequest;
import com.xiang.pic.xiangPicBackend.model.vo.picture.PictureVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 19643
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-04-13 10:20:45
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传或更新图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 构造查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片分页
     *
     * @param picturePage
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    Page<PictureVO> getPicturePage(Page<Picture> picturePage,
                                   PictureQueryRequest pictureQueryRequest,
                                   HttpServletRequest request);

    /**
     * 获取图片视图（用户使用）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片视图分页（用户使用）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 参数校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充图片审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );


    /**
     * 清理图片文件（COS）
     * @param oldPicture
     */
    @Async
    void clearPictureFile(Picture oldPicture);
}
