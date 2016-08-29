package com.github.hippo.server;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存了具体SOA实现类
 * 
 * @author sl
 *
 */
public enum HippoServiceImplCache {

  INSTANCE;
  private Map<String, Object> handlerMap = new HashMap<>();

  public Map<String, Object> getHandlerMap() {
    return handlerMap;
  }

  /**
   * 获取实现类
   * 
   * @param simpleName serviceName
   * @return 实现类
   */
  public Object getCacheBySimpleName(String simpleName) {
    for (String key : handlerMap.keySet()) {
      if (key.contains(simpleName)) return handlerMap.get(key);
    }
    return null;
  }
}
