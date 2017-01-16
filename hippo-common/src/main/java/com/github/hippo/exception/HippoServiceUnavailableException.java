package com.github.hippo.exception;
/**
 * 服务找不到
 * @author sl
 *
 */
public class HippoServiceUnavailableException extends HippoRuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = -405702673172003011L;

  public HippoServiceUnavailableException(String msg) {
    super(msg);

  }

  public HippoServiceUnavailableException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}
