package com.github.hippo.exception;

/**
 *
 * @author wangjian
 */
public class HippoCallTypeNotExistException extends HippoRuntimeException {

    public HippoCallTypeNotExistException(String msg) {
        super(msg);

    }

    public HippoCallTypeNotExistException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
