package com.github.hippo.netty;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoServiceUnavailableException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * hippo client Bootstrap
 *
 * @author sl
 */
public class HippoClientBootstrap implements Comparable<HippoClientBootstrap> {


    private String host;
    private int port;
    private String serviceName;
    private HippoRequestHandler handler;
    private AtomicLong invokeTimes = new AtomicLong(0);
    private NioEventLoopGroup eventLoopGroup;


    public HippoClientBootstrap(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        eventLoopGroup = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            handler = new HippoRequestHandler(this.serviceName, eventLoopGroup, this.host, this.port);
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
            throw new HippoServiceUnavailableException(
                    "[" + this.serviceName + "]服务不可用,初始化失败.host:" + host + ",port:" + port, e);
        } finally {
            HippoClientBootstrapMap.refreshInvokeTimes(serviceName);
        }
    }


    public HippoResultCallBack sendAsync(HippoRequest request, int timeout) {
        HippoResultCallBack hippoResultCallBack = new HippoResultCallBack(request, timeout);
        this.handler.sendAsync(hippoResultCallBack);
        //this.invokeTimes.decrementAndGet();
        return hippoResultCallBack;
    }

    public HippoResponse sendWithCallBack(HippoRequest request, int timeout) {
        HippoResultCallBack hippoResultCallBack = new HippoResultCallBack(request, timeout);
        //this.invokeTimes.decrementAndGet();
        return this.handler.sendWithCallBack(hippoResultCallBack);
    }

    public HippoResponse sendOneWay(HippoRequest hippoRequest) {
        //this.invokeTimes.decrementAndGet();
        return this.handler.sendOneWay(hippoRequest);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public AtomicLong getInvokeTimes() {
        return invokeTimes;
    }

    /**
     * 重置invokeTimes
     */
    public void refreshInvokeTimes() {
        invokeTimes.set(0);
    }

    @Override
    public int compareTo(HippoClientBootstrap o) {
        Long l1 = this.invokeTimes.get();
        Long l2 = o.invokeTimes.get();
        return l1.compareTo(l2);
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
}
