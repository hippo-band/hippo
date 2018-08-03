package com.github.hippo.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * hippo client map cache
 *
 * @author sl
 */
public enum HippoClientCache {
    INSTANCE;
    private Map<String, String> classNameServiceNameMap = new ConcurrentHashMap<>();


    Map<String, String> getClassNameServiceNameMap() {
        return classNameServiceNameMap;
    }
}
