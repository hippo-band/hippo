package com.github.hippo.netty;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
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
  private int hippoReadTimeout;
  private boolean needTimeout;
  private String clientId;

  private Bootstrap bootstrap = new Bootstrap();
  private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
  private HippoRequestHandler handler;
  private ServiceGovern serviceGovern;

  // 记录这个channel读超时的次数,默认超过6次就断掉连接等待重连
  private AtomicInteger readTimeoutTimes = new AtomicInteger(0);



  public static HippoClientBootstrap getBootstrap(String clientId, int hippoReadTimeout,
      boolean needTimeout, ServiceGovern serviceGovern) throws Exception {

    if (!HippoClientBootstrapMap.containsKey(clientId)) {
      synchronized (HippoClientBootstrapMap.class) {
        if (!HippoClientBootstrapMap.containsKey(clientId)) {
          HippoClientBootstrap hippoClientBootstrap =
              new HippoClientBootstrap(clientId, hippoReadTimeout, needTimeout, serviceGovern);
          HippoClientBootstrapMap.put(clientId, hippoClientBootstrap);
        }
      }
    }
    return HippoClientBootstrapMap.get(clientId);
  }

  private HippoClientBootstrap(String clientId, int hippoReadTimeout, boolean needTimeout,
      ServiceGovern serviceGovern) throws Exception {
    this.clientId = clientId;
    this.hippoReadTimeout = hippoReadTimeout;
    this.needTimeout = needTimeout;
    this.serviceGovern = serviceGovern;
    init();
  }

  private void init() throws Exception {
    initHostAndPort();
    try {
      handler = new HippoRequestHandler(this.clientId, this.eventLoopGroup);
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
      LOGGER.error("send error", e);
      throw e;
    }
  }


  private void initHostAndPort() {
    String serviceAddress = serviceGovern.getServiceAddress(clientId);
    String[] split = serviceAddress.split(":");
    this.host = split[0];
    this.port = Integer.parseInt(split[1]);
  }

  public HippoResultCallBack sendAsync(HippoRequest request) throws Exception {
    HippoResultCallBack hippoResultCallBack =
        new HippoResultCallBack(request, needTimeout, hippoReadTimeout, this);
    this.handler.sendAsync(hippoResultCallBack);
    return hippoResultCallBack;
  }

  public void close() {
    eventLoopGroup.shutdownGracefully();
  }

  public String getClientId() {
    return clientId;
  }

  public AtomicInteger getReadTimeoutTimes() {
    return readTimeoutTimes;
  }
}
