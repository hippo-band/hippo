package com.github.hippo.util;

/**
 * 通用工具类
 *
 * @author sl
 */
public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }
}

