package com.github.hippo.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * hippo netty client
 * 
 * @author sl
 *
 */
@Sharable
public class HippoNettyClient extends SimpleChannelInboundHandler<HippoResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoNettyClient.class);

  private String host;
  private int port;
  private int hippoReadTimeout;
  private int hippoWriteTimeout;
  private boolean needTimeout;

  private HippoResponse response;

  public HippoNettyClient(String host, int port, int hippoReadTimeout, int hippoWriteTimeout,
      boolean needTimeout) {
    this.host = host;
    this.port = port;
    this.hippoReadTimeout = hippoReadTimeout;
    this.hippoWriteTimeout = hippoWriteTimeout;
    this.needTimeout = needTimeout;
  }


  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.error("netty client error", cause.fillInStackTrace());
    ctx.close();
  }

  /**
   * send to server
   * 
   * @param request HippoRequest
   * @return HippoResponse
   * @throws Exception
   */
  public HippoResponse send(HippoRequest request) throws Exception {
    Bootstrap bootstrap = new Bootstrap();
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try {
      bootstrap.group(eventLoopGroup);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.TCP_NODELAY, true);

      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
          ChannelPipeline pipeline = channel.pipeline();
          if (needTimeout) {
            if (hippoReadTimeout <= 0) {
              hippoReadTimeout = 3;// default
            }
            if (hippoWriteTimeout <= 0) {
              hippoWriteTimeout = 1;// default
            }
            pipeline.addLast(new ReadTimeoutHandler(hippoReadTimeout));
            pipeline.addLast(new WriteTimeoutHandler(hippoWriteTimeout));
          }
          pipeline.addLast(new HippoEncoder(HippoRequest.class));
          pipeline.addLast(new HippoDecoder(HippoResponse.class));
          pipeline.addLast(HippoNettyClient.this);
        }
      });
      ChannelFuture future = bootstrap.connect(host, port).sync();
      Channel channel = future.channel();
      channel.writeAndFlush(request).sync();
      channel.closeFuture().sync();
      return response;
    } catch (Exception e) {
      LOGGER.error("send error", e);
      throw e;
    } finally {
      eventLoopGroup.shutdownGracefully();
    }

  }

  @Override
  protected void channelRead0(ChannelHandlerContext arg0, HippoResponse response) throws Exception {
    this.response = response;
  }
}
