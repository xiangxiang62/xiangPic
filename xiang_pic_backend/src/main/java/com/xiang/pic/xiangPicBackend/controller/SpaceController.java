package com.xiang.pic.xiangPicBackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiang.pic.xiangPicBackend.annotation.AuthCheck;
import com.xiang.pic.xiangPicBackend.common.BaseResponse;
import com.xiang.pic.xiangPicBackend.common.DeleteRequest;
import com.xiang.pic.xiangPicBackend.common.ResultUtils;
import com.xiang.pic.xiangPicBackend.constant.UserConstant;
import com.xiang.pic.xiangPicBackend.exception.BusinessException;
import com.xiang.pic.xiangPicBackend.exception.ErrorCode;
import com.xiang.pic.xiangPicBackend.exception.ThrowUtils;
import com.xiang.pic.xiangPicBackend.model.domain.Picture;
import com.xiang.pic.xiangPicBackend.model.domain.Space;
import com.xiang.pic.xiangPicBackend.model.domain.User;
import com.xiang.pic.xiangPicBackend.model.dto.picture.*;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceUpdateRequest;
import com.xiang.pic.xiangPicBackend.model.enums.PictureReviewStatusEnum;
import com.xiang.pic.xiangPicBackend.model.vo.picture.PictureTagCategory;
import com.xiang.pic.xiangPicBackend.model.vo.picture.PictureVO;
import com.xiang.pic.xiangPicBackend.service.PictureService;
import com.xiang.pic.xiangPicBackend.service.SpaceService;
import com.xiang.pic.xiangPicBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;


    /**
     * 更新空间（仅管理员可用）
     *
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


}
