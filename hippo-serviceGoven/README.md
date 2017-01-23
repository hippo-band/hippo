#hippo-serviceGoven 

#简介
> hippo-serviceGoven 是整个hippo RPC框架的默认提供的服务治理实现框架，当然你也可以使用自己的服务治理框架，默认实现的服务治理框架是以Eureka框架为基础的，经过修改而成，用户无无须任何繁琐的配置就可以直接使用，当然你也可以通过被框架提供的属性，进行自己的需求配置。
  
#配置
> hippo-serviceGoven 的属性都有默认的值，用户大可不必去配置文件里修改属性，但鉴于用户自己的需求变化，框架还是提供了一些必要的属性选择

* erueka.ipAddress  服务绑定的IP地址 默认实现为localhost
* eureka.serviceUrls  服务注册的服务端地址，这个是必填项
* eureka.port  服务绑定的端口号，默认实现为8761
* eureka.instance.preferIpAddress = true/false 更希望以IP的形式出现，还是hostName的形式出现 默认实现为true
* eureka.instance.leaseRenewalIntervalInSeconds 心跳的更新时间，默认实现是30秒，每30秒会进行一次心跳检测
* eureka.instance.leaseExpirationDurationInSeconds 服务的失效时间，默认实现是90秒，也就是说 进行三次心跳检测都无效的情况下，服务会被踢掉
* eureka.client.registerWithEureka = true /false 是否再服务端注册 默认为true
* eureka.client.preferSameZoneEureka = true/false 是否更加优先寻找处于同一个zone的服务 默认为true
* eureka.client.region 地理位置上的区域划分 默认为 us-east-1
* eureka.client.zone  逻辑上的区域划分 默认为defaultZone

注意：这些属性全部都是通过spring @Value属性注入的
    
#使用
> hippo-serviceGoven是hippo RPC 服务注册 服务发现 默认的实现，具体的使用规则，请前往https://github.com/hippo-band/hippo
