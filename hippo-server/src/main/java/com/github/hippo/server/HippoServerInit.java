package com.github.hippo.server;

import com.github.hippo.annotation.HippoService;
import com.github.hippo.annotation.HippoServiceImpl;
import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoServiceException;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.util.CommonUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 服务注册以及启动netty server
 *
 * @author sl
 */
@Component
@Order
public class HippoServerInit implements ApplicationContextAware, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HippoServerInit.class);
    @Autowired
    private ServiceGovern serviceGovern;
    @Value("${service.name:}")
    private String serviceName;

    @Value("${hippo.server.thread.count}")
    private int threadCount;

    private Map<String, String> metaMap = new HashMap<>();

    private Set<String> registryNames = new HashSet<>();


    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        //init fixed theadcount
        HippoServerThreadPool.FIXED.setThreadCount(threadCount);

        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(HippoServiceImpl.class);
        if (MapUtils.isEmpty(serviceBeanMap)) {
            throw new HippoServiceException(
                    "该项目依赖了hippo-server,在接口实现类请使用[@HippoServiceImpl]来声明");
        }
        Map<String, Object> implObjectMap = HippoServiceCache.INSTANCE.getImplObjectMap();
        Map<String, FastClass> implClassMap = HippoServiceCache.INSTANCE.getImplClassMap();
        Map<String, Class<?>> interfaceMap = HippoServiceCache.INSTANCE.getInterfaceMap();
        for (Object serviceBean : serviceBeanMap.values()) {
            String simpleName = null;
            Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
            int index = 0;
            for (Class<?> class1 : interfaces) {
                //兼容@HippoService方式
                HippoService annotation = class1.getAnnotation(HippoService.class);
                if (annotation == null && CommonUtils.isJavaClass(class1)) {
                    continue;
                }
                if (index == 1) {
                    throw new HippoServiceException(
                            serviceBean.getClass().getName() + "已经实现了[" + simpleName + "]接口,hippoServiceImpl不允许实现2个接口。");
                }
                simpleName = class1.getSimpleName();
                index++;
                // simpleName 提供apiProcess使用
                // 全限定名提供给rpcProcess使用
                String name = class1.getName();
                //提供给apigate访问的方式是接口名+方法名,所以会导致apigate访问过来找到2个实现类导致异常
                if (implObjectMap.containsKey(simpleName)) {
                    throw new HippoServiceException(
                            "接口[" + simpleName + "]已存在。[" + name + "],hippo不支持不同包名但接口名相同,请重命名当前接口名");
                }
                implObjectMap.put(name, serviceBean);
                interfaceMap.put(simpleName, class1);
                implClassMap.put(name, FastClass.create(serviceBean.getClass()));
                if (annotation != null) {
                    registryNames.add(annotation.serviceName());
                } else {
                    metaMap.put(name, serviceName);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        //兼容使用@HippoService方式
        if (StringUtils.isBlank(serviceName) && registryNames.size() > 1) {
            throw new HippoServiceException("多个@HippoService的serviceName必须一样[" + registryNames + "]");
        }

        if (StringUtils.isBlank(serviceName) && registryNames.isEmpty()) {
            throw new HippoServiceException("serviceName可在*.properties里配置service.name={your service name}");
        }

        //@HippoService优先级高于在*.properties里配置
        if (!registryNames.isEmpty()) {
            serviceName = registryNames.iterator().next();
        }

        new Thread(this::run).start();
    }

    private void run() {
        try {
            Thread.sleep(1);
        } catch (Exception e1) {
            LOG.error("Thread.sleep error", e1);
        }
        int port = serviceGovern.register(serviceName, metaMap);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(new HippoDecoder(HippoRequest.class))
                                    .addLast(new HippoEncoder(HippoResponse.class))
                                    .addLast(new HippoServerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("hippoServer init error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
