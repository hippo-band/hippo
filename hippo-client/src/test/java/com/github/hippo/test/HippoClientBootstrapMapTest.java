package com.github.hippo.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author sl
 *
 */
public final class HippoClientBootstrapMapTest {
  private HippoClientBootstrapMapTest() {}

  public static final Map<String, HippoClientBootstrapTest> bootstrapMap = new ConcurrentHashMap<>();

  public static void put(String clientId, HippoClientBootstrapTest bootstrap) {
    bootstrapMap.put(clientId, bootstrap);
  }

  public static HippoClientBootstrapTest get(String clientId) {
    return bootstrapMap.get(clientId);
  }

  public static boolean containsKey(String clientId) {
    return bootstrapMap.containsKey(clientId);
  }

  public static HippoClientBootstrapTest remove(String clientId) {
    return bootstrapMap.remove(clientId);
  }
}
