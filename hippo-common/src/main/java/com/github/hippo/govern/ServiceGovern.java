package com.github.hippo.govern;

import java.util.List;
import java.util.Map;

/**
 * 服务管理
 *
 * @author sl
 */
public interface ServiceGovern {
    /**
     * 服务注册
     * 建议使用@HippoService来声明serviceName
     *
     * @param serviceName 注册服务名
     * @return 注册时的端口号
     */
    int register(String serviceName);

    /**
     * 服务注册
     *
     * @param serviceName 注册服务名
     * @param metaMap     key接口名,value:json
     * @return 注册时的端口号
     */
    int register(String serviceName, Map<String, String> metaMap);

    /**
     * 获取服务地址 ip:port
     * 127.0.0.1:7070
     *
     * @param serviceName 注册服务名
     * @return ip:port
     */
    String getServiceAddress(String serviceName);

    /**
     * 获取serviceName
     *
     * @param className 当前接口className,通过metaMap获取到对应的serviceName
     * @return serviceName
     */
    String getServiceNameByClassName(String className);

    /**
     * 获取serviceName所有的服务器地址ip:prot
     *
     * @param serviceName 注册服务名
     * @return
     */
    List<String> getServiceAddresses(String serviceName);


    void shutdown();

}
