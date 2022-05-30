package com.lazygeniouz.methlog.transform.concurrent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Worker {

    protected final ExecutorService executor;
    private final LinkedList<Future<?>> futures = new LinkedList<>();
    private final int cpuCount = Runtime.getRuntime().availableProcessors();


    private Worker() {
        this.executor = getExecutor();
    }


    public <T> void submit(Callable<T> callable) {
        Future<T> future = executor.submit(callable);
        futures.add(future);
    }

    public void await() throws IOException {
        Future<?> future;
        while ((future = futures.pollFirst()) != null) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private ExecutorService getExecutor() {
        return new ThreadPoolExecutor(
                0, cpuCount * 3,
                30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
    }

    public static Worker get() {
        return new Worker();
    }
}
