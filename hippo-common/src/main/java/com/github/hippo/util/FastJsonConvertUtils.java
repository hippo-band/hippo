package com.github.hippo.util;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.alibaba.fastjson.JSON;

/**
 * fastJson 操作类
 * 
 * @author sl
 *
 */
public class FastJsonConvertUtils {


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
        return JSON.toJSONString(obj);
    } catch (Exception e) {
      throw new ClassCastException("obj cleanse:" + ToStringBuilder.reflectionToString(obj));
    }
  }


  @SuppressWarnings("unchecked")
  public static Map<String, Object> jsonToMap(String data) {
    return JSON.parseObject(data, Map.class);
  }



  public static Object jsonToJavaObject(String object, Class<?> parameterType) {

    return JSON.parseObject(object, parameterType);
  }
}

