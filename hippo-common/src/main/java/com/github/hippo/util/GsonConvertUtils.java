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

/**
 * gson 操作类
 * 
 * @author sl
 *
 */
public class GsonConvertUtils {

  private static final Gson gson = new Gson();

  /**
   * 把json字符串转成java对象
   * 
   * @param json json string
   * @param objectClass obj class
   * @param <T> 泛型
   * @return objectClass instance
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
   * 
   * @param json json string
   * @return obj(LinkedTreeMap)
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
   * 把json字符串转成ListLinkedTreeMap对象
   * 
   * @param json json string
   * @return List LinkedTreeMap
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
   * 
   * @param obj origin obj
   * @return json
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
   * 
   * @param obj origin obj
   * @return obj
   */
  public static Object cleanseToObject(Object obj) {
    if (obj == null) return null;
    try {
      if (isBaseDataType(obj.getClass()))
        return obj;
      else
        return gson.fromJson(gson.toJson(obj), Object.class);
    } catch (Exception e) {
      throw new ClassCastException("obj cleanse:" + ToStringBuilder.reflectionToString(obj));
    }
  }

  /**
   * hippo-server used 获取到json转为T
   * 
   * @param obj origin obj
   * @param objectClass objectClass
   * @param <T> 泛型
   * @return objectClass.instance
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
   * 
   * @param clazz
   * @return true/false
   */
  private static boolean isBaseDataType(@SuppressWarnings("rawtypes") Class clazz) {
    return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class)
        || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class)
        || clazz.equals(Character.class) || clazz.equals(Short.class)
        || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class)
        || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
  }

}
