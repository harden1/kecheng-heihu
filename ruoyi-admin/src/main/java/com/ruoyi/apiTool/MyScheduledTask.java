package com.ruoyi.apiTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MyScheduledTask {
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private ApiUserInfoForBlackLack apiUserInfoForBlackLack;

    // 每小时执行一次（整点开始）
    @Scheduled(cron = "0/50 * * * * ?")
    public void executeTask() {
        System.out.println("执行任务时间：" + java.time.LocalDateTime.now());
        //调用token接口
        String accessToken = accessTokenService.getAccessToken(true);
        System.out.println("accessToken: " + accessToken);
        //测试userinfo
//        apiUserInfoForBlackLack.getUserApiForBlacklack();
        //调用订单二维码数据，查询订单

    }

}