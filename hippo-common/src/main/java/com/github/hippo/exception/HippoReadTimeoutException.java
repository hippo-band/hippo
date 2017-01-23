package com.github.hippo.exception;
/**
 * 服务找不到
 * @author sl
 *
 */
public class HippoReadTimeoutException extends HippoRuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = -405702673172003011L;

  public HippoReadTimeoutException(String msg) {
    super(msg);

  }

  public HippoReadTimeoutException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}
