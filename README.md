#Hippo: 分布式通讯框架

###Hippo是一个分布式通讯框架.支持JAVA依赖接口相互调用,支持JSON进行跨语言通信或APIGate提供restful接口处理所有请求.

相关说明

1.无XML配置,使用简单

2.底层使用Netty进行通信

3.序列化使用protostuff以及gson

#项目描述

###hippo-annotaion

    如果接口需要提供RPC服务需打上HippoService的注解(需要依赖pom)
    <dependency>
        <groupId>com.github.hippo-band</groupId>
        <artifactId>hippo-annotation</artifactId>
        <version>0.0.1</version>
    </dependency>
    详情:http:///

###hippo-common

    hippo的公共类,主要提供client/serviceImpl的注解,序列化/反序列化工具类,服务注册接口(用作用户自定义实现服务发现服务注册),hippo-client和hippo-server都依赖这个POM所以用户不需要自己在依赖
    详情:http:///

###hippo-client

    如果需要远程调用service需依赖这个项目
    <dependency>
        <groupId>com.github.hippo-band</groupId>
        <artifactId>hippo-client</artifactId>
        <version>0.0.1</version>
    </dependency>
    详情:http:///

###hippo-server

    打上HippoService注解的接口的实现类需要依赖这个pom,也就是远程调用接口的实际处理类
    <dependency>
        <groupId>com.github.hippo-band</groupId>
        <artifactId>hippo-server</artifactId>
        <version>0.0.1</version>
    </dependency>
    详情:http:///

###hippo-serviceGoven

    使用eureka作为服务治理,包括服务注册,服务发现,集群管理等.用户可以直接依赖这个POM实现服务治理,也可以不依赖这个POM而自己实现服务治理.
    如需自己实现服务治理要实现hippo-common里的ServiceGovern接口
    使用我们提供的服务发现需在hippo-client和hippo-server同时在引入hippo-serviceGoven pom.同时配套使用我们还提供了eureka的服务端cheris https://github.com/hippo-band/cheris
    <dependency>
        <groupId>com.github.hippo-band</groupId>
        <artifactId>hippo-serviceGoven</artifactId>
        <version>0.0.1</version>
    </dependency>
    [README]:https://github.com/hippo-band/hippo/blob/master/hippo-serviceGoven/Read.md


