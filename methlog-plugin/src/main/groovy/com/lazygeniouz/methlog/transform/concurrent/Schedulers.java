package com.lazygeniouz.methlog.transform.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Schedulers {
    private static final int cpuCount = Runtime.getRuntime().availableProcessors();
    private final static ExecutorService IO = new ThreadPoolExecutor(0,
            cpuCount * 3,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    public static Worker IO() {
        return new Worker(IO);
    }
}
