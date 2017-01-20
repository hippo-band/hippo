package com.github.hippo.chain;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by hanruofei on 16/8/30. 串起rpc调用链
 */
public enum ChainThreadLocal {
  INSTANCE;

  /**
   * 存放chainId
   */
  private ThreadLocal<String> chainId = new ThreadLocal<>();
  /**
   * 存放chainOrder (调用链顺序)
   */
  private ThreadLocal<Integer> chainOrder = new ThreadLocal<>();

  public void clearTL() {
    chainId.remove();
    chainOrder.remove();
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

  /**
   * chainOrder+1
   * 
   * @param co
   */
  public void incChainOrder(int co) {
    this.chainOrder.set(co + 1);
  }

}
