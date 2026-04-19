package com.xiang.pic.xiangPicBackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiang.pic.xiangPicBackend.model.domain.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiang.pic.xiangPicBackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.xiang.pic.xiangPicBackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.xiang.pic.xiangPicBackend.model.vo.spaceuser.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 19643
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-04-19 21:45:50
*/
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 新增空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员
     *
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 构造查询条件
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 对象转封装类
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 对象列表转封装类列表
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
