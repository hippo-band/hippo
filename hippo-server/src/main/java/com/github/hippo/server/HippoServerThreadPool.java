package com.github.hippo.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 公共参数配置
 *
 * @author sl
 */
public enum HippoServerThreadPool {

    SINGLE, FIXED;

    private int threadCount;

    private ExecutorService single = Executors.newSingleThreadExecutor();

    private ExecutorService fixed;

    ExecutorService getPool() {

        if (this == SINGLE) {
            return single;
        } else {
            synchronized (this.getClass()) {
                if (fixed == null) {
                    fixed = Executors.newFixedThreadPool(threadCount <= 0 ? Runtime.getRuntime().availableProcessors() * 3 + 3 : threadCount);
                }
                return fixed;
            }
        }
    }

    void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
