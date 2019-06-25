package com.github.hippo.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * client thread pool
 *
 * @author sl
 */
public enum HippoClientProcessPool {
    INSTANCE;
    private ExecutorService EXECUTORSERVICE =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 20);

    public ExecutorService getPool() {
        return EXECUTORSERVICE;
    }
}

