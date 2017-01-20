package com.github.hippo.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author sl
 *
 */
public final class HippoClientBootstrapMap {
  private HippoClientBootstrapMap() {}

  private static final Map<String, HippoClientBootstrap> bootstrapMap = new ConcurrentHashMap<>();

  public static void put(String serviceName, HippoClientBootstrap bootstrap) {
    bootstrapMap.put(serviceName, bootstrap);
  }

  public static HippoClientBootstrap get(String serviceName) {
    return bootstrapMap.get(serviceName);
  }

  public static boolean containsKey(String serviceName) {
    return bootstrapMap.containsKey(serviceName);
  }

  public static HippoClientBootstrap remove(String serviceName) {
    return bootstrapMap.remove(serviceName);
  }
}
