package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.utils.Constants;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author shkstart
 * @create 2023-07-20 23:08
 */
@Service
public class UserFreezeService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 检查是否被冻结
     * @param state  状态 1 就是不能登录 ，2为冻结发言，3为冻结发布动态
     * @param userId 用户id
     */
    public void checkUserStatus(String state,Long userId) {
        //构建key 获取redis中的值
        String value = redisTemplate.opsForValue().get(Constants.USER_FREEZE + userId);
        //判断是否为空
        if(!StringUtils.isEmpty(value)) {
            //不为空则把value 转化为map对象 这里的value是JSON格式的字符串
            Map map = JSON.parseObject(value, Map.class);
            //判断里面的值 与传入的值进行比较
            String freezingRange = (String) map.get("freezingRange");
            if(state.equals(freezingRange)) {
                //如果一样则抛出错误说 这个账号被冻结了
                throw new BusinessException(ErrorResult.builder().errMessage("您的账号被冻结！").build());
            }
        }
    }
}
