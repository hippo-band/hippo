package cloud.igoldenbeta.hippo.zmq;

import cloud.igoldenbeta.hippo.util.GsonConvertUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import cloud.igoldenbeta.hippo.bean.HippoRequest;
import cloud.igoldenbeta.hippo.bean.HippoResponse;
import cloud.igoldenbeta.hippo.server.HippoServiceImplCache;
import cloud.igoldenbeta.hippo.util.SerializationUtils;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * zmq rpc worker 处理类
 * 
 * @author sl
 *
 */
public class ZmqRpcWorkerProcess {
  private static final Logger log = LoggerFactory.getLogger(ZmqRpcWorkerProcess.class);

  public void process(ZMQ.Socket socket) {
    ZMsg msg = ZMsg.recvMsg(socket);
    msg.addLast(process(msg.removeLast()));
    msg.send(socket); // 将数据发送回去
  }

  private ZFrame process(ZFrame request) {
    HippoRequest paras = SerializationUtils.deserialize(request.getData(), HippoRequest.class);
    HippoResponse response = new HippoResponse();

    try {
      if (paras == null) throw new IllegalAccessException("HippoRequest deserialize err");
      response.setRequestId(paras.getRequestId());

      if (paras.getRequestType() == 0){
        response.setResult(rpcProcess(paras));
      }else if (paras.getRequestType() == 1){
        response.setResult(apiProcess(paras));
      }else {
        throw new IllegalAccessException("HippoRequest requestType err");
      }

      return new ZFrame(SerializationUtils.serialize(response));
    } catch (Exception e1) {

      response.setRequestId(paras == null? "": paras.getRequestId());
      response.setThrowable(e1);
      response.setError(true);
      log.error("process error: request:" + ToStringBuilder.reflectionToString(paras) + "&respose:"
          + ToStringBuilder.reflectionToString(response), e1);
      return new ZFrame(SerializationUtils.serialize(response));
    }
  }

  private Object rpcProcess(HippoRequest paras)throws Exception{
    Object serviceBean = HippoServiceImplCache.INSTANCE.getHandlerMap().get(paras.getClassName());
    FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
    FastMethod serviceFastMethod =
            serviceFastClass.getMethod(paras.getMethodName(), paras.getParameterTypes());
    Object responseDto = serviceFastMethod.invoke(serviceBean, paras.getParameters());
    return responseDto;
  }

  private Object apiProcess(HippoRequest paras)throws Exception{/*先不管重载 不管缓存*/
    if (paras.getParameters() != null && paras.getParameters().length > 1) throw new IllegalAccessException("apiProcess HippoRequest parameters err");

    Object serviceBean = HippoServiceImplCache.INSTANCE.getCacheBySimpleName(paras.getClassName());
    Class<?> serviceBeanClass = serviceBean.getClass();
    Method[] methods = serviceBeanClass.getDeclaredMethods();
    Object responseDto = null;

    for (Method method : methods){
      if (! method.getName().equals(paras.getMethodName())) continue;

      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 0){//无参数
        responseDto = method.invoke(serviceBean);
      }else if (parameterTypes.length == 1){//有参
        Class<?> parameterType = parameterTypes[0];
        Object requestDto = GsonConvertUtils.cleanseToObjectClass(paras.getParameters()[0], parameterType);
        responseDto = method.invoke(serviceBean, requestDto);
      }

      if (responseDto != null){//拿到返回
        if(isBaseDataType(responseDto.getClass())) return responseDto;
          else return GsonConvertUtils.cleanseToObject(responseDto);
      }else {
        return null;
      }
    }

    throw new NoSuchMethodException(paras.getMethodName());
  }

  private static boolean isBaseDataType(@SuppressWarnings("rawtypes") Class clazz)  throws Exception
  {
    return
            (
                    clazz.equals(String.class) ||
                    clazz.equals(Integer.class)||
                    clazz.equals(Byte.class) ||
                    clazz.equals(Long.class) ||
                    clazz.equals(Double.class) ||
                    clazz.equals(Float.class) ||
                    clazz.equals(Character.class) ||
                    clazz.equals(Short.class) ||
                    clazz.equals(BigDecimal.class) ||
                    clazz.equals(BigInteger.class) ||
                    clazz.equals(Boolean.class) ||
                    clazz.equals(Date.class) ||
                    clazz.isPrimitive()
            );
  }
}
