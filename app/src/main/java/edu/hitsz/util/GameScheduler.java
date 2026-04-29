package edu.hitsz.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局调度器，用于定时任务（例如道具效果计时）
 */
public final class GameScheduler {

    private static final ScheduledExecutorService EXEC;

    static {
        // 自定义 ThreadFactory 实现守护线程和命名
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "game-scheduler-" + threadNumber.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        EXEC = new ScheduledThreadPoolExecutor(2, threadFactory);
    }

    private GameScheduler() {}

    public static ScheduledExecutorService get() {
        return EXEC;
    }

    public static void shutdownNow() {
        EXEC.shutdownNow();
    }
}