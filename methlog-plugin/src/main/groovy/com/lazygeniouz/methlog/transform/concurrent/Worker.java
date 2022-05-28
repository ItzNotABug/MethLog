package com.lazygeniouz.methlog.transform.concurrent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Worker {
    protected final LinkedList<Future<?>> futures = new LinkedList<Future<?>>() {
        @Override
        public synchronized boolean add(Future<?> future) {
            return super.add(future);
        }

        @Override
        public synchronized Future<?> pollFirst() {
            return super.pollFirst();
        }
    };
    protected ExecutorService executor;

    Worker(ExecutorService executor) {
        this.executor = executor;
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
}
