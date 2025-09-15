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
    @Autowired
    private ApiTaskForBlackLack apiTaskForBlackLack;
    @Autowired
    private ApiWareHouseDetail  apiWareHouseDetail;

    // 启动后立即执行一次，以后在上一次任务执行完成后，等待 1 小时再执行
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void executeTask() {
        System.out.println("执行任务时间：" + java.time.LocalDateTime.now());
        //调用token接口
        String accessToken = accessTokenService.getAccessToken(false);
        System.out.println("accessToken: " + accessToken);
        //测试userinfo
//        apiUserInfoForBlackLack.getUserApiForBlacklack();
        //调用订单二维码数据，查询订单
        //测试task接口
        //apiTaskForBlackLack.getTackForBlackLack("25072100000");
        //测库存明细接口
//        apiWareHouseDetail.getWareHouseDetailForBlackLack("6911989136731");
    }

}