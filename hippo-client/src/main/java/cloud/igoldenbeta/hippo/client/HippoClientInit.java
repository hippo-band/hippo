package cloud.igoldenbeta.hippo.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cloud.igoldenbeta.hippo.annotation.HippoClient;

/**
 * 初始化有@RpcConsumer注解的类
 * 
 * @author sl
 *
 */
@Configuration
public class HippoClientInit implements ApplicationContextAware {
  private Map<String, Object> rpcConsumerMap = new HashMap<>();
  @Autowired
  private HippoProxy hippoProxy;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {}

  @Bean
  public BeanPostProcessor beanPostProcessor() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName)
          throws BeansException {
        Class<?> objClz = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
          objClz = AopUtils.getTargetClass(bean);
        }
        for (Field field : objClz.getDeclaredFields()) {
          HippoClient rpcConsumer = field.getAnnotation(HippoClient.class);
          if (rpcConsumer != null) {
            @SuppressWarnings("rawtypes")
            Class type = field.getType();
            String key = type.getCanonicalName();
            if (!rpcConsumerMap.containsKey(key)) {
              rpcConsumerMap.put(key, hippoProxy.create(type));
            }
            try {
              field.setAccessible(true);
              field.set(bean, rpcConsumerMap.get(key));
              field.setAccessible(false);
            } catch (Exception e) {
              throw new BeanCreationException(beanName, e);
            }
          }
        }
        return bean;
      }


      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName)
          throws BeansException {
        return bean;
      }
    };

  }
}
