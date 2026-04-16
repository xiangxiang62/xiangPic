package com.xiang.pic.xiangPicBackend.service;

import com.xiang.pic.xiangPicBackend.model.domain.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiang.pic.xiangPicBackend.model.domain.User;
import com.xiang.pic.xiangPicBackend.model.dto.space.SpaceAddRequest;

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
}
