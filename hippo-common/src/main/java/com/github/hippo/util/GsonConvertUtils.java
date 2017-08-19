package com.github.hippo.util;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * gson 操作类
 * 
 * @author sl
 *
 */
public class GsonConvertUtils {

  private static final Gson gson = new Gson();

  /**
   * hippo-server used 清洗对象格式
   * 
   * @param obj origin obj
   * @return obj
   */
  public static Object cleanseToObject(Object obj) {
    if (obj == null) return null;
    try {
      if (obj.getClass().isPrimitive())
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
      return gson.fromJson(gson.toJson(obj), objectClass);
    } catch (Exception e) {
      throw new ClassCastException(
          "json to obj:" + ToStringBuilder.reflectionToString(obj) + "," + objectClass);
    }
  }

  public static Map<String, Object> jsonToMap(String data) {
    return new GsonBuilder().create().fromJson(data,
        new TypeToken<Map<String, Object>>() {}.getType());
  }
}
