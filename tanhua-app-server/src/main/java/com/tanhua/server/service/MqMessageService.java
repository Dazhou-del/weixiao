package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MqMessageService {

    @Autowired
    private AmqpTemplate amqpTemplate;

    //发送日志消息 type就是记录是什么状态 0101是登录 ，0102是注册  推荐的id 推荐动态就是pid 视频就是vid
    //发送的信息一般都是JSON类型的 所有要把map转化为JSON
    public void sendLogService(Long userId,String type,String key,String busId) {
        try {
            Map map = new HashMap();
            map.put("userId",userId.toString());
            map.put("type",type);
            map.put("logTime",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            map.put("busId",busId);
            String message = JSON.toJSONString(map);
            amqpTemplate.convertAndSend("tanhua.log.exchange",
                    "log."+key,message);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    //发送动态审核消息
    public void sendAudiService(String movementId) {
        try {
            amqpTemplate.convertAndSend("tanhua.audit.exchange",
                    "audit.movement",movementId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }
}