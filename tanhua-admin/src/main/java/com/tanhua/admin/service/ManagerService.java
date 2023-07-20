package com.tanhua.admin.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private MovementApi movementApi;
    @DubboReference
    private CommentApi commentApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public PageResult findAllUsers(Integer page, Integer pagesize) {
        IPage<UserInfo> iPage = userInfoApi.findAll(page, pagesize);
        List<UserInfo> list = iPage.getRecords();
        for (UserInfo userInfo : list) {
            String key= Constants.USER_FREEZE+userInfo.getId();
            if (redisTemplate.hasKey(key)){
                userInfo.setUserStatus("2");
            }
        }
        return new PageResult(page, pagesize, iPage.getTotal(), iPage.getRecords());
    }

    //根据id查询
    public UserInfo findUserById(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        //查看redis中数据是否存在，存在则冻结了 手动设置值
        String key= Constants.USER_FREEZE+userId;
        if (redisTemplate.hasKey(key)){
            userInfo.setUserStatus("2");
        }
        return userInfo;
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
    //根据id查询movemen对象
    public MovementsVo findXingqing(String movenId) {
        Movement movement = movementApi.findById(movenId);
        UserInfo userInfo = userInfoApi.findById(movement.getUserId());
        MovementsVo movementsVo=MovementsVo.init(userInfo,movement);
        return movementsVo;
    }

    /**
     * 分页查询评论
     * @param page
     * @param pagesize
     * @param publishID
     * @return
     */
    public PageResult findComments(Integer page, Integer pagesize, String publishID) {
            //分页查询评论 根据messageID
        PageResult pr = commentApi.findCommentslit(publishID, CommentType.COMMENT, page, pagesize);
        List<Comment> items = (List<Comment>)pr.getItems();
        //根据评论列表 获取评论者的id列表
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
//        根据ids 查出userinfo
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
//        构建对象返回
        List<CommentVo> vos=new ArrayList<>();
        for (Comment item : items) {
            UserInfo userInfo = map.get(item.getUserId());
            if (userInfo!=null){
                CommentVo vo=CommentVo.init(userInfo,item);
                vos.add(vo);
            }
        }
        pr.setItems(vos);
        return pr;
    }

    /**
     * 用户冻结
     * map中有前端传来的参数
     * userId 用户id
     * freezingTime  冻结时间，1为冻结3天，2为冻结7天，3为永久冻结
     * freezingRange 冻结范围，1为冻结登录，2为冻结发言，3为冻结发布动态
     * reasonsForFreezing 冻结原因
     * frozenRemarks 冻结备注
     * @param params
     * @return
     */
    public Map userFreeze(Map params) {
        //构建key
        String userId = params.get("userId").toString();
        String key= Constants.USER_FREEZE+userId;
        //判断输入的冻结时间 1为冻结3天，2为冻结7天，3为永久冻结
        Integer freezingTime = Integer.valueOf(params.get("freezingTime").toString());
        int days=0;
        if(freezingTime==1){
            days=3;
        }else if(freezingTime==2){
            days=7;
        }
        String value = JSON.toJSONString(params);
        if(days>0){
            redisTemplate.opsForValue().set(key,value,days, TimeUnit.MINUTES);
        }else {
            redisTemplate.opsForValue().set(key,value);
        }
        Map map = new HashMap();
        map.put("message","冻结成功");
        return map;
    }

    /**
     * 用户解冻
     * @param params
     * @return
     */
    public Map userUnfreeze(Map params) {
        //构建key
        String userId = params.get("userId").toString();
        String key= Constants.USER_FREEZE+userId;
        //删掉key redis
        redisTemplate.delete(key);
        Map map = new HashMap();
        map.put("message","冻结成功");
        return map;
    }
}