package cloud.igoldenbeta.hippo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;



/**
 * 序列化工具 protostuff
 * 
 * @author sl
 *
 */
public class SerializationUtils {
  private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

  private static Objenesis objenesis = new ObjenesisStd(true);

  private SerializationUtils() {}

  @SuppressWarnings("unchecked")
  private static <T> Schema<T> getSchema(Class<T> cls) {
    Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
    if (schema == null) {
      schema = RuntimeSchema.createFrom(cls);
      if (schema != null) {
        cachedSchema.put(cls, schema);
      }
    }
    return schema;
  }

  /**
   * 序列化
   * 
   * @param obj
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> byte[] serialize(T obj) {
    Class<T> cls = (Class<T>) obj.getClass();
    LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    try {
      Schema<T> schema = getSchema(cls);
      return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    } finally {
      buffer.clear();
    }
  }

  /**
   * 反序列化
   * 
   * @param data
   * @param cls
   * @return
   */
  public static <T> T deserialize(byte[] data, Class<T> cls) {
    try {
      T message = objenesis.newInstance(cls);
      Schema<T> schema = getSchema(cls);
      ProtostuffIOUtil.mergeFrom(data, message, schema);
      return message;
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

}
