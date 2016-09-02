package com.github.hippo.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.github.hippo.annotation.HippoService;
import com.github.hippo.annotation.HippoServiceImpl;
import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.netty.HippoHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 服务注册以及启动netty server
 * 
 * @author sl
 *
 */
@Component
@Order
public class HippoServer implements ApplicationContextAware, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(HippoServer.class);
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
    new Thread(() -> {
      try {
        Thread.sleep(1);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      int port = serviceGovern.register(registryNames.iterator().next());
      EventLoopGroup bossGroup = new NioEventLoopGroup();
      EventLoopGroup workerGroup = new NioEventLoopGroup();
      try {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
              @Override
              public void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(new HippoDecoder(HippoRequest.class))
                    .addLast(new HippoEncoder(HippoResponse.class)).addLast(new HippoHandler());
              }
            }).option(ChannelOption.SO_BACKLOG, 2048).childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
      } catch (Exception e) {
        LOG.error("hippoServer error", e);
      } finally {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
      }
    }).start();
  }
}
