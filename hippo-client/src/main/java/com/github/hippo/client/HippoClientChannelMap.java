package com.github.hippo.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * 保持长连接的客户端channel
 * 
 * @author sl
 *
 */
public final class HippoClientChannelMap {
  private HippoClientChannelMap() {}

  private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

  public static void put(String clientId, Channel channel) {
    channelMap.put(clientId, channel);
  }

  public static Channel get(String clientId) {
    return channelMap.get(clientId);
  }

  public static boolean containsKey(String clientId) {
    return channelMap.containsKey(clientId);
  }

  public static Channel remove(String clientId) {
    return channelMap.remove(clientId);
  }
}
