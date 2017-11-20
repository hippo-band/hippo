package com.github.hippo.exception;

/**
 *
 * @author wangjian
 */
public class HippoCallTypeNotExistException extends HippoRuntimeException {

  /**
  * 
  */
  private static final long serialVersionUID = 8196501755259591782L;

  public HippoCallTypeNotExistException(String msg) {
    super(msg);

  }

  public HippoCallTypeNotExistException(String msg, Throwable throwable) {
    super(msg, throwable);
  }
}
