package com.tanhua.admin.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.admin.mapper.LogMapper;
import com.tanhua.model.domain.Log;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author shkstart
 * @create 2023-07-21 16:31
 */
@Component
public class LogListener {
    @Autowired
    private LogMapper logMapper;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            value = "tanhua.log.queue",
                            declare = "true"
                    ),
                    exchange = @Exchange(
                            value = "tanhua.log.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = {"log.*"}
            )
    )
    public void log(String message) {
        try {
            Map<String, Object> map = JSON.parseObject(message);
            //1、获取数据
            Long userId = Long.valueOf(map.get("userId").toString());
            String date = (String) map.get("logTime");
//            String objId = (String) map.get("objId");
            String type = (String) map.get("type");
            //2、保存到数据库
            Log log = new Log(userId, date, type);
            logMapper.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
