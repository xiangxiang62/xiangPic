package com.xiang.pic.xiangPicBackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiang.pic.xiangPicBackend.annotation.AuthCheck;
import com.xiang.pic.xiangPicBackend.common.BaseResponse;
import com.xiang.pic.xiangPicBackend.common.DeleteRequest;
import com.xiang.pic.xiangPicBackend.common.ResultUtils;
import com.xiang.pic.xiangPicBackend.constant.UserConstant;
import com.xiang.pic.xiangPicBackend.exception.BusinessException;
import com.xiang.pic.xiangPicBackend.exception.ErrorCode;
import com.xiang.pic.xiangPicBackend.exception.ThrowUtils;
import com.xiang.pic.xiangPicBackend.manager.auth.SpaceUserAuthManager;
import com.xiang.pic.xiangPicBackend.model.domain.Picture;
import com.xiang.pic.xiangPicBackend.model.domain.Space;
import com.xiang.pic.xiangPicBackend.model.domain.User;
import com.xiang.pic.xiangPicBackend.model.dto.picture.*;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceAddRequest;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceEditRequest;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceQueryRequest;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceUpdateRequest;
import com.xiang.pic.xiangPicBackend.model.dto.user.UserAddRequest;
import com.xiang.pic.xiangPicBackend.model.enums.SpaceLevelEnum;
import com.xiang.pic.xiangPicBackend.model.vo.space.SpaceLevel;
import com.xiang.pic.xiangPicBackend.model.vo.space.SpaceVO;
import com.xiang.pic.xiangPicBackend.service.SpaceService;
import com.xiang.pic.xiangPicBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space picture = spaceService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 创建空间
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long result = spaceService.addSpace(spaceAddRequest,loginUser);
        return ResultUtils.success(result);

    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        // 获取封装类
        return ResultUtils.success(spaceVO);
    }



    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> picturePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest pictureQueryRequest,
                                                         HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }



    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        spaceService.editSpace(spaceEditRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 删除空间
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        spaceService.deleteSpace(id, loginUser);
        return ResultUtils.success(true);
    }




    /**
     * 空间等级列表
     *
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

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
