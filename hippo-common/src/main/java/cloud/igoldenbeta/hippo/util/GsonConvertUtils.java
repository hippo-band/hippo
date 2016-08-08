package cloud.igoldenbeta.hippo.util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.springframework.util.StringUtils;
import java.util.List;

public class GsonConvertUtils {

    private static Gson gson = new Gson();

	/**
	 * 把json字符串转成java对象
	 */
	public static<T> T toObject(String json, Class<T> objectClass) {
		if(StringUtils.isEmpty(json)) return null;

        T result = null;
        try {
            result = gson.fromJson(json, objectClass);
        } catch (Exception e) {
            throw new ClassCastException("json to obj");
        }
        return result;
	}

    /**
     * 把json字符串转成LinkedTreeMap对象
     */
    public static Object toObject(String json) {
        if(StringUtils.isEmpty(json)) return null;

        try {
            Object o = gson.fromJson(json, Object.class);
            return o;
        } catch (Exception e) {
            throw new ClassCastException("json to obj");
        }
    }

    /**
     * 把json字符串转成List<LinkedTreeMap>对象
     */
	@SuppressWarnings("rawtypes")
  public static List<LinkedTreeMap> toMapList(String json) {
		if(StringUtils.isEmpty(json)) return null;

		try {
            List<LinkedTreeMap> results = gson.fromJson(json, new TypeToken<List<Object>>(){}.getType());

			return results;
        } catch (Exception ex) {
            throw new ClassCastException("json to toMapList");
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
            throw new ClassCastException("obj to json");
        }
    }


    /** hippo-server used
     * 清洗对象格式
     */
    public static Object cleanseToObject(Object obj) {
        if (obj == null) return null;

        try {
            return gson.fromJson(gson.toJson(obj), Object.class);
        } catch (Exception e) {
            throw new ClassCastException("obj cleanse");
        }
    }
    /** hippo-server used
     * 获取到json转为T
     */
    public static<T> T cleanseToObjectClass(Object obj, Class<T> objectClass) {
        if(obj == null) return null;

        T result = null;
        try {
            if (obj instanceof String) result = gson.fromJson((String) obj, objectClass);
            else result = gson.fromJson(gson.toJson(obj), objectClass);
        } catch (Exception e) {
            throw new ClassCastException("json to obj");
        }
        return result;
    }

}
