package com.github.hippo.netty;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.enums.HippoRequestEnum;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;

public class HippoRequestHandler extends SimpleChannelInboundHandler<HippoResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoRequestHandler.class);
  private HippoResponse response;
  private volatile ConcurrentHashMap<String, HippoResponse> responseMap = new ConcurrentHashMap<>();
  private String cliendId;
  private EventLoopGroup eventLoopGroup;
  private volatile boolean isInActive;
  private Channel channel;

  public HippoRequestHandler(String cliendId, EventLoopGroup eventLoopGroup) {
    this.cliendId = cliendId;
    this.eventLoopGroup = eventLoopGroup;
  }

  public HippoResponse getResponse(String requestId) {
    return responseMap.remove(requestId);
  }


  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    super.channelRegistered(ctx);
    this.channel = ctx.channel();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.error("netty client error", cause);
    // 读超时就不close了,保持长链接,不过这里可以+上计数
    if (cause instanceof ReadTimeoutException) {
      this.response = new HippoResponse();
      this.response.setError(true);
      this.response.setThrowable(cause);
    } else {
      // close会触发channelInactive方法
      ctx.close();
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext arg0, HippoResponse response) throws Exception {
    this.response = response;
    System.out.println(response.getRequestId() + ">>>" + response.getResult());
    // ping不需要记录到返回结果MAP里
    if (response != null && !("-99").equals(response.getRequestId())) {
      responseMap.put(response.getRequestId(), response);
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.WRITER_IDLE) {
        HippoRequest hippoRequest = new HippoRequest();
        hippoRequest.setClientId(cliendId);
        hippoRequest.setRequestType(HippoRequestEnum.PING.getType());
        ctx.writeAndFlush(hippoRequest);
      } else if (e.state() == IdleState.READER_IDLE) {
        // 可以计数,超时可以放到具体的某个线程
        throw ReadTimeoutException.INSTANCE;
      }
    }
  }

  /**
   * 没想好是否要主动重连 下一个请求进来也是能重连的
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    ctx.close();
    if (eventLoopGroup != null) {
      eventLoopGroup.shutdownGracefully();
    }
    isInActive = true;
  }

  public boolean isInActive() {
    return isInActive;
  }

  public void sendAsync(HippoRequest request) {
    this.channel.writeAndFlush(request);
  }
}
