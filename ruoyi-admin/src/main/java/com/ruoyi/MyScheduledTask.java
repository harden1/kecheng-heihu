package com.ruoyi;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MyScheduledTask {

    // 每小时执行一次（整点开始）
    @Scheduled(cron = "0/2 * * * * ?")
    public void executeTask() {
        System.out.println("执行任务时间：" + java.time.LocalDateTime.now());

    }
}