### **概述:**

**Hippo: 一个基于java的微服务框架,配置简单、使用方便。**

### 快速开始 

可以下载 [demo](https://github.com/hippo-band/hippo-demo) 进行测试,由于默认服务注册使用的是eureka,在测试Demo的时候需要先起一个eureka服务.具体查看 [https://github.com/hippo-band/hippo-demo](https://github.com/hippo-band/hippo-demo)

#### 前提条件

- JDK: version 8 or higher
- Maven: version 3 or higher

#### Maven dependency

maven分的比较细,具体如下：

提供接口的项目需要依赖以下pom

```
<!-- https://mvnrepository.com/artifact/com.github.hippo-band/hippo-annotation -->

<dependency>

    <groupId>com.github.hippo-band</groupId>

    <artifactId>hippo-annotation</artifactId>

    <version>1.0.0</version>

</dependency>
```

接口的具体实现类的项目(也就是通俗的服务端)需要依赖以下pom

ps:hippo-server-with-eureka是集成了去eureka注册的相关代码

```
<!-- https://mvnrepository.com/artifact/com.github.hippo-band/hippo-server-with-eureka -->

<dependency>

    <groupId>com.github.hippo-band</groupId>

    <artifactId>hippo-server-with-eureka</artifactId>

    <version>1.1.0</version>

</dependency>
```

客户端需要依赖以下pom

ps:hippo-cleint-with-eureka同样集成了去eureka获取注册信息的相关代码

```
<!-- https://mvnrepository.com/artifact/com.github.hippo-band/hippo-client-with-eureka -->
<dependency>
    <groupId>com.github.hippo-band</groupId>
    <artifactId>hippo-client-with-eureka</artifactId>
    <version>1.1.0</version>
</dependency>
```

也有不依赖eureka的pom依赖 就是去掉server/client artifactId后面的-with-eureka(那就需要自己实现服务注册的相关代码[实现ServiceGovern

接口])

#### 定义接口

```
package com.github.hippo.demo.service;

@HippoService(serviceName = "hippo.demo.service")
public interface TestService{
    public String ping(String username);
}
```

@HippoService(serviceName = "hippo.demo.service") 就是定义服务注册名,通常很多都写在配置文件里的,hippo采用在接口上定义注解实现,优点是在client调用的时候代码就很简洁。
ps:多个接口在同一个项目里必须使用同一个服务注册名

#### 接口实现类

```
package com.github.hippo.demo.service.impl;

@HippoServiceImpl()
public class TestServiceImpl implements TestService{
   @Override
  public String ping(String username) {
    return username;
  }
}
```

@HippoServiceImpl就是打个注解可以注入到spring.

#### 服务配置

```
在application-context.xml里加上
<context:component-scan base-package="com.github.hippo" />
在任一*.properties里配上eureka地址即可
eureka.serviceUrl=http://127.0.0.1/eureka/
```

application-context.xml主要是扫到hippo的相关jar包
至于服务ip和端口hippo会自动获取,当然也是可以人工在*.properties指定。
然后启动项目即可,如果使用的是hippo-demo那就是直接运行ServerContainer类.

#### 客户端配置

```
在application-context.xml里加上
<context:component-scan base-package="com.github.hippo" />
在任一*.properties里配上eureka地址即可
eureka.serviceUrl=http://127.0.0.1/eureka/
```

和service provider一样的配置

#### Junit Test

```
package junitTest

@ContextConfiguration(locations = "classpath:/application-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestServiceTest {
  
  @HippoClient
  private TestService testService;
  
  @Test
  public void ping() throws Exception {
    testService.ping("sl");
  }
}
```

#### Just so so？

支持3种调用方式 sync async-with-callback oneway
集成hystrix可以实现降级熔断等

支持json方式的通信方式,主要集成在apigate实现透传调用服务,再也不用写一个server在配套写一个webapp项目提供http服务了。

如下

```
@Test
  public void ping1() throws Throwable {
    System.out.println(hippoProxy.apiRequest("hippo.demo.service", "TestService/ping1",
        "{\"username\":\"598fdc0b0cf29eb2a0928a6d\",\"test\":[1,2,3]}"));
  }
```

服务内部实现了调用链,通过调用链可以串起所有的请求走向。
提供了集成elk的logback代码。
。。。。。。未完待续
