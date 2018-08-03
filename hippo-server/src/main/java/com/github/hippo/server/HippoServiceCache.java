package com.github.hippo.server;

import org.springframework.cglib.reflect.FastClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存了具体SOA实现类/接口
 *
 * @author sl
 */
public enum HippoServiceCache {

    INSTANCE;
    private Map<String, Object> implObjectMap = new HashMap<>();

    private Map<String, Class<?>> interfaceMap = new HashMap<>();

    private Map<String, FastClass> implClassMap = new HashMap<>();

    Map<String, Object> getImplObjectMap() {
        return implObjectMap;
    }

    Map<String, FastClass> getImplClassMap() {
        return implClassMap;
    }

    Map<String, Class<?>> getInterfaceMap() {
        return interfaceMap;
    }

}
