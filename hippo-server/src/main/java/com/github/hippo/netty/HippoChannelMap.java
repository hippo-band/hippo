package com.github.hippo.netty;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

/**
 * 保持长连接的客户端channel
 * 
 * @author sl
 *
 */
public class HippoChannelMap {
  private HippoChannelMap() {}

  private static final Map<String, SocketChannel> MAP = new ConcurrentHashMap<>();

  public static void put(String clientId, SocketChannel socketChannel) {
    MAP.put(clientId, socketChannel);
  }

  public static Channel get(String clientId) {
    return MAP.get(clientId);
  }

  public static boolean containsKey(String clientId) {
    return MAP.containsKey(clientId);
  }

  public static void remove(SocketChannel socketChannel) {
    for (Entry<String, SocketChannel> entry : MAP.entrySet()) {
      if (entry.getValue() == socketChannel) {
        MAP.remove(entry.getKey());
      }
    }
  }
}
