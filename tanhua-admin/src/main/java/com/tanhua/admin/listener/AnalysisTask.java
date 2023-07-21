package com.tanhua.admin.listener;

import com.tanhua.admin.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author shkstart
 * @create 2023-07-21 17:25
 */
@Component
public class AnalysisTask {
    @Autowired
    private AnalysisService analysisService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void analysis() throws ParseException {
        //业务逻辑
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("开始统计："+time);
        //调logService完成日志统计
        analysisService.analysis();
        System.out.println("结束统计");
    }

}
