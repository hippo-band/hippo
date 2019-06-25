package com.github.hippo.chain;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by hanruofei on 16/8/30. 串起rpc调用链
 */
public enum ChainThreadLocal {
    INSTANCE;

    /**
     * 存放chainId
     */
    private ThreadLocal<String> chainId = new ThreadLocal<>();

    private ThreadLocal<String> spanId = new ThreadLocal<>();
    /**
     * 存放chainOrder (调用链顺序)
     */
    private ThreadLocal<Integer> chainOrder = new ThreadLocal<>();

    public void clearTL() {
        chainId.remove();
        spanId.remove();
    }

    public void clearSpanId() {
        spanId.remove();
    }

    /**
     * 获取ThreadLocal中的chainId value
     *
     * @return chainId
     */
    public String getChainId() {
        if (StringUtils.isBlank(chainId.get())) {
            chainId.set(UUID.randomUUID().toString());
        }
        return chainId.get();
    }

    /**
     * 获取ThreadLocal中的requestId value
     *
     * @return requestId
     */
    public String getSpanId() {
        if (StringUtils.isBlank(spanId.get())) {
            spanId.set(UUID.randomUUID().toString());
        }
        return spanId.get();
    }

    /**
     * 获取ThreadLocal中的chainOrder value
     *
     * @return chainOrder
     */
    public int getChainOrder() {
        if (Objects.isNull(chainOrder.get())) {
            chainOrder.set(1);
        }
        return chainOrder.get();
    }


    /**
     * 插入ThreadLocal中的value
     *
     * @param chainId chainId
     */
    public void setChainId(String chainId) {
        this.chainId.set(chainId);
    }

    public void setSpanId(String spanId) {
        this.spanId.set(spanId);
    }

    /**
     * chainOrder+1
     *
     * @param co chainOrder
     */
    public void incChainOrder(int co) {
        this.chainOrder.set(co + 1);
    }

}
