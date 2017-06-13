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
  private Map<String, Object> implObjectMap = new HashMap<>();

  public Map<String,Object> getImplObjectMap() {
    return implObjectMap;
  }

  /**
   * 获取实现类
   * 
   * @param simpleName serviceName
   * @return 实现类
   */
  public Object getCacheBySimpleName(String simpleName) {
    for (String key : implObjectMap.keySet()) {
      if (key.equalsIgnoreCase(simpleName) || key.equalsIgnoreCase(simpleName + "impl")
          || key.equalsIgnoreCase(simpleName + "serviceimpl")) {
        return implObjectMap.get(key);
      }
    }
    return null;
  }
}
