package com.meitu.data.common.Thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时线程创建
 * @author zj
 * @since 2018/7/20
 */
public class ScheduledExecutor {

    private ScheduledExecutorService executorService;
    private long period = 1000;
    private long initialDelay = 1000;

    private void startScheduledExecutor() {
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("new ThreadName").build());
        //参数说明: 线程执行之后延迟 initialDelay 执行方法  然后以 period 周期执行
        executorService.scheduleAtFixedRate(() -> {
            //每次线程运行去要去执行的方法
            method();
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private void method() {
        System.out.println("scheduled thread method.");
    }
}
