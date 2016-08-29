package com.github.hippo.util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class GsonConvertUtils {

  private static final Gson gson = new Gson();

  /**
   * 把json字符串转成java对象
   */
  public static <T> T toObject(String json, Class<T> objectClass) {
    if (StringUtils.isEmpty(json)) return null;
    try {
      return gson.fromJson(json, objectClass);
    } catch (Exception e) {
      throw new ClassCastException("json to obj:" + json + "," + objectClass);
    }
  }

  /**
   * 把json字符串转成LinkedTreeMap对象
   */
  public static Object toObject(String json) {
    if (StringUtils.isEmpty(json)) return null;

    try {
      return gson.fromJson(json, Object.class);
    } catch (Exception e) {
      throw new ClassCastException("json to obj:" + json);
    }
  }

  /**
   * 把json字符串转成List<LinkedTreeMap>对象
   */
  @SuppressWarnings("rawtypes")
  public static List<LinkedTreeMap> toMapList(String json) {
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    try {
      return gson.fromJson(json, new TypeToken<List<Object>>() {}.getType());
    } catch (Exception ex) {
      throw new ClassCastException("json to toMapList:" + json);
    }
  }

  /**
   * 把对象转成json字符串
   */
  public static String toJson(Object obj) {
    if (obj == null) return null;

    try {
      return gson.toJson(obj);
    } catch (Exception e) {
      throw new ClassCastException("obj to json:" + ToStringBuilder.reflectionToString(obj));
    }
  }


  /**
   * hippo-server used 清洗对象格式
   */
  public static Object cleanseToObject(Object obj) {
    if (obj == null) return null;
    try {
      if (isBaseDataType(obj.getClass())) return obj;
      else return gson.fromJson(gson.toJson(obj), Object.class);
    } catch (Exception e) {
      throw new ClassCastException("obj cleanse:" + ToStringBuilder.reflectionToString(obj));
    }
  }

  /**
   * hippo-server used 获取到json转为T
   */
  public static <T> T cleanseToObjectClass(Object obj, Class<T> objectClass) {
    if (obj == null) return null;

    try {
      if (obj instanceof String)
        return gson.fromJson((String) obj, objectClass);
      else
        return gson.fromJson(gson.toJson(obj), objectClass);
    } catch (Exception e) {
      throw new ClassCastException(
          "json to obj:" + ToStringBuilder.reflectionToString(obj) + "," + objectClass);
    }
  }

  /**
   * 判断是否是基础类型
   * @param clazz
   * @return
     */
  private static boolean isBaseDataType(@SuppressWarnings("rawtypes") Class clazz) {
    return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class)
            || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class)
            || clazz.equals(Character.class) || clazz.equals(Short.class)
            || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class)
            || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
  }

}
