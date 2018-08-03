package com.github.hippo.netty;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.callback.CallTypeHandler;
import com.github.hippo.callback.RemoteCallHandler;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.exception.HippoServiceException;
import com.github.hippo.threadpool.HippoClientProcessPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * client process handler
 *
 * @author sl
 */
public class HippoRequestHandler extends SimpleChannelInboundHandler<HippoResponse> {


    private static final Logger LOGGER = LoggerFactory.getLogger(HippoRequestHandler.class);

    private ConcurrentHashMap<String, HippoResultCallBack> callBackMap = new ConcurrentHashMap<>();
    private String serviceName;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private String host;
    private int port;

    public HippoRequestHandler(String serviceName, EventLoopGroup eventLoopGroup, String host,
                               int port) {
        this.serviceName = serviceName;
        this.eventLoopGroup = eventLoopGroup;
        this.host = host;
        this.port = port;
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext arg0, HippoResponse response) throws Exception {
        // ping不需要记录到返回结果MAP里
        if (response != null && !("-99").equals(response.getRequestId())) {
            HippoClientProcessPool.INSTANCE.getPool().execute(() -> {
                HippoResultCallBack hippoResultCallBack = callBackMap.remove(response.getRequestId());
                // oneway方式没有hippoResultCallBack
                if (hippoResultCallBack == null) {
                    return;
                }
                RemoteCallHandler handler = CallTypeHandler.INSTANCE
                        .getHandler(hippoResultCallBack.getHippoRequest().getCallType());
                if (handler != null) {
                    handler.back(hippoResultCallBack, response);
                }
            });
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                HippoRequest hippoRequest = new HippoRequest();
                hippoRequest.setServiceName(serviceName);
                hippoRequest.setRequestId("-99");
                hippoRequest.setRequestType(HippoRequestEnum.PING.getType());
                ctx.writeAndFlush(hippoRequest);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.close();
        shutdown();
        this.callBackMap.values().forEach(c -> {
            HippoResponse response = new HippoResponse();
            response.setError(true);
            response.setThrowable(
                    new HippoServiceException("hippo server error trigger client channelInactive"));
            c.signal(response);
        });
        callBackMap.clear();
        HippoClientBootstrapMap.remove(serviceName, host, port);

    }

    public void shutdown() {
        try {
            if (eventLoopGroup != null && !eventLoopGroup.isShutdown()
                    && !eventLoopGroup.isShuttingDown()) {
                eventLoopGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendAsync(HippoResultCallBack hippoResultCallBack) {
        callBackMap.put(hippoResultCallBack.getHippoRequest().getRequestId(), hippoResultCallBack);
        this.channel.writeAndFlush(hippoResultCallBack.getHippoRequest());
    }

    public HippoResponse sendOneWay(HippoRequest hippoRequest) {
        this.channel.writeAndFlush(hippoRequest);
        return buildEmptyHippoResponse(hippoRequest);
    }

    public HippoResponse sendWithCallBack(HippoResultCallBack hippoResultCallBack) {
        sendAsync(hippoResultCallBack);
        return buildEmptyHippoResponse(hippoResultCallBack.getHippoRequest());
    }

    private HippoResponse buildEmptyHippoResponse(HippoRequest hippoRequest) {
        HippoResponse hippoResponse = new HippoResponse();
        hippoResponse.setRequestId(hippoRequest.getRequestId());
        hippoResponse.setChainId(hippoRequest.getChainId());
        hippoResponse.setChainOrder(hippoRequest.getChainOrder());
        return hippoResponse;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("netty client error", cause.fillInStackTrace());
        HippoClientBootstrapMap.remove(serviceName, host, port);
        ctx.close();
        shutdown();
    }

}
