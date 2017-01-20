package com.github.hippo.logger;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.hippo.cache.MsgThreadLocal;

/**
 * Created by hanruofei on 16/9/12.
 */
public class MsgIdConvert extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        return MsgThreadLocal.Instance.loggerGetMsgId();
    }
}
