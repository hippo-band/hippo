package com.github.hippo.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum HippoClientProcessPool {
  INSTANCE;
  private ExecutorService executorService =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

  public ExecutorService getPool() {
    return executorService;
  }
}

