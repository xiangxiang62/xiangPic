package com.xiang.pic.xiangPicBackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiang.pic.xiangPicBackend.model.domain.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiang.pic.xiangPicBackend.model.domain.User;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceAddRequest;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceEditRequest;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceQueryRequest;
import com.xiang.pic.xiangPicBackend.model.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 19643
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-04-16 14:13:00
*/
public interface SpaceService extends IService<Space> {


    /**
     * 添加个人空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 根据空间级别，自动填充限额
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间
     *
     * @param space 空间
     * @param add 创建时校验 or 编辑时校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间视图
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间视图分页
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 编辑空间信息
     *
     * @param spaceEditRequest
     * @param loginUser
     */
    void editSpace(SpaceEditRequest spaceEditRequest, User loginUser);

    /**
     * 删除空间
     *
     * @param id
     * @param loginUser
     */
    void deleteSpace(long id, User loginUser);
}
