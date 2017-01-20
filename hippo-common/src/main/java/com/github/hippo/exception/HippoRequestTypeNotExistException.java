package com.github.hippo.exception;
/**
 * 请求类型不存在
 * @author sl
 *
 */
public class HippoRequestTypeNotExistException extends HippoRuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = -405702673172003011L;

  public HippoRequestTypeNotExistException(String msg) {
    super(msg);

  }

  public HippoRequestTypeNotExistException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}
