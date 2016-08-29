package cloud.igoldenbeta.hippo.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.github.hippo.annotation.HippoService;
import com.github.hippo.annotation.HippoServiceImpl;
import com.github.hippo.govern.ServiceGovern;

import cloud.igoldenbeta.hippo.zmq.ZmqRpcWorkerListener;

/**
 * 服务注册以及启动zmq
 * 
 * @author sl
 *
 */
@Component
@Order
public class HippoServer implements ApplicationContextAware, InitializingBean {

  @Autowired
  private ServiceGovern serviceGovern;

  private Set<String> registryNames = new HashSet<>();

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(HippoServiceImpl.class);
    if (MapUtils.isNotEmpty(serviceBeanMap)) {
      for (Object serviceBean : serviceBeanMap.values()) {
        String interfaceName =
            serviceBean.getClass().getAnnotation(HippoServiceImpl.class).value().getName();
        HippoServiceImplCache.INSTANCE.getHandlerMap().put(interfaceName, serviceBean);
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
        for (Class<?> class1 : interfaces) {
          HippoService annotation = class1.getAnnotation(HippoService.class);
          if (annotation != null) {
            registryNames.add(annotation.serviceName());
          }
        }

      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    if (registryNames.size() > 1) {
      throw new IllegalAccessError("多个HippoService的serviceName必须一样[" + registryNames + "]");
    }
    // 服务注册
    int port = serviceGovern.register(registryNames.iterator().next());
    // 起zmq服务
    new Thread(new ZmqRpcWorkerListener(port)).start();
  }
}
