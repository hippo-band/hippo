package com.github.hippo.netty;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoServiceUnavailableException;
import com.github.hippo.govern.ServiceGovern;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * hippo client Bootstrap
 * 
 * @author sl
 *
 */
public class HippoClientBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoClientBootstrap.class);

  private String host;
  private int port;
  private int timeout;
  private String serviceName;

  private Bootstrap bootstrap = new Bootstrap();
  private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
  private HippoRequestHandler handler;
  private ServiceGovern serviceGovern;


  public static HippoClientBootstrap getBootstrap(String serviceName, int timeout,
      ServiceGovern serviceGovern) throws Exception {

    if (!HippoClientBootstrapMap.containsKey(serviceName)) {
      synchronized (HippoClientBootstrapMap.class) {
        if (!HippoClientBootstrapMap.containsKey(serviceName)) {
          HippoClientBootstrap hippoClientBootstrap =
              new HippoClientBootstrap(serviceName, timeout, serviceGovern);
          HippoClientBootstrapMap.put(serviceName, hippoClientBootstrap);
        }
      }
    }
    return HippoClientBootstrapMap.get(serviceName);
  }

  private HippoClientBootstrap(String serviceName, int timeout, ServiceGovern serviceGovern)
      throws Exception {
    this.serviceName = serviceName;
    this.timeout = timeout;
    this.serviceGovern = serviceGovern;
    //init();
  }

  private void init() {
    initHostAndPort();
    try {
      handler = new HippoRequestHandler(this.serviceName, this.eventLoopGroup);
      bootstrap.group(eventLoopGroup);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
          channel.pipeline().addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS))
              .addLast(new HippoEncoder(HippoRequest.class))
              .addLast(new HippoDecoder(HippoResponse.class)).addLast(handler);
        }
      });
      bootstrap.connect(host, port).sync();
    } catch (Exception e) {
      LOGGER.error("hippo client init error:" + this.serviceName, e);
      throw new HippoServiceUnavailableException("[" + this.serviceName + "]服务不可用,初始化失败.", e);
    }
  }


  private void initHostAndPort() {
    String serviceAddress = serviceGovern.getServiceAddress(serviceName);
    if (StringUtils.isBlank(serviceAddress)) {
      throw new HippoServiceUnavailableException("[" + serviceName + "]没有发现可用的服务.");
    }
    String[] split = serviceAddress.split(":");
    this.host = split[0];
    this.port = Integer.parseInt(split[1]);
    if (StringUtils.isBlank(host) || port <= 0 || port > 65532) {
      throw new HippoServiceUnavailableException(
          "[" + serviceName + "]服务参数异常.host=" + host + ",port=" + port);
    }
  }

  public HippoResultCallBack sendAsync(HippoRequest request) throws Exception {
    HippoResultCallBack hippoResultCallBack = new HippoResultCallBack(request, timeout);
    this.handler.sendAsync(hippoResultCallBack);
    return hippoResultCallBack;
  }

  public HippoResponse sendWithCallBack(HippoRequest request) {
    HippoResultCallBack hippoResultCallBack = new HippoResultCallBack(request, timeout);
    return this.handler.sendWithCallBack(hippoResultCallBack);
  }

  public HippoResponse  sendOneWay(HippoRequest hippoRequest) throws Exception {
     return this.handler.sendOneWay(hippoRequest);
  }

}
