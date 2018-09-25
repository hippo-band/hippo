package com.github.hippo.util;

import org.nustaq.serialization.FSTConfiguration;


/**
 * 序列化工具 fst
 *
 * @author sl
 */
public class SerializationUtils {

    static FSTConfiguration configuration = FSTConfiguration.createStructConfiguration();

    /**
     * 序列化
     *
     * @param obj obj
     * @param <T> 泛型
     * @return data[]
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        return configuration.asByteArray(obj);
    }

    /**
     * 反序列化
     *
     * @param data origin data
     * @return cls.instance
     */
    public static <T> T deserialize(byte[] data) {
        return (T) configuration.asObject(data);
    }

}
