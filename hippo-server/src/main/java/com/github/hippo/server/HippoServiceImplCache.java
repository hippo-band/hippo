package com.github.hippo.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 缓存了具体SOA实现类
 * 
 * @author sl
 *
 */
public enum HippoServiceImplCache {

  INSTANCE;
  private Map<String, Object> implObjectMap = new HashMap<>();

  public Map<String, Object> getImplObjectMap() {
    return implObjectMap;
  }

  /**
   * 获取实现类
   * 
   * @param simpleName serviceName
   * @return 实现类
   */
  public Object getCacheBySimpleName(String simpleName) {
    if (StringUtils.isBlank(simpleName)) {
      return null;
    }
    for (String key : implObjectMap.keySet()) {
      String newKey = key.substring(key.lastIndexOf('.') + 1);
      if (newKey.equalsIgnoreCase(simpleName)) {
        return implObjectMap.get(key);
      }
    }
    return null;
  }
}
