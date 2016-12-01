package com.github.hippo.netty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.server.HippoServiceImplCache;
import com.github.hippo.util.GsonConvertUtils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * netty handler处理类
 * 
 * @author sl
 *
 */
@Sharable
public class HippoServerHandler extends SimpleChannelInboundHandler<HippoRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoServerHandler.class);
  private static final ExecutorService pool = Executors.newCachedThreadPool();

  private void handle(ChannelHandlerContext ctx, HippoRequest request) {
    HippoResponse response = new HippoResponse();
    try {
      response.setRequestId(request.getRequestId());

      if (request.getRequestType() == 0) {
        response.setResult(rpcProcess(request));
      } else if (request.getRequestType() == 1) {
        response.setResult(apiProcess(request));
      } else if (request.getRequestType() == 2) {
        response.setResult("ping success");
        response.setRequestId("-99");
      } else {
        response.setError(true);
        response.setThrowable(new IllegalAccessException("HippoRequest requestType err"));
      }
    } catch (Exception e1) {
      if (e1 instanceof InvocationTargetException) {
        response.setThrowable(e1.getCause());
      } else {
        response.setThrowable(e1);
      }
      response.setRequestId(request.getRequestId());
      response.setResult(request);
      response.setError(true);
      LOGGER.error("process error: request:" + ToStringBuilder.reflectionToString(request)
          + "&respose:" + ToStringBuilder.reflectionToString(response), e1);
    }
    ctx.writeAndFlush(response);
  }

  private Object rpcProcess(HippoRequest paras) throws InvocationTargetException {
    Object serviceBean = HippoServiceImplCache.INSTANCE.getHandlerMap().get(paras.getClassName());
    FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
    FastMethod serviceFastMethod =
        serviceFastClass.getMethod(paras.getMethodName(), paras.getParameterTypes());
    return serviceFastMethod.invoke(serviceBean, paras.getParameters());
  }

  private Object apiProcess(HippoRequest paras) throws Exception {/* 先不管重载 不管缓存 */
    if (paras.getParameters() != null && paras.getParameters().length > 1)
      throw new IllegalAccessException("apiProcess HippoRequest parameters err");
    Object serviceBean = HippoServiceImplCache.INSTANCE.getCacheBySimpleName(paras.getClassName());
    Class<?> serviceBeanClass = serviceBean.getClass();
    Method[] methods = serviceBeanClass.getDeclaredMethods();
    Object responseDto = null;
    for (Method method : methods) {
      if (!method.getName().equals(paras.getMethodName())) {
        continue;
      }
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 0) {// 无参数
        responseDto = method.invoke(serviceBean);
      } else if (parameterTypes.length == 1) {// 有参
        Class<?> parameterType = parameterTypes[0];
        Object requestDto =
            GsonConvertUtils.cleanseToObjectClass(paras.getParameters()[0], parameterType);
        responseDto = method.invoke(serviceBean, requestDto);
      }
      // 拿到返回
      return GsonConvertUtils.cleanseToObject(responseDto);
    }
    throw new NoSuchMethodException(paras.getMethodName());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error("netty server error", cause.fillInStackTrace());
    ctx.close();
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HippoRequest request) throws Exception {
    pool.execute(() -> handle(ctx, request));
  }
}
