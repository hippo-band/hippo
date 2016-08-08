package cloud.igoldenbeta.hippo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 声明在实现类上,interfaceClass传接口class
 * 
 * @author sl
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface HippoServiceImpl {
  /**
   * 接口类class
   * @return
   */
  Class<? extends Object> value();
}
