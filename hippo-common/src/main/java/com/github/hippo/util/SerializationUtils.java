package com.github.hippo.util;

/**
 * 序列化工具 fst
 *
 * @author sl
 */
public class SerializationUtils {


    private SerializationUtils() {
    }


    /**
     * 序列化
     *
     * @param obj obj
     * @param <T> 泛型
     * @return data[]
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        return org.springframework.util.SerializationUtils.serialize(obj);
    }

    /**
     * 反序列化
     *
     * @param data origin data
     * @param cls  obj
     * @return cls.instance
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        return (T) org.springframework.util.SerializationUtils.deserialize(data);
    }


}
