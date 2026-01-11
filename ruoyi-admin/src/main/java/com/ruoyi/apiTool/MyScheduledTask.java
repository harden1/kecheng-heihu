package com.ruoyi.apiTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务计划接口
 */

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
    @Autowired
    private ScheduleReport scheduleReport;

    // 启动后立即执行一次，以后在上一次任务执行完成后，等待 1 小时再执行
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void executeTask() {
//        System.out.println("执行任务时间：" + java.time.LocalDateTime.now());
        //调用token接口
//        String accessToken = accessTokenService.getAccessToken(false);
//        System.out.println("accessToken: " + accessToken);
        //测试userinfo
//        apiUserInfoForBlackLack.getUserApiForBlacklack();
        //调用订单二维码数据，查询订单
        //测试task接口
        //apiTaskForBlackLack.getTackForBlackLack("25072100000");
        //测库存明细接口
//        apiWareHouseDetail.getWareHouseDetailForBlackLack("6911989136731");
    }

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "0 */1 * * * ?")
    public void executeTaskReport() {
        scheduleReport();
    }
    public void scheduleReport() {
        // 防重入
        if (!running.compareAndSet(false, true)) {
            System.out.println("任务正在执行中，跳过执行");
            return;
        }
        try {
            int minute = LocalDateTime.now().getMinute() % 2;
            if (minute == 0) {
//                System.out.println("执行任务一时间：" + java.time.LocalDateTime.now());
                scheduleReport.goodReportBatch();
            } else {
//                System.out.println("执行任务二时间：" + java.time.LocalDateTime.now());
                scheduleReport.badReportOne();
            }
        } finally {
            running.set(false);
        }
    }
    /* 每小时查一次运行情况报表
     * 待报工，已报工，定时任务状态，报工失败的报表，以及激活定时任务，重置定时任务
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void executeTaskRecordCron() {


    }
}