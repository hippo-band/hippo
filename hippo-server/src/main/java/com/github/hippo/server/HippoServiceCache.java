package com.github.hippo.server;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存了具体SOA实现类/接口
 * 
 * @author sl
 *
 */
public enum HippoServiceCache {

  INSTANCE;
  private Map<String, Object> implObjectMap = new HashMap<>();

  private Map<String, Class<?>> interfaceMap = new HashMap<>();

  Map<String, Object> getImplObjectMap() {
    return implObjectMap;
  }

  Map<String, Class<?>> getInterfaceMap() {
    return interfaceMap;
  }

}
