package com.github.hippo.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.hippo.netty.HippoClientBootstrap;

/**
 * 
 * @author sl
 *
 */
public final class HippoClientBootstrapMap {
  private HippoClientBootstrapMap() {}

  private static final Map<String, HippoClientBootstrap> bootstrapMap = new ConcurrentHashMap<>();

  public static void put(String clientId, HippoClientBootstrap bootstrap) {
    bootstrapMap.put(clientId, bootstrap);
  }

  public static HippoClientBootstrap get(String clientId) {
    return bootstrapMap.get(clientId);
  }

  public static boolean containsKey(String clientId) {
    return bootstrapMap.containsKey(clientId);
  }

  public static HippoClientBootstrap remove(String clientId) {
    return bootstrapMap.remove(clientId);
  }
}
