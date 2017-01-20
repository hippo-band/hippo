package com.github.hippo.logger;

import com.github.hippo.chain.ChainThreadLocal;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by hanruofei on 16/9/12.
 */
public class ChainOrderConvert extends ClassicConverter {
  @Override
  public String convert(ILoggingEvent event) {
    return String.valueOf(ChainThreadLocal.INSTANCE.getChainOrder());
  }
}
