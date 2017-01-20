package com.github.hippo.test;

import java.util.Date;
import java.util.Random;
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
public class HippoClientBootstrapTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoClientBootstrapTest.class);

  private String host;
  private int port;
  private int hippoReadTimeout;
  private boolean needTimeout;
  // 记录这个channel读超时的次数,默认超过6次就断掉连接等待重连
  private AtomicInteger readTimeoutTimes = new AtomicInteger(0);
  private String serviceName;

  private Bootstrap bootstrap = new Bootstrap();
  private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
  // private Channel channel;
  private HippoRequestHandlerTest handler;

  private ServiceGovern serviceGovern;

  public static HippoClientBootstrapTest getBootstrap(String serviceName, int hippoReadTimeout,
      boolean needTimeout, ServiceGovern serviceGovern) throws Exception {
    if (!HippoClientBootstrapMapTest.containsKey(serviceName)) {
      synchronized (HippoClientBootstrapMapTest.class) {
        if (!HippoClientBootstrapMapTest.containsKey(serviceName)) {
          HippoClientBootstrapTest hippoClientBootstrap =
              new HippoClientBootstrapTest(serviceName, hippoReadTimeout, needTimeout, serviceGovern);
          HippoClientBootstrapMapTest.put(serviceName, hippoClientBootstrap);
        }
      }
    }
    return HippoClientBootstrapMapTest.get(serviceName);
  }

  private HippoClientBootstrapTest(String serviceName, int hippoReadTimeout, boolean needTimeout,
      ServiceGovern serviceGovern) throws Exception {
    this.serviceName = serviceName;
    this.hippoReadTimeout = hippoReadTimeout;
    this.needTimeout = needTimeout;
    this.serviceGovern = serviceGovern;
    init();
  }

  private void init() throws Exception {
    initHostAndPort();
    try {
      handler = new HippoRequestHandlerTest(this.serviceName, this.eventLoopGroup);
      bootstrap.group(eventLoopGroup);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
          int readTimeout = 0;
          if (needTimeout) {
            readTimeout = hippoReadTimeout;
          }
          channel.pipeline().addLast(new IdleStateHandler(readTimeout, 30, 0, TimeUnit.SECONDS))
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
    // String serviceAddress = serviceGovern.getServiceAddress(serviceName);
    // String[] split = serviceAddress.split(":");
    // this.host = split[0];
    // this.port = Integer.parseInt(split[1]);
    this.host = "127.0.0.1";
    this.port = 8888;
  }


  public AtomicInteger getReadTimeoutTimes() {
    return readTimeoutTimes;
  }

  public HippoResultCallBackTest sendAsync(HippoRequest request) throws Exception {
    HippoResultCallBackTest hippoResultCallBack =
        new HippoResultCallBackTest(request, needTimeout, hippoReadTimeout, this);
    this.handler.sendAsync(hippoResultCallBack);
    return hippoResultCallBack;
  }


  public static void main(String[] args) throws Exception {

    for (int i = 0; i < 3; i++) {
      new Thread(() -> {

        try {
          HippoClientBootstrapTest bootstrap =
              HippoClientBootstrapTest.getBootstrap("testClient111", 1, false, null);

          HippoRequest request = new HippoRequest();
          request.setServiceName("testClient111");
          String requestId = "12345-" + new Random().nextInt(1000);
          request.setRequestId(requestId);
          HippoResultCallBackTest sendAsync = bootstrap.sendAsync(request);
          System.out.println(
              new Date().toLocaleString() + ">>111>>>" + sendAsync.getResult().getResult());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

    }

    // for (int i = 0; i < 3; i++) {
    // new Thread(() -> {
    //
    //
    // try {
    // HippoClientBootstrapTest bootstrap2 =
    // HippoClientBootstrapTest.getBootstrap("testClient222", 1, false, null);
    //
    // HippoRequest request = new HippoRequest();
    // request.setserviceName("testClient222");
    // String requestId = "abcde-" + new Random().nextInt(1000);
    // request.setRequestId(requestId);
    // HippoResultCallBack sendAsync = bootstrap2.sendAsync(request);
    // System.out.println(
    // new Date().toLocaleString() + ">>222>>>" + sendAsync.getResult().getResult());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }).start();
    // }
    Thread.sleep(3 * 1000);
    for (int i = 0; i < 3; i++) {
      new Thread(() -> {
        try {
          HippoClientBootstrapTest bootstrap3 =
              HippoClientBootstrapTest.getBootstrap("testClient111", 1, true, null);
          HippoRequest request = new HippoRequest();
          request.setServiceName("testClient111");
          String requestId = "56789-" + new Random().nextInt(1000);
          request.setRequestId(requestId);
          HippoResultCallBackTest sendAsync = bootstrap3.sendAsync(request);
          System.out.println(
              new Date().toLocaleString() + ">>333>>>" + sendAsync.getResult().getResult());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();
      Thread.sleep(3 * 1000);
    }
  }

  public void close() {
    eventLoopGroup.shutdownGracefully();
  }

  public String getserviceName() {
    return serviceName;
  }

}
