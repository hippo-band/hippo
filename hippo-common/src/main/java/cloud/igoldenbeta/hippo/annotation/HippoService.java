package cloud.igoldenbeta.hippo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 声明在接口上 如果有多个接口需要提供SOA服务,serviceName必须一样
 * 
 * @author sl
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface HippoService {
  /**
   * 服务注册时的名字
   * 
   * @return
   */
  String serviceName();
}
