package com.tanhua.admin.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ManagerService {

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private MovementApi movementApi;

    public PageResult findAllUsers(Integer page, Integer pagesize) {
        IPage iPage = userInfoApi.findAll(page, pagesize);
        return new PageResult(page, pagesize, iPage.getTotal(), iPage.getRecords());
    }

    //根据id查询
    public UserInfo findUserById(Long userId) {
        return userInfoApi.findById(userId);
    }

    //查询指定用户发布的所有视频列表
    public PageResult findAllVideos(Integer page, Integer pagesize, Long uid) {
        return videoApi.findByUserId(page, pagesize, uid);
    }

    //查询动态
    public PageResult findAllMovements(Integer page, Integer pagesize, Long uid, Integer state) {
        //1、调用API查询数据 ：Movment对象
        PageResult result = movementApi.findByUserId(uid,state,page,pagesize);
        //2、解析PageResult，获取Movment对象列表
        List<Movement> items = (List<Movement>) result.getItems();
        //3、一个Movment对象转化为一个Vo
        if(CollUtil.isEmpty(items)) {
            return new PageResult();
        }
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : items) {
            UserInfo userInfo = map.get(movement.getUserId());
            if(userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, movement);
                vos.add(vo);
            }
        }
        //4、构造返回值
        result.setItems(vos);
        return result;
    }
}